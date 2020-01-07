(defproject to-do-api "0.1.0"
  :description "Client and server side template"
  :url "https://github/burkaydurdu/charizard"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [ring/ring-json "0.5.0"]
                 [ring/ring-defaults "0.3.2"]
                 [clj-commons/secretary "1.2.4"]
                 [org.clojure/java.jdbc "0.7.10"]
                 [org.postgresql/postgresql "9.4.1212"]
                 [migratus-lein "0.7.2"]]
  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-ring "0.12.5"]
            [lein-cljfmt "0.6.4"]
            [migratus-lein "0.7.2"]]

  :source-paths ["src"]
  
  :resource-paths ["resources"]

  :main ^:skip-aot to-do-api.core

  :ring {:handler to-do-api.core/handler}
   
  :min-lein-version "2.6.1"

  :migratus {:store :database
             :migration-dir "migrations/"
             :db {:classname "org.postgresql.Driver"
                  :subprotocol "postgresql"
                  :subname "//localhost/to_do"
                  :user "burkaydurdu"}}
  
  :profiles {:uberjar {:aot :all}

             :dev {:dependencies [[binaryage/devtools "0.9.10"]
                                  [figwheel-sidecar "0.5.18"]
                                  [ring "1.7.1"]
                                  [compojure "1.6.1"]
                                  [amalloy/ring-gzip-middleware "0.1.3"]]

                   :plugins [[lein-figwheel "0.5.18"]]}})