(ns to-do-api.db.query
  (:require [clojure.java.jdbc :as j]
            [to-do-api.db.database :refer [db]]))

(def register-params [:name :email :gender :password])

(def login-params [:email :password])

(def exp-data (partial apply conj))

(defn gen-id [] (str (java.util.UUID/randomUUID)))

(defn now [] (new java.util.Date))

(defn serializer-data [data data-keys]
  (if (vector? data-keys)
    (select-keys data data-keys)
    data))

(defn- register-user [params]
  (-> (j/insert! db :users (conj {:id (gen-id)} params))
      exp-data
      (serializer-data [:id :name :email])))

(defn get-user-with-token [token]
  (j/query db ["SELECT *FROM users where token = ?" token] {:result-set-fn first}))

(defn- get-user [params]
  (let [email (:email params)
        password (:password params)]
    (j/update! db :users {:token (gen-id)} ["email = ? and password = ?" email password])
    (-> (j/query db ["SELECT *FROM users where email = ? and password = ?" email password] {:result-set-fn first})
        (serializer-data [:id :name :email :token]))))

(defn- get-user-states [user]
  (j/query db ["SELECT *FROM states WHERE user_id = ?" (:id user)]))

(defn- create-state [params]
  (j/insert! db :states (conj {:id (gen-id)
                               :created_at (now)} params)))

(defn- update-state [params]
  (j/update! db :states (conj {:updated_at (now)}
                              params) ["id = ?" (:id params)]))

(defn- delete-state [id]
  (j/delete! db :states ["id = ?" id]))

(defn- params-control [role params]
  (case role
    :register (= (count (select-keys params register-params))
                 (count register-params))
    :login    (= (count (select-keys params login-params))
                 (count login-params))
    :state    true
    false))

(defn query-control [role params]
  (try
    (if (params-control role params)
        (case role
          :register  (register-user params)
          :login     (get-user params)
          :state     (get-user-states (:user params)))
        (throw (Exception. "Invalid params")))
    (catch Exception e
      {:error true :message (.getMessage e)})))

