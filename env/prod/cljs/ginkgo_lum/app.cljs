(ns ginkgo-lum.app
  (:require [ginkgo-lum.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
