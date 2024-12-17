(ns geo-v3.server
  (:require [org.httpkit.server :refer [run-server]]
            [integrant.core :as ig]
            [taoensso.timbre :as log]))

(defn start-server
  [api server-options]
  (let [server (run-server api server-options)]
    (log/info :server-started {:server-options server-options})
    server))

(defn stop-server
  [server]
  (server)
  (log/info :server-stopped))

(defmethod ig/init-key ::server [_ {:keys [api opts]}]
  (start-server api opts))

(defmethod ig/halt-key! ::server [_ server]
  (stop-server server))
