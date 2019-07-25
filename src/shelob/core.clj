(ns shelob.core
  (:require [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as appenders]))

(timbre/merge-config!
 {:appenders {:spit (appenders/spit-appender {:fname "shelob.log"})}})
