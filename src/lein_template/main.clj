(ns lein-template.main
  (:gen-class)
  (:use [lein-template.config :only [app-configs cfg]]
        [clojure.tools.cli :only [cli]]
        [org.httpkit.server :only [run-server]]
        [lein-template.core :only [server-routes]]
        [clojure.tools.logging :only [info]]))

(defn- to-int [s] (Integer/parseInt s))

(defonce server (atom nil))

(defn -main [& args]
  (let [[options _ banner]
        (cli args
             ["-p" "--port" "Port to listen" :default 8080 :parse-fn to-int]
             ["--thread" "Http worker thread count" :default 4 :parse-fn to-int]
             ["--profile" "dev or prod" :default :dev :parse-fn keyword]
             ["--[no-]help" "Print this help"])]
    (when (:help options) (println banner) (System/exit 0))
    ;; config can be accessed by (cfg :key)
    (swap! app-configs merge options)
    (when-not (nil? @server) (@server)) ;; for run -main multi-times
    (reset! server (run-server (server-routes) {:port (cfg :port)
                                                :thread (cfg :thread)}))
    (info (str "server started. listen on 0.0.0.0@" (cfg :port)))))
