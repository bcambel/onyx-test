(defproject bctest "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.onyxplatform/onyx "0.8.2"]
                 [org.onyxplatform/onyx-seq "0.8.3.1"]
                 [cheshire "5.5.0"]
                 ]
  :profiles {:uberjar {:aot [bctest.launcher.aeron-media-driver
                             bctest.launcher.launch-prod-peers]}
             :dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]]
                   :source-paths ["env/dev" "src"]}})
