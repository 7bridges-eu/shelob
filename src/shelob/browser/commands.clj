(ns shelob.browser.commands
  (:require
   [shelob.browser :as shb]))

(defmulti browser-command :msg)

(defmethod browser-command :go [{:keys [driver url]}]
  (shb/go driver url))

(defmethod browser-command :title [{:keys [driver]}]
  (.getTitle driver))

(defmethod browser-command :source [{:keys [driver]}]
  (.getPageSource driver))
