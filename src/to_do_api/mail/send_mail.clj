(ns to-do-api.mail.send-mail
  (:require [to-do-api.mail.mailer :refer [mail-con]]
            [postal.core :refer [send-message]]))

(def application-name "dev@todo.com")

(defn send-to [to subject body]
  (send-message mail-con
                {:from application-name
                 :to to
                 :subject subject
                 :body body}))
