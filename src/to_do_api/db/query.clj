(ns to-do-api.db.query
  (:require [clojure.java.jdbc :as j]
            [to-do-api.db.database :refer [db]]))

(defn gen-id []
  (str (java.util.UUID/randomUUID)))

(defn now [] (new java.util.Date))

(defn register-user [params]
  (let [_ (println params)]
    (j/insert! db :users (conj {:id (gen-id)} params))))

(defn create-state [params]
  (j/insert! db :states (conj {:id (gen-id)
                               :created_at (now)} params)))

(defn update-state [params]
  (j/update! db :states (conj {:updated_at (now)}
                              params) ["id = ?" (:id params)]))

(defn delete-state [id]
  (j/delete! db :states ["id = ?" id]))
