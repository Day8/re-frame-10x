(defproject todomvc-re-frame "lein-git-inject/version"
  :dependencies [[org.clojure/clojure        "1.10.1"]
                 [org.clojure/clojurescript  "1.10.773"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [thheller/shadow-cljs "2.9.10"]
                 [reagent "0.10.0"]
                 [re-frame "0.12.0"]
                 [day8.re-frame/tracing "0.5.6"]
                 [day8.re-frame/re-frame-10x "0.6.6"]
                 [secretary "1.2.3"]]

  :plugins      [[day8/lein-git-inject "0.0.14"]
                 [lein-shadow          "0.1.6"]]

  :middleware   [leiningen.git-inject/middleware]

  :profiles {:dev  {:dependencies [[binaryage/devtools "1.0.2"]]}}

  :source-paths ["src" "../../src" "../../gen-src"]

  :clean-targets ^{:protect false} [:target-path
                                    "resources/public/js"]

  :shadow-cljs {:nrepl  {:port 8777}

                :builds {:app {:target     :browser
                               :output-dir "resources/public/js"
                                 :modules    {:todomvc {:init-fn  todomvc.core/main
                                                        :preloads [day8.re-frame-10x.preload]}}
                               :dev        {:compiler-options {:closure-defines {re-frame.trace.trace-enabled?        true
                                                                                 day8.re-frame-10x.debug?             true
                                                                                 day8.re-frame.tracing.trace-enabled? true}
                                                               :external-config  {:devtools/config {:features-to-install [:formatters :hints]}}}}
                               :devtools   {:http-root "resources/public"
                                            :http-port 8280}}}}

  :aliases {"dev-auto" ["with-profile" "dev" "shadow" "watch" "app"]})
