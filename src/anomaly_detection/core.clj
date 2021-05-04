(ns anomaly-detection.core
  (:gen-class)
  (:require [ubergraph.core :as uber]
            (anomaly-detection [[alg :as alg]
                                [queries :as queries]
                                [reporting :as reporting]]))

;; Want Count-Min structures (for nodes and edges) and list of seen nodes and edges for each relation type
;; Transfer: based on the simulation data
;; Debit: based on the simulation data
;; CashIn: based on the simulation data
;; CashOut: based on the simulation data
;; Payment: based on the simulation data

;; Upon call to API, will insert into count-mins and then the database.


(defn relation-map
  [idFrom idTo weight time relation-type]
  (assoc {} :idFrom idFrom :idTo idTo :weight weight :time time :relation relation-type))

(defn update-counters
  [cm-edges cm-nodes edge-map]
  (let [first-node (:idFrom edge-map)
        second-node (:idTo edge-map)
        weight (:weight edge-map)
        new-cm-edges (alg/weighted-insert cm-edges edge-map weight)
        new-cm-nodes (alg/weighted-insert (alg/weighted-insert cm-nodes first-node weight) second-node weight)]
    [new-cm-edges new-cm-nodes edge-map [first-node second-node]]))