(ns triangle.util)


(defn round-one-dec [n]
  (/ (js/Math.round (* n 10)) 10))
