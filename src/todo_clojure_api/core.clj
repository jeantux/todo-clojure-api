(ns todo-clojure-api.core
  (:gen-class)

    (:require [io.pedestal.http.route :as route]
              [io.pedestal.http :as http]
              [todo-clojure-api.database :as db]))

(defn assoc-store [ctx]
    (update ctx :request assoc :store db/store))

(def db-interceptor
  {:name :db-interceptor
   :enter assoc-store})
            
(defn get-index []
  {:status 200 :body (str "Welcome to todo-clojure-api")})

(defn new-task-map [uuid name status]
  {:id uuid :name name :status status})

(defn new-task [req]
  (let [uuid (java.util.UUID/randomUUID)
        name (get-in req [:query-params :name])
        status (get-in req [:query-params :status])
        task (new-task-map uuid name status)
        store (:store req)]
    
    (swap! store assoc uuid task)
    {:status 201 :body {:message "Task create with succes", :task task}}
  ))

(defn all-tasks [req]
  {:status 200 :body @(:store req)})

(def routes (route/expand-routes
          #{["/" :get get-index :route-name :index]
            ["/task" :post [db-interceptor new-task]  :route-name :new-task]
            ["/all-tasks" :get [db-interceptor all-tasks]  :route-name :all-tasks]}))

(def service-map {::http/routes routes
                  ::http/port   9999
                  ::http/type   :jetty
                  ::http/join?  :false})

(defonce server (atom nil))

(defn start-server [] 
    (reset! server (http/start (http/create-server service-map))))

(start-server)



