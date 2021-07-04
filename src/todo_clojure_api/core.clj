(ns todo-clojure-api.core
  (:gen-class)
  (:require [todo-clojure-api.server :as server]))

(server/start)
