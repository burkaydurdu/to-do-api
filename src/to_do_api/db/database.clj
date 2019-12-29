(ns to-do-api.db.database
  (:require [clojure.java.jdbc :as j]))

(def db {:dbtype "postgresql"
         :dbname "to_do"
         :host   "localhost"
         :user   "burkaydurdu"})

