(ns to-do-api.mail.mailer)

(def mail-con
  {:host "smtp.gmail.com"
   :ssl  true
   :user (System/getenv "MAIL_USER")
   :pass (System/getenv "MAIL_PASS")})
