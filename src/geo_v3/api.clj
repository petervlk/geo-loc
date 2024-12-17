(ns geo-v3.api
  (:require [compojure.api.sweet :refer [api context GET]]
            [compojure.route :as route]
            [integrant.core :as ig]
            [geo-v3.spec :as spec]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.http-response :as r]))

(defn make-routes
  [locator]
  (api
    {:coercion :spec
     :swagger  {:ui   "/api-docs"
                :spec "/swagger.json"
                :data {:info {:title "LemonPI Geo Location V2"}
                       :tags [{:name "geo-location-v2"}]}}

     ;; :exceptions {:handlers (error/create-error-handler :geo-location-v2)}
     }

    (context "/" []
             (GET "/live" []
                  (r/ok "live"))

             (GET "/ready" []
                  (r/ok "ready")))

    (context "/api/v1/ph" []
             :tags ["ph"]
             (GET "/context-object" []
                  :query-params [ip-address :- ::spec/ip-address]
                  :summary "return context object using IP address"
                  (when-let [ctx-obj (locator ip-address)]
                    (r/ok ctx-obj))))

    (route/not-found "<h1>Page not found</h1>")))

(defmethod ig/init-key ::api [_ {:keys [locator]}]
  (-> (make-routes locator)
      (wrap-params)
      (wrap-json-response)))
