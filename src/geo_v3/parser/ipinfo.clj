(ns geo-v3.parser.ipinfo
  (:require
   [clojure.java.io :as io]
   [geo-v3.core :as c]))

(def ipinfo-mmdb-file (io/file (io/resource "ip_geolocation_standard_sample.mmdb")))
(def ipinfo-reader (c/mmdb-reader ipinfo-mmdb-file))

;; TODO -- use malli for coercion
(defn as-float [float-or-string]
  (cond
    (float? float-or-string) float-or-string
    (string? float-or-string) (Float/parseFloat float-or-string)))

;; TODO -- use malli for coercion
(defn as-int [int-or-string]
  (cond
    (int? int-or-string) int-or-string
    (string? int-or-string) (Integer/parseInt int-or-string)))

(defn parse-data
  [data]
  {:city         (get data "city")
   :country-code (get data "country")
   :region       (get data "region")
   :timezone     (get data "time_zone")
   :latitude     (as-float (get data "lat"))
   :longitude    (as-float (get data "lng"))
   :postal-code  (get data "postal_code")
   :geoname-id   (as-int (get data "geoname_id")) ;; this one is optional
   })

(comment
  ;; missing IPs
  (c/lookup ipinfo-reader "201.20.8.231")

  ;; hits
  (parse-data
    (c/lookup ipinfo-reader "201.20.83.231"))

  (c/lookup ipinfo-reader "201.20.83.231")

  ;; => {:timezone "America/Fortaleza",
  ;;     :city "Fortaleza",
  ;;     :postal-code "60000-000",
  ;;     :region "Ceará",
  ;;     :geoname-id "3399415",
  ;;     :lat "-3.71722",
  ;;     :country "BR",
  ;;     :lng "-38.54306",
  ;;     :region-code "CE"}

  ,)
