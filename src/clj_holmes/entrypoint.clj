(ns clj-holmes.entrypoint
  (:require [clj-holmes.engine :as engine]
            [clj-holmes.logic.sarif :as sarif]
            [clojure.data.json :as json]
            [clojure.string :as string])
  (:import (java.io File)))

(defn ^:private clj-file? [file]
  (and (.isFile file)
       (-> file .toString (string/includes? "project.clj") not)
       (-> file .toString (.endsWith ".clj"))))

(defn ^:private clj-files-from-directory [directory]
  (->> directory
       File.
       file-seq
       (filter clj-file?)
       (map str)))

(defn ^:private scan [filename]
  (let [code (slurp filename)
        scan-result (engine/process code)]
    (assoc scan-result :filename filename)))

(defn -main [directory]
  (let [files (clj-files-from-directory directory)
        scans-results (map scan files)
        sarif-report (sarif/scans->sarif scans-results)
        sarif-output-file (format "%s/scan.sarif" directory)]
    (->> sarif-report json/write-str (spit sarif-output-file))
    (println "Sarif report can be find in" sarif-output-file)))

(comment
  (-main "/home/dpr/dev/nu/auth"))