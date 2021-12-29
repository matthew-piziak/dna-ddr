(ns ginkgo-lum.dna
  (:require [ginkgo-lum.utils :as u]
            [clojure.string :as s]
            [instaparse.core :as p]
            [clj-fasta.core :as fa]
            [clojure.java.io :as io]
            [com.dean.interval-tree.core :as dean]))

(def nc000852 (s/join (u/read-lines "genomes/NC_000852")))

(s/index-of nc000852 "TTGAACCGATACATTTGCATTTCCTACAAGAGAACC")

;;; remember to handle complements
;;; handle overlapping

(def prot000852
  (let [r (io/reader (io/resource "genomes/NC_000852_prot"))] (fa/fasta-seq r)))

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

(def interval-map (dean/interval-map (map as-interval prot000852)))

;;; will need tests

(defn query-locs [s]
  (let [idx (s/index-of nc000852 s)]
    [idx (+ idx (count s))]))

(defn resp [s]
  (interval-map (query-locs s)))

;; (interval-map (query-locs "CGCTACCTTT"))

;; (time (interval-map (query-locs "ATCAAATATTAACGGAAACGTAAGAAGTTCTACCGCAAATGTTCAGGGTAGATTATCAGCAACAACCCCG")))
