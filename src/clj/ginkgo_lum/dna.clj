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

(def nc000852
  {:name "PBCV-1"
   :genome (s/join (u/read-lines "genomes/NC_000852"))
   :prots (dean/interval-map (map as-interval (let [r (io/reader (io/resource "genomes/NC_000852_prot"))] (fa/fasta-seq r))))})
(def nc007346
  {:name "EhV-86"
   :genome (s/join (u/read-lines "genomes/NC_007346"))
   :prots (dean/interval-map (map as-interval (let [r (io/reader (io/resource "genomes/NC_007346_prot"))] (fa/fasta-seq r))))})

(def genomes [nc000852 nc007346])

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

(resp "TATTAT")

;; (interval-map (query-locs "CGCTACCTTT"))

;; (time (interval-map (query-locs "ATCAAATATTAACGGAAACGTAAGAAGTTCTACCGCAAATGTTCAGGGTAGATTATCAGCAACAACCCCG")))
