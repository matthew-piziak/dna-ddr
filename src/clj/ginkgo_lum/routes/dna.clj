(ns ginkgo-lum.routes.dna
    (:require
     [ginkgo-lum.layout :as layout]
     [clojure.java.io :as io]
     [ginkgo-lum.middleware :as middleware]
     [ring.util.response]
     [ring.util.http-response :as response]))

(defn dna-page [request]
  (layout/render request "dna.html"))

(defn dna-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get dna-page}]])
