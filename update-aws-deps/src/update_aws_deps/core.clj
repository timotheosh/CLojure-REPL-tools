(ns update-aws-deps.core
  (:require [clj-http.client :as client])
  (:gen-class))

(def wish-list
  [])

(def valid-api-list
  [:lambda
   :glacier
   :lex-models
   :rds-data
   :sqs
   :datapipeline
   :apigateway
   :rds
   :ecs
   :sns
   :machinelearning
   :autoscaling
   :cloudtrail
   :kms
   :ecr
   :iam
   :es
   :dax
   :redshift
   :elasticbeanstalk
   :sts
   :opsworks
   :route53
   :apigatewayv2
   :cloudformation
   :ec2
   :s3
   :sdb
   :cloudfront
   :dynamodb
   :swf
   :streams-dynamodb
   ])

(defn- valid-api?
  "true if valid-api-list contains api"
  [api]
  (let [short-name (second (clojure.string/split (str api) #"/"))]
    (some #(= (keyword short-name) %) valid-api-list)))

(defn- data->str
  "Converts the dependency data retreived from Cognitect's AWS repo into edn format."
  [data]
  (let [api (first (:api data))
        endpoints (first (:endpoints data))
        spacer (reduce str (repeat 8 " "))]
    (str "{:deps {"
         (first api) "  {:mvn/version \"" (second api) "\"}\n"
         spacer (first endpoints) "  {:mvn/version \"" (second endpoints) "\"}\n"
         (reduce str (for [x (keys (:services data))]
                       (when (valid-api? x)
                         (str spacer
                              x "  {:mvn/version \""
                              (x (:services data)) "\"}\n"))))
         spacer "}\n"
         "}\n")))

(defn- get-latest-dependencies
  "Downloads and returns the latest dependencies from Cognitect's AWS lib repo in Github."
  []
  (data->str
   (read-string
    (:body
     (client/get
      "https://raw.githubusercontent.com/cognitect-labs/aws-api/master/latest-releases.edn")))))

(defn- update-deps
  "Updates the deps.edn file."
  []
  (spit (str (System/getenv "HOME") "/deps.edn") (get-latest-dependencies)))

(defn -main
  "Updates the deps.edn file."
  [& args]
  (update-deps))
