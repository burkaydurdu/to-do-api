(ns to-do-api.db.query
  (:require [clojure.java.jdbc :as j]
            [to-do-api.db.database :refer [db]]))

(def register-params [:name :email :gender :password])

(def login-params [:email :password])

(def exp-data (partial apply conj))

(defn gen-id [] (str (java.util.UUID/randomUUID)))

(def date (java.util.TimeZone/setDefault (java.util.TimeZone/getTimeZone "UTC")))

(defn serializer-data [data data-keys]
  (cond
    (empty? data) nil
    (vector? data-keys) (select-keys data data-keys)
    :else data))

(defn- register-user [params]
  (-> (j/insert! db :users (conj {:id (gen-id)} params))
      exp-data
      (serializer-data [:id :name :email])))

(defn get-user-with-token [token]
  (j/query db ["SELECT *FROM users where token = ?" token] {:result-set-fn first}))

(defn- set-token-in-user [email password]
  (j/update! db :users {:token (gen-id)} ["email = ? and password = ?" email password]))

(defn- find-user [email password]
  (-> (j/query db ["SELECT *FROM users where email = ? and password = ?" email password] {:result-set-fn first})
      (serializer-data [:id :name :email :token])))

(defn- get-user [params]
  (let [email (:email params)
        password (:password params)]
    (set-token-in-user email password)
    (if-let [user (find-user email password)]
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

(defn- params-control [role params]
  (case role
    :register (= (count (select-keys params register-params))
                 (count register-params))
    :login    (= (count (select-keys params login-params))
                 (count login-params))
    :state    true
    :states   true
    false))

(defn query-control [role params]
  (try
    (if (params-control role params)
        (case role
          :register   (register-user params)
          :login      (get-user params)
          :state      (get-user-states (:user params))
          :states     (create-and-update params))
        (throw (Exception. "Invalid params")))
    (catch Exception e
      {:error true :message (.getMessage e)})))

