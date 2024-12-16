(ns geo-v3.geo-v3
  (:require [geo-v3.core :as geo]
            [integrant.core :as ig])
  (:gen-class))

(def config
  {::geo/mmdb {:file-url "GeoLite2-City-Test.mmdb"}
   ::geo/locator {:mmdb (ig/ref ::geo/mmdb)}})

(comment

  (geo/lookup (::geo/locator (ig/init config)) "2.125.160.216")

  ,)

(defn greet
  "Callable entry point to the application."
  [data]
  (println (str "Hello, " (or (:name data) "World") "!")))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (greet {:name (first args)}))
