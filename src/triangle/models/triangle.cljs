(ns triangle.models.triangle
  (:require [triangle.models.line :as l]
            [triangle.models.point :as p]
            [triangle.util :as util]))

;----------------
; triangle model
;----------------
(declare valid-triangle?)

(defprotocol ITriangle
  (p1 [t])
  (p2 [t])
  (p3 [t])
  (valid? [t]))

(defrecord Triangle [p1 p2 p3 valid]
  ITriangle
  (p1 [t] p1)
  (p2 [t] p2)
  (p3 [t] p3)
  (valid? [t] valid))

(defn triangle [p1 p2 p3]
  {:pre [(every? #(satisfies? p/IPoint %) [p1 p2 p3])]}
  (->Triangle p1 p2 p3 (valid-triangle? p1 p2 p3)))

;----------------
; util functions
;----------------

(defn sides
  ([{:keys [p1 p2 p3]}]
   (sides p1 p2 p3))

  ([p1 p2 p3]
   [(l/line p1 p2)
    (l/line p1 p3)
    (l/line p2 p3)]))

(defn equal-sides
  "Returns the lines of the triangle that have the same length."
  [t margin]
  (let [equal-lines (some (fn [gl] (let [c (count (:lines gl))]
                              (when (< 1 c) (:lines gl)))) (l/group-lines margin (sides t)))]
    (if equal-lines equal-lines [])))

(defn valid-triangle?
  "Returns true when p1 p2 and p3 spans a valid triangle.
  A valid triangle is defined as three valid lines and
  a slope difference between the spanned lines."
  [p1 p2 p3]
  ; Points are sorted to ensure cronological order regarding
  ; first x and secondly y. Used to validate slope.
  (let [[p-one p-two p-three] (sort p/compare-by-xy [p1 p2 p3])]
    (and
      (every? l/valid? (sides p1 p2 p3))
      (not=
        (util/round-one-dec (l/slope p-one p-two))
        (util/round-one-dec (l/slope p-two p-three))))))

(defn equilateral?
  "True if all three sides have same length."
  [t same-length-sides]
  (and (valid? t) (= 3 (count same-length-sides))))

(defn isosceles?
  "True if two or more sides have same length."
  [t same-length-sides]
  (and (valid? t) (< 1 (count same-length-sides))))

(defn scalene?
  "True if no sides have same length."
  [t same-length-sides]
  (and (valid? t) (= 0 (count same-length-sides))))

