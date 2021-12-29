(ns ginkgo-lum.utils
  (:require [clojure.java.io :as io]))

(defn read-lines [i]
  (line-seq (io/reader (io/resource i))))
