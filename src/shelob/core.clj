;; Copyright 2019 7bridges s.r.l.
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;; http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns shelob.core
  "The entry point of shelob.
  This namespace contains function to initialise, stop, and reset shelob, and a
  couple of functions to send messages through shelob.
  Logging facilities are initialised here."
  (:require
   [clojure.spec.alpha :as sp]
   [expound.alpha :as e]
   [shelob.driver :as shd]
   [shelob.messages :as shm]
   [taoensso.timbre :as timbre]
   [taoensso.timbre.appenders.core :as appenders]))

(sp/def ::browser keyword?)
(sp/def ::driver-options (sp/keys :req-un [::browser]))
(sp/def ::log-file string?)
(sp/def ::log-level keyword?)
(sp/def ::pool-size number?)
(sp/def ::init-messages vector?)

(sp/def ::context
  (sp/keys :req-un [::driver-options]
           :opt-un [::log-file ::log-level ::pool-size ::init-messages]))

(defn init-log
  "Initialises logging system using :log-file and :log-level in `ctx`."
  [ctx]
  (timbre/merge-config!
   {:appenders {:spit (appenders/spit-appender {:fname (:log-file ctx "shelob.log")})}
    :level (:log-level ctx :debug)}))

(defn exception-default-fn
  "Prints out exception"
  [_source e]
  (println "Exception!" (.getMessage e)))

(defn init
  "Validates `ctx` against `::context` and initialise shelob."
  [ctx]
  (timbre/debugf "Initialising shelob with context: %s" ctx)
  (if (= (sp/conform ::context ctx) ::sp/invalid)
    (throw (ex-info "Invalid context" (e/expound ::context ctx)))
    (let [init-messages (:init-messages ctx)]
      (init-log ctx)
      (shd/init-driver-pool ctx)
      (when init-messages
        (shm/process-messages init-messages exception-default-fn)))))

(defn send-message
  "Sends a single `message` to the executors."
  ([ctx scrape-fn message]
   (send-message ctx scrape-fn exception-default-fn message))
  ([ctx scrape-fn exception-fn message]
   (timbre/debugf "Sending message: %s" message)
   (cond
     (nil? ctx)
     (do
       (timbre/debugf "`ctx` is invalid: %s" ctx)
       (throw (ex-info "`ctx` is invalid" {:ctx ctx})))

     (nil? scrape-fn)
     (do
       (timbre/debugf "`scrape-fn` is invalid: %s" scrape-fn)
       (throw (ex-info "`scrape-fn` is invalid" {:scrape-fn scrape-fn})))

     (nil? message)
     (do
       (timbre/debugf "`message` is invalid: %s" message)
       (throw (ex-info "`message` is invalid" {:message message})))

     :else
     (shm/send-batch-messages ctx [message] scrape-fn exception-fn))))

(defn send-messages
  "Sends a collection of `messages` to the executors."
  ([ctx scrape-fn messages]
   (send-messages ctx scrape-fn exception-default-fn messages))
  ([ctx scrape-fn exception-fn messages]
   (timbre/debugf "Sending %d messages" (count messages))
   (cond
     (nil? ctx)
     (do
       (timbre/debugf "`ctx` is invalid: %s" ctx)
       (throw (ex-info "`ctx` is invalid" {:ctx ctx})))

     (nil? scrape-fn)
     (do
       (timbre/debugf "`scrape-fn` is invalid: %s" scrape-fn)
       (throw (ex-info "`scrape-fn` is invalid" {:scrape-fn scrape-fn})))

     (nil? messages)
     (do
       (timbre/debugf "`messages` is invalid: %s" messages)
       (throw (ex-info "`messages` is invalid" {:messages messages})))

     :else
     (->> messages
          (partition-all 500)
          (reduce
           (fn [results messages-batch]
             (into results (shm/send-batch-messages ctx messages-batch scrape-fn exception-fn)))
           [])))))

(defn stop
  "Stops shelob by closing the web drivers in the driver pool."
  []
  (timbre/debug "Stopping shelob")
  (shd/close-driver-pool @shd/driver-pool))

(defn reset
  "Stops shelob and restart it by initialising the driver pool."
  [ctx]
  (timbre/debug "Resetting shelob")
  (stop)
  (shd/init-driver-pool ctx))
