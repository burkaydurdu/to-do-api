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

(defn todo-response [data]
  (let [error? (boolean (:error data))]
    {:status (if error? 404 200) :body data}))

(defn request-control [request]
  (let [header (:headers request)
        token (get header "token")]
    {:user (query/get-user-with-token token)}))

(defroutes rr
  (POST "/register" {params :body} 
        (todo-response (query/query-control :register params)))
  (POST "/login" {params :body}
        (todo-response (query/query-control :login params)))
  (GET "/state" request
        (todo-response (query/query-control :state (request-control request))))
  (POST "/state" {params :body} 
        (todo-response (query/query-control :create-state params)))
  (PUT "/state"  {params :body}  
       (todo-response (query/query-control :update-state params)))
  (DELETE "/state" {params :body} 
          (todo-response (query/query-control :delete-state params))))

  ;(GET "/ip" request (session-handler request))
  ;(GET "/dede" [] (r/content-type (r/response {:name "burkay"}) "text/json")) 
(def handler
    (->  
      rr
      wrap-reload
      (wrap-defaults api-defaults)
      (wrap-json-body {:keywords? true})
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
