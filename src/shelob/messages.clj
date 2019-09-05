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

(ns shelob.messages
  "This namespace contains facilities to operate with messages.
  The entry point is `send-batch-messages`."
  (:require
   [clojure.core.async :as as]
   [shelob.browser :as shb]
   [shelob.scraper :as shs]
   [shelob.driver :as shd]
   [taoensso.timbre :as timbre]))

(defn process-messages
  "Processes `messages` on a given driver or on one from the driver pool.
  Every message is validated with `shelob.browser/validate` and processed with
  `shelob.browser/browser-command`. "
  ([messages]
   (doseq [driver @shd/driver-pool]
     (process-messages driver messages)))
  ([driver messages]
   (doseq [message messages]
     (timbre/debug message {})
     (->> (assoc message :driver driver)
          shb/validate
          shb/browser-command))))

(defn- driver-listener
  "Creates a command executor listener.
  The messages in `in-ch`, wrapped in a sequence, are processed to get to a page
  the source of which is placed in `out-ch`."
  [driver in-ch out-ch]
  (as/thread
    (loop []
      (when-let [messages (as/<!! in-ch)]
        (process-messages driver messages)
        (->> (.getPageSource driver)
             (as/>!! out-ch))
        (recur)))))

(defn- scraper-listener
  "Runs `scrape-fn` on the page source in `in-ch` in a separate thread."
  [in-ch scrape-fn]
  (as/thread
    (when-let [source (as/<!! in-ch)]
      (->> source
           shs/parse
           scrape-fn))))

(defn init-executors
  "Starts a thread for each instanced driver and returns a merged channel."
  [in-ch out-ch]
  (timbre/debug "Initialize executors..." {})
  (->> @shd/driver-pool
       (reduce (fn [acc driver] (conj acc (driver-listener driver in-ch out-ch))) [])
       as/merge))

(defn init-scrapers
  "Starts `n` scraper threads with `ctx` and returns a merged channel."
  [n in-ch scrape-fn]
  (timbre/debug "Initialize scrapers..." {})
  (-> n
      (repeatedly #(scraper-listener in-ch scrape-fn))
      as/merge))

(defn send-batch-messages
  "Sends `messages` to be processed with `scrape-fn`. `ctx` can contain the size
  of the pool, otherwise it is fixed to `shelob.driver/driver-pool-size`."
  [ctx messages scrape-fn]
  (let [msg-ch (as/chan (:pool-size ctx shd/driver-pool-size))
        scraper-ch (as/chan)
        result-ch (init-scrapers (count messages) scraper-ch scrape-fn)]
    (init-executors msg-ch scraper-ch)
    (as/onto-chan msg-ch messages)
    (let [results (as/<!! (as/reduce into [] result-ch))]
      (as/close! msg-ch)
      (as/close! scraper-ch)
      results)))
