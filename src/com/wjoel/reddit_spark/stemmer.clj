(ns com.wjoel.reddit-spark.stemmer
  (:import (java.io BufferedReader StringReader)
           (org.apache.lucene.analysis.en EnglishAnalyzer
                                          EnglishPossessiveFilter)
           (org.apache.lucene.analysis.core LowerCaseFilter
                                            StopFilter
                                            StopAnalyzer)
           (org.apache.lucene.analysis.en PorterStemFilter)
           (org.apache.lucene.analysis.tokenattributes CharTermAttribute)))

(defn stem-string [string]
  (let [reader (-> (StringReader. string)
                   (BufferedReader.))]
    (with-open [analyzer (EnglishAnalyzer.)]
      (let [token-stream (-> (.tokenStream analyzer nil reader)
                             (LowerCaseFilter.)
                             (StopFilter. StopAnalyzer/ENGLISH_STOP_WORDS_SET)
                             (EnglishPossessiveFilter.)
                             (PorterStemFilter.))
            cattr (.addAttribute token-stream CharTermAttribute)]
        (.reset token-stream)
        (loop [tokens nil]
          (if (.incrementToken token-stream)
            (recur (cons (.toString cattr)
                         tokens))
            (reverse tokens)))))))
