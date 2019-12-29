(ns to-do-api.core
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.defaults :refer [api-defaults wrap-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.cors :refer [wrap-cors]]
            [compojure.core :refer :all]
            [to-do-api.db.query :as query]))


#_(defn session-handler 
  [{session :session cookies :cookies}]
  (let [count (:count session 0)
        session (assoc session :count (inc count))
        response  (r/response {:name "Burkay"
                               :surname "Durdu"
                               :session (:count session)
                               :cookies cookies})]
     (-> (r/content-type response "text/plain")
        (assoc :cookies {:session-id {:value "session-id-hash"}})
        (assoc :session session))))

(defroutes rr
  (POST "/register" {params :body} 
        (println params))
  (POST "/state" {params :multipart-params} (query/create-state params))
  (PUT "/state"  {params :multipart-params} (query/update-state params))
  (DELETE "/state" request (query/delete-state request))
  ;(GET "/ip" request (session-handler request))
  ;(GET "/dede" [] (r/content-type (r/response {:name "burkay"}) "text/json")) 
  )

(def handler
    (->  
      rr
      wrap-reload
      (wrap-defaults api-defaults)
      wrap-json-response
      wrap-keyword-params
      wrap-params
      wrap-multipart-params
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :post :put :delete])
      wrap-gzip
))

(defn -main []
  (jetty/run-jetty handler {:port 3011}))
