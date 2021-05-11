(ns anomaly-detection.core
  (:gen-class)
  (:require [ubergraph.core :as uber]
            (bigml.sketchy [count-min :as count-min])
            [clojure.string :as str]
            [clojure.set :as set]
            [clojure.data.json :as json]
            [anomaly-detection.alg :as alg]
            ;;[anomaly-detection.queries :as queries]
            [anomaly-detection.reporting :as reporting]))
  
;; Time Management
(def time-interval 1)
(def time-period (atom 1))

;; Counters
(def num-hash-bits 9)
(def cm-transfer-edge (atom (count-min/create :hash-bits num-hash-bits)))
(def cm-transfer-node (atom (count-min/create :hash-bits num-hash-bits)))
(def cm-debit-edge (atom (count-min/create :hash-bits num-hash-bits)))
(def cm-debit-node (atom (count-min/create :hash-bits num-hash-bits)))
(def cm-cash-in-edge (atom (count-min/create :hash-bits num-hash-bits)))
(def cm-cash-in-node (atom (count-min/create :hash-bits num-hash-bits)))
(def cm-cash-out-edge (atom (count-min/create :hash-bits num-hash-bits)))
(def cm-cash-out-node (atom (count-min/create :hash-bits num-hash-bits)))
(def cm-payment-edge (atom (count-min/create :hash-bits num-hash-bits)))
(def cm-payment-node (atom (count-min/create :hash-bits num-hash-bits)))

(def prev-cm-transfer-edge (atom (count-min/create :hash-bits num-hash-bits)))
(def prev-cm-transfer-node (atom (count-min/create :hash-bits num-hash-bits)))
(def prev-cm-debit-edge (atom (count-min/create :hash-bits num-hash-bits)))
(def prev-cm-debit-node (atom (count-min/create :hash-bits num-hash-bits)))
(def prev-cm-cash-in-edge (atom (count-min/create :hash-bits num-hash-bits)))
(def prev-cm-cash-in-node (atom (count-min/create :hash-bits num-hash-bits)))
(def prev-cm-cash-out-edge (atom (count-min/create :hash-bits num-hash-bits)))
(def prev-cm-cash-out-node (atom (count-min/create :hash-bits num-hash-bits)))
(def prev-cm-payment-edge (atom (count-min/create :hash-bits num-hash-bits)))
(def prev-cm-payment-node (atom (count-min/create :hash-bits num-hash-bits)))

(def seen-transfer-edge (atom #{}))
(def seen-transfer-node (atom #{}))
(def seen-debit-edge (atom #{}))
(def seen-debit-node (atom #{}))
(def seen-cash-in-edge (atom #{}))
(def seen-cash-in-node (atom #{}))
(def seen-cash-out-edge (atom #{}))
(def seen-cash-out-node (atom #{}))
(def seen-payment-edge (atom #{}))
(def seen-payment-node (atom #{}))

(def prev-cm-list [prev-cm-transfer-edge prev-cm-transfer-node prev-cm-debit-edge
                   prev-cm-debit-node prev-cm-cash-in-edge prev-cm-cash-in-node
                   prev-cm-cash-out-edge prev-cm-cash-out-node
                   prev-cm-payment-edge prev-cm-payment-node])

(def cm-list [cm-transfer-edge cm-transfer-node cm-debit-edge cm-debit-node
              cm-cash-in-edge cm-cash-in-node cm-cash-out-edge cm-cash-out-node
              cm-payment-edge cm-payment-node])

(def seen-list [seen-transfer-edge seen-transfer-node seen-debit-edge seen-debit-node
                seen-cash-in-edge seen-cash-in-node seen-cash-out-edge seen-cash-out-node
                seen-payment-edge seen-payment-node])

(defn update-counters
  [cm-edges cm-nodes seen-edges seen-nodes edge-map]
  (let [first-node (:idFrom edge-map)
        second-node (:idTo edge-map)
        weight (:weight edge-map)]
    (swap! cm-edges alg/weighted-insert edge-map weight)
    (swap! cm-nodes alg/weighted-insert first-node weight)
    (swap! cm-nodes alg/weighted-insert second-node weight)
    (swap! seen-edges conj edge-map)
    (swap! seen-nodes conj first-node second-node)))

(defn new-time-period?
  "Checks if still in the same time period"
  [t]
  (not (= t @time-period)))

(defn increase-increment
  "If not in the same time period, handle switch to new time period"
  ; check for concerns and output to file
  []
  (reporting/report
   (apply set/union (for [i (range (count cm-list))]
                      (alg/get-concerns @(nth prev-cm-list i) @(nth cm-list i) @(nth seen-list i))))
   (str "./out/flagged" @time-period ".json"))

  ; increment the time-period
  (swap! time-period inc)

  ; dampen and update history and create new current sketches
  (pmap
   (fn [i] (swap! (get i prev-cm-list) (comp (fn [x] (count-min/merge x @(get i cm-list))) alg/count-min-dampen)))
   (range (count cm-list)))

  (pmap
   (fn [i] (swap! (get i cm-list) count-min/create :hash-bits num-hash-bits))
   (range (count cm-list)))

  (pmap
   (fn [i] (swap! (get i seen-list) empty))
   (range (count cm-list)))
  )

(defn parse-line 
  "read in a line from the CSV file and parse it into a map
   will have idFrom, idTo, weight, relation, and timePeriod"
  ; file in format: step,type,amount,nameOrig,oldbalanceOrg,newbalanceOrig,nameDest,oldbalanceDest,newbalanceDest,isFraud,isFlaggedFraud
  [line]
  (let [fields (str/split line #",")]
    {:idFrom (nth fields 3)
     :idTo (nth fields 6)
     :weight (Double/parseDouble (nth fields 2))
     :timePeriod (Integer/parseInt (nth fields 0))
     :relation (nth fields 1)
     :isFraud (= (Integer/parseInt (nth fields 9)) 1)}))

(defn -main
  "Main point of entry, want to read each line of the file
   parse the line, and then update our state/counters"
  []
  (with-open [rdr (clojure.java.io/reader "./data/paysim_log_no_header.csv")]
    (doseq [line (line-seq rdr)]
      (let [edge-map (parse-line line)
            index-edge (case (:relation edge-map)
                      "TRANSFER" 0
                      "DEBIT" 2
                      "CASH_IN" 4
                      "CASH_OUT" 6
                      "PAYMENT" 8)
            index-node (inc index-edge)]
        (while (new-time-period? (:timePeriod edge-map))
              (increase-increment))
        (update-counters
         (get cm-list index-edge)
         (get cm-list index-node)
         (get seen-list index-edge)
         (get seen-list index-node)
         edge-map)))))