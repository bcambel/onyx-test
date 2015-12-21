(defproject bctest "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                  ; [org.clojure/clojure "1.7.0"]
                  [org.clojure/clojure "1.8.0-RC4"]
                 [org.onyxplatform/onyx "0.8.2"]
                 [org.onyxplatform/onyx-seq "0.8.3.1"]
                 [cheshire "5.5.0"]
                 [clojurewerkz/urly "1.0.0"]
                 [clj-time "0.11.0"]
                 [eu.bitwalker/UserAgentUtils "1.18"]
                 [com.maxmind.geoip2/geoip2 "2.3.1"]
                 [com.taoensso/carmine "2.12.1"]
                 [org.onyxplatform/onyx-redis "0.8.2.2"]
                 ]
  :jvm-opts ["-Xmx10g"]
  :profiles {:uberjar {:aot [bctest.launcher.aeron-media-driver
                             bctest.launcher.launch-prod-peers]}
             :dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]]
                   :source-paths ["env/dev" "src"]}})
