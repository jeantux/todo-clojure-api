(ns todo-clojure-api.server
    (:require [io.pedestal.http :as http]
              [io.pedestal.interceptor :as interceptor]
              [todo-clojure-api.database :as db]
              [todo-clojure-api.routes :as routes]))

(defn assoc-store [ctx]
    (update ctx :request assoc :store db/store))
        
(def db-interceptor
    {:name :db-interceptor :enter assoc-store})

(def service-map-base {::http/routes routes/routes
    ::http/port   9000
    ::http/type   :jetty
    ::http/join?  :false})

(def service-map (-> service-map-base
    (http/default-interceptors)
    (update ::http/interceptors conj (interceptor/interceptor db-interceptor))))

(defonce server (atom nil))

(defn start-server []
    (reset! server (http/start (http/create-server service-map))))

(defn start []
    (start-server))
