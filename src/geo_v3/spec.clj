(ns geo-v3.spec
  (:require [clojure.edn :as edn]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]))

;; TODO -- add ipv6 support
(s/def ::ip-address
  (letfn [(pred
            [s]
            (let [parts (string/split s #"\.")]
              (and (= (count parts) 4)
                   (every? (fn [part]
                             (try
                               (let [n (edn/read-string part)]
                                 (and (integer? n)
                                      (>= 256 n 0)))
                               (catch Exception _ false)))
                           parts))))]
    (s/spec pred)))
