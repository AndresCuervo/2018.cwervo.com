(defproject vr_test "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring-server "0.4.0"]
                 [reagent "0.7.0"]
                 [reagent-utils "0.2.1"]
                 [ring "1.6.2"]
                 [ring/ring-defaults "0.3.1"]
                 [compojure "1.6.0"]
                 [hiccup "1.0.5"]
                 [hiccups "0.3.0"]
                 [cljsjs/aframe "0.7.0-0"]
                 [sablono "0.8.0"]
                 [yogthos/config "0.9"]
                 [org.clojure/clojurescript "1.9.908"
                  :scope "provided"]
                 [secretary "1.2.3"]
                 [venantius/accountant "0.2.0"
                  :exclusions [org.clojure/tools.reader]]]

  :plugins [[lein-environ "1.0.2"]
            [lein-cljsbuild "1.1.5"]
            [lein-asset-minifier "0.2.7"
             :exclusions [org.clojure/clojure]]]

  :ring {:handler vr-test.handler/app
         :uberwar-name "vr_test.war"}

  ;; Add newest lein version, and add a workaround for the renamed java.xml.bind error for Java 9:
  ;; https://github.com/http-kit/http-kit/issues/356
  :min-lein-version "2.8.1"
  ;; :jvm-opts ["--add-modules" "java.xml.bind"]

  :uberjar-name "vr_test.jar"

  :main vr-test.server

  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc"]
  :resource-paths ["resources" "target/cljsbuild"]

  :minify-assets
  {:assets
   {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild
  {:builds {:min
            {:source-paths ["src/cljs" "src/cljc" "env/prod/cljs"]
             :compiler
             {:output-to        "target/cljsbuild/public/js/app.js"
              :output-dir       "target/cljsbuild/public/js"
              :source-map       "target/cljsbuild/public/js/app.js.map"
              :optimizations :advanced

              :language-in :ecmascript6
              ;; Hmmmmmmm, ecmascript6 & ecmascript6-strict don't seem to work??? RIP
              :language-out :ecmascript5

              ;; Wow, okay, this is dope: save whatever variables from being munged
              ;; here, thanks to this great blog post: http://www.lispcast.com/clojurescript-externs
              ;; I wish there was a way to save everything in this.whatever, ya know?
              :externs ["externs/aframe.js"]

              :closure-warnings {:global-this :off}

              ;; Debugging tip from: https://github.com/clojure/clojurescript/wiki/Advanced-Compilation
              ;; Set both of these to true to see proper errors!
              :pseudo-names true
              :pretty-print true
              }}
            :app
            {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
             :figwheel {:on-jsload "vr-test.core/mount-root"}
             :compiler
             {:main "vr_test.dev"
              :asset-path "/js/out"
              :output-to "target/cljsbuild/public/js/app.js"
              :output-dir "target/cljsbuild/public/js/out"
              :source-map true
              :optimizations :none
              ;; :language-out :es6-strict
              :pretty-print  true}}



            }
   }


  :figwheel
  {:http-server-root "public"
   :server-port 3449
   :nrepl-port 7002
   :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"
                      ]
   :css-dirs ["resources/public/css"]
   :ring-handler vr-test.handler/app}



  :profiles {:dev {:repl-options {:init-ns vr-test.repl
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :dependencies [[binaryage/devtools "0.9.4"]
                                  [ring/ring-mock "0.3.1"]
                                  [ring/ring-devel "1.6.2"]
                                  [prone "1.1.4"]
                                  [figwheel-sidecar "0.5.13"]
                                  [org.clojure/tools.nrepl "0.2.13"]
                                  [com.cemerick/piggieback "0.2.2"]
                                  [pjstadig/humane-test-output "0.8.2"]
                                  ]

                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.13"]
                             ]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :env {:dev true}}

             :uberjar {:hooks [minify-assets.plugin/hooks]
                       :source-paths ["env/prod/clj"]
                       :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
                       :env {:production true}
                       :aot :all
                       :omit-source true}})
