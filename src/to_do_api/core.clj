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
            [compojure.route :as route]
            [to-do-api.db.query :as query]))

(defn todo-response [data]
  (let [error? (boolean (:error data))]
    {:status (if error? 404 200) :body data}))

(defn request-control [request]
  (let [header (:headers request)
        token (get header "token")]
    {:user (query/get-user-with-token token)
     :body (:body request)}))

(defn mail-verify-callback [data]
  (conj (todo-response data) {:headers {"Content-Type" "text/plain"}}))

(def not-found-data "<meta http-equiv = 'refresh' content = '0; url = http://listoftodo.com' />")

(defn mail-verify [data]
  (if (= data [1])
    not-found-data
    not-found-data))

(defroutes rr
  (GET "/check_token" request
       (todo-response {:error (-> (request-control request) :user nil?)}))
  (GET "/reset_password" {params :params}
       (todo-response (query/query-control :reset-password params)))
  (GET "/create_password" {params :params}
       (todo-response (query/query-control :create-password params)))
  (GET "/verify_mail" {params :params}
        (mail-verify (query/query-control :mail-verify params)))
  (POST "/register" {params :body}
        (todo-response (query/query-control :register params)))
  (POST "/login" {params :body}
        (todo-response (query/query-control :login params)))
  (POST "/logout" request
        (todo-response (query/query-control :logout (request-control request))))
  (PUT "/user" request
        (todo-response (query/query-control :user-update (request-control request))))
  (GET "/state" request
        (todo-response (query/query-control :state (request-control request))))
  (POST "/state" request
        (todo-response (query/query-control :states (request-control request))))
  (route/not-found not-found-data))

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
      wrap-gzip))

(defn -main []
  (jetty/run-jetty handler {:port 3011}))
