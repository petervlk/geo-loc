(ns geo-v3.main
  (:require
   [geo-v3.api :as geo-api]
   [geo-v3.core :as geo-core]
   [geo-v3.parser.ipinfo :as ipinfo]
   [geo-v3.parser.maxmind :as maxmind]
   [geo-v3.server :as geo-server]
   [integrant.core :as ig])
  (:gen-class))

(def config
  {::geo-core/mmdb {:file-url
                    "GeoLite2-City-Test.mmdb"
                    #_"ip_geolocation_standard_sample.mmdb"}
   ::geo-core/locator {:mmdb (ig/ref ::geo-core/mmdb)
                       :parser
                       #'maxmind/parse-record
                       #_ipinfo/parse-record}
   ::geo-api/api {:locator (ig/ref ::geo-core/locator)}
   ::geo-server/server {:api (ig/ref ::geo-api/api)
                        :opts {:port 6001}}})

(comment

  (def system (ig/init config))
  (ig/halt! system)

  ,)

(defn -main
  [& _]
  (ig/init config))
