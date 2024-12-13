(ns geo-v3.core
  (:require
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]
   [clojure.java.io :as io]
   [clojure.java.data :as j]
   [taoensso.timbre :as log])
  (:import
   (com.maxmind.db
     CHMCache
     InvalidDatabaseException
     Reader)
   (java.net
     InetAddress)))

(defn mmdb-reader
  [mmdb-file]
  (try (new Reader mmdb-file (new CHMCache))
       (catch InvalidDatabaseException e
         (log/error e :invalid-file "Did not retrieve a valid .mmdb file.")
         (throw e))))

(defn lookup [mmdb-reader ip]
  (or
    (.get mmdb-reader (InetAddress/getByName ip) java.util.Map)
    (log/warn :mmdb-lookup-failed {:ip ip})))

(defn java-map->clojure-map
  [^java.util.Map java-map]
  (->> java-map
      (into {})
      (cske/transform-keys csk/->kebab-case-keyword)))

(def ipinfo-mmdb-file (io/file (io/resource "ip_geolocation_standard_sample.mmdb")))
(def maxmind-mmdb-file (io/file (io/resource "GeoLite2-City-Test.mmdb")))
(def ipinfo-reader (mmdb-reader ipinfo-mmdb-file))
(def maxmind-reader (mmdb-reader maxmind-mmdb-file))

(comment

  (.hasNext (.networks maxmind-reader java.util.Map))

  ,)

(comment
  ;; missing IPs
  (lookup maxmind-reader "2.1.160.216")
  (lookup ipinfo-reader "201.20.8.231")

  ;; hits
  (java-map->clojure-map
    (lookup ipinfo-reader "201.20.83.231"))

  (java-map->clojure-map
    (lookup maxmind-reader "2.125.160.216"))

  ,)
