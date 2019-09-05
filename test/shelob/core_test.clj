(ns shelob.core-test
  (:require
   [clojure.test :refer [deftest is]]
   [shelob.core :as sh]
   [shelob.driver :as shd]
   [shelob.messages :as shm]
   [taoensso.timbre :as timbre]))

(deftest init-test
  (with-redefs [timbre/debug (fn [& _])
                sh/init-log (fn [_])
                shd/init-driver (fn [_])
                shm/process-messages (fn [_])]
    (is (nil? (sh/init {:driver-options {:browser :firefox}})))
    (is (thrown? Exception (sh/init {})))))

(deftest send-message-test
  (with-redefs [timbre/debug (fn [& _])
                shm/send-batch-messages (fn [_ _ _])]
    (is (nil? (sh/send-message {:driver-options {:browser :firefox}} identity {})))
    (is (thrown? Exception (sh/send-message nil identity {})))
    (is (thrown? Exception
                 (sh/send-message {:driver-options {:browser :firefox}} nil {})))
    (is (thrown? Exception
                 (sh/send-message {:driver-options {:browser :firefox}} identity nil)))))

(deftest send-messages-test
  (with-redefs [timbre/debug (fn [& _])
                shm/send-batch-messages (fn [_ _ _])]
    (is (empty? (sh/send-messages {:driver-options {:browser :firefox}} identity [{}])))
    (is (thrown? Exception (sh/send-messages nil identity [{}])))
    (is (thrown? Exception
                 (sh/send-messages {:driver-options {:browser :firefox}} nil [{}])))
    (is (thrown? Exception
                 (sh/send-messages {:driver-options {:browser :firefox}} identity nil)))))
