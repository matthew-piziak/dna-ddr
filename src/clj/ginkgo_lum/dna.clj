(ns ginkgo-lum.dna
  (:require [ginkgo-lum.utils :as u]
            [clojure.string :as s]
            [instaparse.core :as p]
            [clj-fasta.core :as fa]
            [clojure.java.io :as io]
            [com.dean.interval-tree.core :as dean]))

(defn loc [desc]
  ;; check complements
  ;; [location=complement(join(66178..66227,66628..67120))]
  (let [[_ a b] (or
                 (re-find #"location=complement\((\d+)..(\d+)\)" desc)
                 (re-find #"location=(\d+)..(\d+)" desc)
                 (re-find #"location=join\((\d+)..(\d+),\d+..\d+\)" desc)
                 (re-find #"location=complement\(join\((\d+)..(\d+),\d+..\d+\)\)" desc))]
    [(Long/parseLong a) (Long/parseLong b)]))

(defn as-interval [l]
  [(loc (l :description)) (l :description)])

(defn genome [f name]
    {:name name
     :genome (s/join (u/read-lines (str "genomes/" f)))
     :prots (dean/interval-map
             (map as-interval
                  (let [r (io/reader (io/resource (str "genomes/" f "_prot")))]
                    (fa/fasta-seq r))))})
(def genomes
  [(genome "NC_000852" "PBCV-1")
   (genome "NC_007346" "EhV-86")
   (genome "NC_008724" "ATCV-1")
   (genome "NC_009899" "PBCV-AR158")
   (genome "NC_016072" "Megavirus") ; NCBI redirect from NC_023740
   (genome "NC_020104" "APMV")
   (genome "NC_023423" "Pithovirus")
   (genome "NC_023719" "Phage-G")
   (genome "NC_027867" "Mollivirus")
   (genome "NC_014637" "BV-PW1")])

;;; remember to handle complements
;;; handle overlapping
;;; will need tests

(defn query-locs [s]
  (remove nil?
   (for [g genomes]
     (if (s/includes? (g :genome) s)
       (let [idx (s/index-of (g :genome) s)]
         (str "[" (g :name) "] " (apply str ((g :prots) [idx (+ idx (count s))]))))))))

;;; return all results
(defn resp [s]
  (let [results (query-locs s)]
    (if (empty? results) "[DNA NOT FOUND]"
        results)))
