(ns triangle.state
  (:require [reagent.core :as r]))

(defonce app-state (r/atom nil))
(defonce history-state (r/atom nil))

(defn init-app-state [p1 p2 p3]
  (when-not @app-state
    (reset! app-state {:p1 p1 :p2 p2 :p3 p3})))

(defn init-history-state [p-handle]
  (when-not @history-state
    (reset! history-state {:handle p-handle
                           :history []})))

(defn record-state [_ _ _ s]
  (swap! history-state (fn [{:keys [history] :as coll}]
                         (assoc coll :history (conj history s)))))

(defn start-recording-history []
    (add-watch app-state :record record-state))

(defn stop-recording-history []
  (remove-watch app-state :record))

(add-watch app-state :record record-state)