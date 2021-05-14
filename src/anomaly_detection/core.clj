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
  
(def log-filename "./out/log.csv")

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
(def seen-debit-edge (atom #{}))
(def seen-cash-in-edge (atom #{}))
(def seen-cash-out-edge (atom #{}))
(def seen-payment-edge (atom #{}))

(def prev-cm-list [prev-cm-transfer-edge prev-cm-transfer-node prev-cm-debit-edge
                   prev-cm-debit-node prev-cm-cash-in-edge prev-cm-cash-in-node
                   prev-cm-cash-out-edge prev-cm-cash-out-node
                   prev-cm-payment-edge prev-cm-payment-node])

(def cm-list [cm-transfer-edge cm-transfer-node cm-debit-edge cm-debit-node
              cm-cash-in-edge cm-cash-in-node cm-cash-out-edge cm-cash-out-node
              cm-payment-edge cm-payment-node])

(def seen-list [seen-transfer-edge seen-debit-edge seen-cash-in-edge
                seen-cash-out-edge seen-payment-edge])

(defn update-counters
  [cm-edges cm-nodes seen-edges edge-map]
  (let [first-node (:idFrom edge-map)
        second-node (:idTo edge-map)
        weight (:weight edge-map)]
    (swap! cm-edges alg/weighted-insert (alg/limit-edge edge-map) weight)
    (swap! cm-nodes alg/weighted-insert first-node weight)
    (swap! cm-nodes alg/weighted-insert second-node weight)
    (swap! seen-edges conj edge-map)))

(defn flush-concerns  
  []
  (let [flush-lists (into-array (for [i (range (count seen-list))] (first (swap-vals! (nth seen-list i) empty))))
        concerns-set (set (into-array (apply set/union (doall (for [i (map (fn [x] (* 2 x)) (range (/ (count cm-list) 2)))]
                                                   (alg/get-concerns
                                                    @(nth prev-cm-list i)
                                                    @(nth cm-list i)
                                                    @(nth prev-cm-list (+ i 1))
                                                    @(nth cm-list (+ i 1))
                                                    (nth flush-lists (/ i 2))))))))]
    (reporting/report-csv concerns-set log-filename true)
    (reporting/report-csv (set/difference (into-array (reduce set/union flush-lists)) concerns-set) log-filename false)))
  
(defn new-time-period?
  "Checks if still in the same time period"
  [t]
  (>= (- t @time-period) time-interval))

(defn increase-increment
  "If not in the same time period, handle switch to new time period"
  ; check for concerns and output to file
  []
  ; increment the time-period
  (swap! time-period + time-interval)

  ; dampen and update history and create new current sketches  
  (doseq [i (range (count cm-list))] (swap! (nth prev-cm-list i) (comp (fn [x] (count-min/merge x @(nth cm-list i))) alg/count-min-dampen)))
  (doseq [i (range (count cm-list))] (swap! (nth cm-list i) (fn [x] (count-min/create :hash-bits num-hash-bits)))))

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
  (reporting/start-report-csv log-filename)
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
        (do (while (new-time-period? (:timePeriod edge-map))
              (increase-increment))
            (update-counters
             (nth cm-list index-edge)
             (nth cm-list index-node)
             (nth seen-list (int (/ index-edge 2)))
             edge-map)
            (flush-concerns))))))