(ns todo-clojure-api.routes
    (:require [io.pedestal.http.route :as route]))

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
