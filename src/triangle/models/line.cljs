(ns triangle.models.line
  (:require [triangle.models.point :as p]))

;------------
; line model
;------------

(defprotocol ILine
  (p1 [l])
  (p2 [l]))

(defrecord Line [p1 p2]
  ILine
  (p1 [l] p1)
  (p2 [l] p2))

(defn line [p1 p2]
  {:pre [(every? #(satisfies? p/IPoint %) [p1 p2])]}
  (->Line p1 p2))

;----------------
; util functions
;----------------

(defn dist
  ([l]
   (dist (p1 l) (p2 l)))

  ([p1 p2]
   (js/Math.sqrt (+ (js/Math.pow (- (p/x p2) (p/x p1)) 2)
                    (js/Math.pow (- (p/y p2) (p/y p1)) 2)))))

(defn- add-line-to-group [margin grouped-lines l]
  (let [d (dist l)
        ;the index where d is between min and max
        index (some (fn [i] (let [{:keys [d-max d-min]} (nth grouped-lines i)]
                              (when (and (<= d d-max)
                                         (>= d d-min)) i))) (range (count grouped-lines)))]

    (if index
      (update-in grouped-lines [index :lines] conj l)
      ;add a new group
      (conj grouped-lines {:d-min (- d margin)
                           :d-max (+ d margin)
                           :lines [l]}))))

(defn group-lines
  "Takes a sequence of lines and returns them grouped by length.
  An optional accuracy-margin can be given to make lines group more easily.

  ex of returned data structure created with accuracy-margin of 5 and three lines.
  [{:lines [l1 l2]
    :d-min 100
    :d-max 110}
    {:lines [l3]
    :d-min 177
    :d-max 187}]"
  ([lines]
   (group-lines 0 lines))

  ([accuracy-margin lines]
   (reduce (fn [g l] (add-line-to-group accuracy-margin g l)) [] lines)))

(defn slope
  ([l]
   (slope (p1 l) (p2 l)))

  ([p1 p2]
   (/ (- (p/x p2) (p/x p1))
      (- (p/y p2) (p/y p1)))))

(defn valid? [l]
  (not= 0 (dist l)))
