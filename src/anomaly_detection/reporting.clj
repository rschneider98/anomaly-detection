(ns anomaly-detection.reporting
  "Functions to store out metric calculations and records"
  (:gen-class)
  (:require [clojure.data.json :as json]
            [clojure.string :as str]))

(defn report-json
  "Output concerns to a file, takes collection of maps and filename, saves as CSV
   (report concerns filename)"
  [flagged filename]
  (spit filename (json/write-str flagged)))

(defn start-report-csv
  [filename] 
  (spit filename "timePeriod,idFrom,idTo,relation,weight,isFraud,isFlaggedFraud\n"))

(defn unpack
  "file in format: step,type,amount,nameOrig,oldbalanceOrg,newbalanceOrig,nameDest,oldbalanceDest,newbalanceDest,isFraud,isFlaggedFraud
   :idFrom (nth fields 3)
   :idTo (nth fields 6)
   :weight (Double/parseDouble (nth fields 2))
   :timePeriod (Integer/parseInt (nth fields 0))
   :relation (nth fields 1)
   :isFraud (= (Integer/parseInt (nth fields 9)) 1)
   Want to output as a string: timePeriod, idFrom, idTo, relation, weight, isFraud"
  [observation isFlagged]
  (let [{:keys [idFrom idTo weight timePeriod relation isFraud]} observation]
    (str (str/join "," (map str [timePeriod idFrom idTo relation weight isFraud isFlagged])) "\n")))

(defn unpack-list 
  [observations flag]
  (apply str (for [item observations] (unpack item flag))))

(defn report-csv
  "Output concerns to a file, takes collection of maps and filename, saves as CSV
   (report concerns filename)"
  [flagged filename isFlagged]
  (do (spit filename (unpack-list flagged isFlagged) :append true)))