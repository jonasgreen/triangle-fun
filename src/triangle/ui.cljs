(ns triangle.ui
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [goog.events :as events]
            [triangle.models.point :refer [x y point]]
            [triangle.models.triangle :as t-m]
            [triangle.state :as state])
  (:import [goog.events EventType]))

(def accuracy-margin 5)

(def text-color "rgba(255,255,255,0.6)")

(def text-style
  {:-webkit-user-select "none"
   :-moz-user-select    "none"
   :color               text-color
   :font-size           12})

(def x-y-label-style
  (merge text-style {:font-size 10
                     :position  :absolute}))

(def x-slider-start 30)
(def x-slider-end 500)
(def y-slider 50)
(def p-slider-start (point x-slider-start y-slider))
(def p-slider-end (point x-slider-end y-slider))

(declare line)

;-----------------
; control actions
;-----------------

(defn drag-move-handler [on-drag]
  (fn [evt]
    (on-drag (.-clientX evt) (.-clientY evt))))

(defn drag-end-handler [drag-move drag-end on-end]
  (fn [evt]
    (events/unlisten js/window EventType.MOUSEMOVE drag-move)
    (events/unlisten js/window EventType.MOUSEUP @drag-end)
    (on-end)))

(defn dragging [{:keys [on-drag on-start on-end]
                 :or   {on-start (fn []) on-end (fn [])}}]
  (let [drag-move (drag-move-handler on-drag)
        drag-end-atom (atom nil)
        drag-end (drag-end-handler drag-move drag-end-atom on-end)]
    (on-start)
    (reset! drag-end-atom drag-end)
    (events/listen js/window EventType.MOUSEMOVE drag-move)
    (events/listen js/window EventType.MOUSEUP drag-end)))

(defn move-point [p]
  (fn [x y]
    (swap! state/app-state assoc p (point x y))))

(defn move-slider [p]
  (fn [x y]
    (let [new-x (-> x
                    (min x-slider-end)
                    (max x-slider-start))
          position (/ (- new-x x-slider-start)
                      (- x-slider-end x-slider-start))
          history (:history @state/history-state)]
      (swap! state/history-state assoc p (point new-x y-slider))
      (reset! state/app-state (nth history (int (* (dec (count history)) position)))))))

;-------------
; ui-elements
;-------------

(defn svg-line [p1 p2 color]
  [:line {:stroke color :stroke-width 0.5 :x1 (x p1) :y1 (y p1) :x2 (x p2) :y2 (y p2)}])

(defn svg-triangle [{:keys [p1 p2 p3]}]
  [:polygon {:fill "rgba(101,101,101,0.7)" :points (str (x p1) "," (y p1) " " (x p2) "," (y p2) " " (x p3) "," (y p3))}])

(defn intersecters [{:keys [p1 p2 p3]}]
  [:g (map-indexed (fn [i p]
                     ^{:key i} [:g
                                [svg-line (point 0 (y p)) (point (+ (x p) 4000) (y p)) "white"]
                                [svg-line (point (x p) 0) (point (x p) (+ (y p) 4000)) "white"]]) [p1 p2 p3])])

(defn handle [{:keys [on-drag color]} p]
  [:circle {:style         {:cursor :pointer}
            :stroke        "none"
            :r             10
            :fill          color
            :on-mouse-down #(dragging {:on-drag on-drag})
            :cx            (x p)
            :cy            (y p)}])

(defn svg-handle-layer [{:keys [p1 p2 p3] :as t}]
  [:g
   [intersecters t]

   [handle {:on-drag (move-point :p1) :color "red"} p1]
   [handle {:on-drag (move-point :p2) :color "blue"} p2]
   [handle {:on-drag (move-point :p3) :color "yellow"} p3]])

(defn slider-handle [p]
  [:g
   [:text {:fill  text-color
           :style (merge text-style {:font-size 14})
           :x     (+ 3 (x p))
           :y     (+ (y p) 5)}
    "H"]
   [:rect {:style         {:cursor :pointer}
           :x             (x p)
           :y             (- (y p) 10)
           :stroke        "white"
           :stroke-width  0.5
           :fill          "rgba(0,0,0,0)"
           :width         16
           :height        20
           :on-mouse-down #(dragging {:on-drag  (move-slider :handle)
                                      :on-start state/stop-recording-history
                                      :on-end   state/start-recording-history})}]])

(defn svg-history-slider []
  (fn []
    (let [p-handle @(reaction (:handle @state/history-state))]
      [:g
       [svg-line p-slider-start p-slider-end "rgba(255,255,255, 0.3)"]
       [slider-handle p-handle]])))

(defn explanation-text [on? x y label-text]
  [:text {:fill      (if on? "white" text-color)
          :style     text-style
          :x         x
          :y         y
          :font-size 14} label-text])

(defn explanation-line [{:keys [p1 p2]}]
  [:line {:stroke "green" :stroke-width 2 :x1 (x p1) :y1 (y p1) :x2 (x p2) :y2 (y p2)}])

(defn svg-explanation-layer [t]
  (let [equal-sides (t-m/equal-sides t accuracy-margin)]
    [:g
     [explanation-text (t-m/equilateral? t equal-sides) 555 54 "equilateral"]
     [explanation-text (t-m/isosceles? t equal-sides) 638 54 "isosceles"]
     [explanation-text (t-m/scalene? t equal-sides) 717 54 "scalene"]

     ;explanation sides in green
     (when-not (t-m/scalene? t equal-sides)
       (map-indexed (fn [i l] ^{:key i} [explanation-line l]) equal-sides))]))


(defn x-labels [points]
  [:div
   (map-indexed (fn [i p] ^{:key i} [:div {:style (merge x-y-label-style {:left (+ (x p) 4) :bottom 2})} (x p)]) points)])

(defn y-labels [points]
  [:div
   (map-indexed (fn [i p] ^{:key i} [:div {:style (merge x-y-label-style {:left 4 :top (+ (y p) 4)})} (y p)]) points)])

(defn render-app []
  (fn []
    (let [{:keys [p1 p2 p3]} @(reaction @state/app-state)
          t (t-m/triangle p1 p2 p3)]

      [:div {:style {:width "100vw" :height "100vh"}}

       ;svg
       [:svg {:style {:background "black"} :width "100%" :height "100%"}
        [svg-triangle t]
        [svg-explanation-layer t]
        [svg-handle-layer t]
        [svg-history-slider]]

       [x-labels [p1 p2 p3]]
       [y-labels [p1 p2 p3]]])))
