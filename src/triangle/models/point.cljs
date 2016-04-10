(ns triangle.models.point)

;------------
; line model
;------------

(defprotocol IPoint
  (x [p])
  (y [p]))

(defrecord Point [x-coord y-coord]
  IPoint
  (x [_] x-coord)
  (y [_] y-coord))

(defn point [x y]
  {:pre [(every? number? [x y])]}
  (->Point x y))

;----------------
; util functions
;----------------

(defn compare-by-xy
  "Comparator used to sort points by firstly x and secondly y"
  [p1 p2]
  (compare [(x p1) (y p1)]
           [(x p2) (y p2)]))
