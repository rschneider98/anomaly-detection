(ns anomaly-detection.reporting
  "Functions to store out metric calculations and records"
  (:gen-class)
  (:require [clojure.string :as str]
            [org.clojure/data.json "2.2.2"]))

(defn report
  "Output concerns to a file, takes collection of maps and filename, saves as CSV
   (report concerns filename)"
  [flagged filename]
  (spit (str/join ["reporting/", filename]) (json/write-str flagged)))