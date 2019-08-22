(ns shelob.browser.commands
  (:require
   [shelob.browser :as shb]))

(defmulti browser-command :msg)

(defmethod browser-command :go [{:keys [driver url]}]
  (println "Going to" url)
  (shb/go driver url))

(defmethod browser-command :get-title [{:keys [driver]}]
  (println "Get title")
  (.getTitle driver))
