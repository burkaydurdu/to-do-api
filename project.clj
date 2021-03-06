(defproject to-do-api "0.1.0"
  :description "Client and server side template"
  :url "https://github/burkaydurdu/charizard"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [ring "1.7.1"]
                 [ring/ring-json "0.5.0"]
                 [ring/ring-defaults "0.3.2"]
                 [ring-cors "0.1.12"]
                 [ring/ring-codec "1.1.2"]
                 [amalloy/ring-gzip-middleware "0.1.3"]
                 [compojure "1.6.1"]
                 [clj-commons/secretary "1.2.4"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.postgresql/postgresql "42.2.9.jre7"]
                 [migratus-lein "0.7.2"]
                 [digest "1.4.9"]
                 [com.draines/postal "2.0.3"]
                 [org.clojure/core.async "0.7.559"]]
  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-ring "0.12.5"]
            [lein-cljfmt "0.6.4"]
            [migratus-lein "0.7.2"]]

  :source-paths ["src"]

  :resource-paths ["resources"]

  :main ^:skip-aot to-do-api.core

  :ring {:handler to-do-api.core/handler}

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :min-lein-version "2.6.1"

  :migratus {:store :database
             :migration-dir "migrations/"
             :db {:classname "org.postgresql.Driver"
                  :subprotocol "postgresql"
                  :subname ~(or (System/getenv "POSTGRES_SUBNAME") "//localhost/to_do")
                  :user ~(or (System/getenv "POSTGRES_USER") "burkaydurdu")
                  :password ~(or (System/getenv "POSTGRES_PASSWORD") nil)}}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.10"]
                                  [figwheel-sidecar "0.5.18"]]}
             :uberjar {:omit-source  true
                        :aot          :all
                        :auto-clean   false
                        :uberjar-name "todo_back.jar"
                        :source-paths ["src/clj" "src/cljs"]}})

