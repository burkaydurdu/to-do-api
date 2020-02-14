(ns to-do-api.db.query
  (:require [clojure.java.jdbc :as j]
            [to-do-api.db.database :refer [db]]
            [digest :as digest]
            [to-do-api.mail.send-mail :refer [send-to]]))

(def register-params [:name :email :gender :password])

(def login-params [:email :password])

(def user-update-params [:name :dark_mode :font_size])

(def mail-verify-params [:email :token])

(def exp-data (partial apply conj))

(def date (java.util.TimeZone/setDefault (java.util.TimeZone/getTimeZone "UTC")))

(defn gen-id [] (str (java.util.UUID/randomUUID)))

(defn serializer-data [data data-keys]
  (cond
    (empty? data) nil
    (vector? data-keys) (select-keys data data-keys)
    :else data))

(defn get-server-ip-address []
 (or (System/getenv "SERVER_IP") "http://localhost:3011"))

(defn get-front-ip-address []
  (or (System/getenv "FRONT_IP") "http://localhost:3449"))

(defn register-mail-body [user mail-verify-token]
  (format "<h3> Hello %s </h3><br/><a href=\"%s/verify_mail?email=%s&token=%s\"> Activate Account </a>"
          (:name user) (get-server-ip-address) (:email user) mail-verify-token))

(defn reset-password-mail-body [email reset-token]
  (format "<h3> Hello </h3><br/><a href=\"%s/#/create_password?email=%s&token=%s\"> Reset Password </a>"
           (get-front-ip-address) email  reset-token))

(defn- register-user [params]
  (let [mail-verify-token (gen-id)
        user (-> (j/insert! db :users (merge params {:id (gen-id)
                                                     :mail_verify_token mail-verify-token
                                                     :password (digest/md5 (:password params))}))
                 exp-data
                 (serializer-data [:id :name :email]))
        mail (send-to (:email params) "[TODO] Activate Account" [{:type "text/html"}
                                                                  :content (register-mail-body user mail-verify-token)])]
    (if mail
      user
      (throw (Exception. "Error")))))

(defn get-user-with-token [token]
  (j/query db ["SELECT *FROM users where token = ?" token] {:result-set-fn first}))

(defn- set-token-in-user [email password]
  (j/update! db :users {:token (gen-id)} ["email = ? and password = ?" email (digest/md5 password)]))

(defn- user-update [params]
  (j/update! db :users (:body params) ["token = ?" (-> params :user :token)]))

(defn- find-user-with-confirm [email password]
  (-> (j/query db ["SELECT *FROM users where email = ? and password = ? and mail_verify = true" email (digest/md5 password)] {:result-set-fn first})
      (serializer-data [:id :name :email :token :dark_mode :font_size])))

(defn- get-user [params]
  (let [email (:email params)
        password (:password params)]
    (set-token-in-user email password)
    (if-let [user (find-user-with-confirm email password)]
      user
      (throw (Exception. "User not found!")))))

(defn- get-user-states [user]
  (j/query db ["SELECT *FROM states WHERE user_id = ?" (:id user)]))

(defn multi-create-and-update-query [data t-con]
  (if (empty? data)
    '(1)
    (j/db-do-prepared
      t-con
      (concat ["INSERT INTO states (id, user_id, title, all_done, s_order, created_at)
               VALUES (?,?,?,?,?,NOW()::timestamp) ON CONFLICT (id)
               DO UPDATE SET title = EXCLUDED.title, all_done = EXCLUDED.all_done,
               s_order = EXCLUDED.s_order, updated_at = NOW()::timestamp;"] data) {:multi? true})))

(defn multi-delete-query [data t-con]
  (if (empty? data)
    '(1)
    (j/db-do-prepared
      t-con
      (concat ["DELETE FROM states Where id = ? and user_id = ?"] data) {:multi? true})))

(defn- create-and-update [params]
  (let [user-id            (-> params :user :id)
        delete-data        (keep #(vector % user-id) (-> params :body :delete-list))
        create-update-data (mapv #(vector (:id %) user-id (:title %) (:all_done %) (:s_order %))
                                 (-> params :body :create-or-update-list))]
    (j/with-db-transaction [t-con db]
       (multi-create-and-update-query create-update-data t-con)
       (multi-delete-query delete-data t-con))))

(defn- delete-states [params]
  (let [user-id (-> params :user :id)
        data    (-> params :body :delete-list)]
    (map #(j/delete! db :states ["id = ? and user_id = ?" % user-id]) data)))

(defn- delete-user-token [user]
  (j/update! db :users {:token nil} ["id = ? and token = ?" (:id user) (:token user)]))

(defn- mail-verify [params]
  (j/update! db :users {:mail_verify true} ["mail_verify_token = ? and email = ?"
                                            (:token params) (:email params)]))

(defn- reset-password [params]
  (let [reset-token (gen-id)
        user (j/update! db :users {:reset_password_token reset-token} ["email = ?" (:email params)])
        mail (send-to (:email params) "[TODO] Reset Password" [{:type "text/html"
                                                                :content (reset-password-mail-body
                                                                          (:email params)
                                                                          reset-token)}])]
    (if (and (= 1 (first user)) mail)
      user
      (throw (Exception. "User not found")))))

(defn- create-password [params]
  (j/update! db
             :users
             {:password (digest/md5 (:password params))}
             ["email = ? and reset_password_token = ?" (:email params) (:token params)]))

(defn valid-params-control [params r-params]
  (= (count (select-keys params r-params))
     (count r-params)))

(defn- params-control [role params]
  (case role
    :register (valid-params-control params register-params)
    :login    (valid-params-control params login-params)
    :state    true
    :states   true
    :logout   (-> params :user nil? not)
    :user-update (valid-params-control (:body params) user-update-params)
    :mail-verify (valid-params-control params mail-verify-params)
    :reset-password (valid-params-control params [:email])
    :create-password (valid-params-control params [:token :email :password])
    false))

(defn query-control [role params]
  (try
    (if (params-control role params)
        (case role
          :register    (register-user params)
          :login       (get-user params)
          :state       (get-user-states (:user params))
          :states      (create-and-update params)
          :logout      (delete-user-token (:user params))
          :user-update (user-update params)
          :mail-verify (mail-verify params)
          :reset-password (reset-password params)
          :create-password (create-password params))
        (throw (Exception. "Invalid params")))
    (catch Exception e
      {:error true :message (.getMessage e)})))

