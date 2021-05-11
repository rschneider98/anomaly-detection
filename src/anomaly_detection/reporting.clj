(ns anomaly-detection.reporting
  "Functions to store out metric calculations and records"
  (:gen-class)
  (:require [clojure.data.json :as json]))

(defn report
  "Output concerns to a file, takes collection of maps and filename, saves as CSV
   (report concerns filename)"
  [flagged filename]
  (spit filename (json/write-str flagged)))