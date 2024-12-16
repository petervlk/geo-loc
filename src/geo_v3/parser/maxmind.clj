(ns geo-v3.parser.maxmind
  (:require
   [clojure.java.io :as io]
   [geo-v3.core :as c]))

(def maxmind-mmdb-file (io/file (io/resource "GeoLite2-City-Test.mmdb")))
(def maxmind-reader (c/mmdb-reader maxmind-mmdb-file))

(defn parse-data
  "Creates a parser for standard maxmind mmdb datbase. Records have a
  nested structure."
  [data]
  {:city         (get-in data ["city" "names" "en"])
   :country-code (get-in data ["country" "iso_code"])
   :region       (some-> data (get "subdivisions") first (get-in ["names" "en"]))
   :timezone     (get-in data ["location" "time_zone"])
   :latitude     (get-in data ["location" "latitude"])
   :longitude    (get-in data ["location" "longitude"])
   :postal-code  (get-in data ["postal" "code"])
   :geoname-id   (get-in data ["city" "geoname_id"])  ;; this one is optional
   })

(comment
  ;; missing IPs
  (c/lookup maxmind-reader "2.1.160.216")

  ;; hits
  (c/lookup maxmind-reader "2.125.160.216")

  ;; maxmind lookup data example
  {"continent"          {"code"       "EU"
                         "geoname_id" 6255148
                         "names"      {"de"    "Europa"
                                       "ru"    "Европа"
                                       "pt-BR" "Europa"
                                       "ja"    "ヨーロッパ"
                                       "en"    "Europe"
                                       "fr"    "Europe"
                                       "zh-CN" "欧洲"
                                       "es"    "Europa"}}
   "country"            {"iso_code"   "GB"
                         "geoname_id" 2635167
                         "names"      {"de"    "Vereinigtes Königreich"
                                       "ru"    "Великобритания"
                                       "pt-BR" "Reino Unido"
                                       "ja"    "イギリス"
                                       "en"    "United Kingdom"
                                       "fr"    "Royaume-Uni"
                                       "zh-CN" "英国"
                                       "es"    "Reino Unido"}}
   "city"               {"geoname_id" 2655045
                         "names"      {"en" "Boxford"}}
   "location"           {"accuracy_radius" 100
                         "time_zone"       "Europe/London"
                         "latitude"        51.75
                         "longitude"       -1.25}
   "postal"             {"code" "OX1"}
   "registered_country" {"is_in_european_union" true
                         "names"                {"de"    "Frankreich"
                                                 "ru"    "Франция"
                                                 "pt-BR" "França"
                                                 "ja"    "フランス共和国"
                                                 "en"    "France"
                                                 "fr"    "France"
                                                 "zh-CN" "法国"
                                                 "es"    "Francia"}
                         "iso_code"             "FR"
                         "geoname_id"           3017382}
   "subdivisions"       [{"iso_code"   "ENG"
                          "geoname_id" 6269131
                          "names"
                          {"en"    "England"
                           "fr"    "Angleterre"
                           "es"    "Inglaterra"
                           "pt-BR" "Inglaterra"}}
                         {"iso_code"   "WBK"
                          "geoname_id" 3333217
                          "names"
                          {"en"    "West Berkshire"
                           "zh-CN" "西伯克郡"
                           "ru"    "Западный Беркшир"}}]}

  (parse-data
    (c/lookup maxmind-reader "2.125.160.216"))

  ,)
