(ns to-do-api.db.query
  (:require [clojure.java.jdbc :as j]
            [to-do-api.db.database :refer [db]]))

(defn gen-id []
  (str (java.util.UUID/randomUUID)))

(defn now [] (new java.util.Date))

(def exp-data (partial apply conj))

(defn serializer-data [data data-keys]
  (if (vector? data-keys)
    (select-keys data data-keys)
    data))

(defn register-user [params]
  (try
    (-> (j/insert! db :users (conj {:id (gen-id)} params))
        exp-data
        (serializer-data [:id :name :email]))
    (catch Exception e
      {:error true :message (.getMessage e)})))

(defn get-user [params]
  (let [email (:email params)
        password (:password params)
        id (gen-id)]
    (try
      (j/update! db :users {:token id} ["email = ? and password = ?" email password])
      (-> (j/query db ["SELECT *FROM users where email = ? and password = ?" email password]
                   {:result-set-fn first})
          (serializer-data [:id :name :email :token]))
      (catch Exception e
        {:error true :message (.getMessage e)}))))

(defn create-state [params]
  (j/insert! db :states (conj {:id (gen-id)
                               :created_at (now)} params)))

(defn update-state [params]
  (j/update! db :states (conj {:updated_at (now)}
                              params) ["id = ?" (:id params)]))

(defn delete-state [id]
  (j/delete! db :states ["id = ?" id]))
