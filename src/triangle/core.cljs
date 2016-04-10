(ns triangle.core
  (:require
    [reagent.core :as r]
    [triangle.state :as state]
    [triangle.ui :as ui]
    [triangle.models.point :as p]))

(enable-console-print!)

;hot-swap support
(defn on-js-reload []
  (swap! state/app-state update :js-reloads inc))

;application entry point
(defn main []

  ;init state
  (state/init-history-state ui/p-slider-end)
  (state/init-app-state (p/point 100 140) (p/point 180 375) (p/point 382 315))

  ;render app
  (r/render [ui/render-app] (js/document.getElementById "app")))