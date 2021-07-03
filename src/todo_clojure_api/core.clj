(ns todo-clojure-api.core
  (:gen-class)

    (:require [io.pedestal.http.route :as route]
              [io.pedestal.http :as http]
              [todo-clojure-api.database :as db]
              [io.pedestal.interceptor :as interceptor]))

(defn assoc-store [ctx]
    (update ctx :request assoc :store db/store))

(def db-interceptor
  {:name :db-interceptor
   :enter assoc-store})
            
(defn get-index [req]
  {:status 201 :body (str "Welcome to todo-clojure-api")})

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

(defn delete-task [req]
  (let [store (:store req)
        uuid (get-in req [:path-params :id])
        uuid-string (java.util.UUID/fromString uuid)
       ]
  (swap! store dissoc uuid-string)
  {:status 200 :body {:message "Task removed with success"}}))

  (defn update-task [req]
    (let [uuid (get-in req [:path-params :id])
          id-string (java.util.UUID/fromString uuid)
          name (get-in req [:query-params :name])
          status (get-in req [:query-params :status])
          task (new-task-map uuid name status)
          store (:store req)]
      
      (swap! store assoc id-string task)
      {:status 201 :body {:message "Task updated with succes", :task task}}
    ))

(defn all-tasks [req]
  {:status 200 :body @(:store req)})

(def routes (route/expand-routes
          #{["/" :get get-index :route-name :index]
            ["/all-tasks" :get all-tasks :route-name :all-tasks]
            ["/task" :post new-task :route-name :new-task]
            ["/task/:id" :delete delete-task :route-name :delete-task]
            ["/task/:id" :patch update-task :route-name :update-task]}))

(def service-map-base {::http/routes routes
                  ::http/port   9000
                  ::http/type   :jetty
                  ::http/join?  :false})

(def service-map (-> service-map-base
                     (http/default-interceptors)
                     (update ::http/interceptors conj (interceptor/interceptor db-interceptor))))

(defonce server (atom nil))

(defn start-server [] 
    (reset! server (http/start (http/create-server service-map))))

(start-server)



