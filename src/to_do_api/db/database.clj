(ns to-do-api.db.database)

(def db {:dbtype "postgresql"
         :dbname "to_do"
         :host   (or (System/getenv "POSTGRES_HOST") "localhost")
         :user   (or (System/getenv "POSTGRES_USER") "burkaydurdu")
         :pass   (or (System/getenv "POSTGRES_PASSWORD") nil)})
