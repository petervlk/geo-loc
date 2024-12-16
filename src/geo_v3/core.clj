(ns geo-v3.core
  (:require
   [clojure.java.io :as io]
   [integrant.core :as ig]
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
    (try
      (.get mmdb-reader (InetAddress/getByName ip) java.util.Map)
      (catch Exception e
        (log/error e :mmdb-lookup-failed)))
    (log/warn :mmdb-lookup-failed {:ip ip})))

(defmethod ig/init-key ::mmdb [_ {:keys [file-url]}]
  (io/file (io/resource file-url)))

(defmethod ig/init-key ::locator [_ {:keys [mmdb]}]
  (mmdb-reader mmdb))


(comment

  (require '[clojure.java.data :as j])

  (def ipinfo-reader (-> "ip_geolocation_standard_sample.mmdb"
                         io/resource
                         io/file
                         mmdb-reader))

  (def maxmind-reader (-> "GeoLite2-City-Test.mmdb"
                          io/resource
                          io/file
                          mmdb-reader))

  ;; missing IPs
  (lookup maxmind-reader "2.1.160.216")
  (lookup ipinfo-reader "201.20.8.231")

  ;; hits
  (lookup maxmind-reader "2.125.160.216")

  ;; attempt to iterate maxmind db records
  (.hasNext (.networks maxmind-reader java.util.Map))

  ,)
