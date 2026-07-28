(ns day8.re-frame-10x.preload-react-19-test
  "Regression tests for React 19 support.

   react-dom@19 removed `createRoot` (and `render`) from the top-level
   `react-dom` exports — they only exist on the `react-dom/client`
   subpath. The default preload cannot require that subpath (shadow-cljs
   resolves JS :requires at compile time; react-dom@17 has no /client
   entry, so React-17 consumers would fail to compile, #425). React 19
   support therefore lives in an explicit, opt-in entry point:

     :preloads [day8.re-frame-10x.preload.react-19]

   Static structural checks (read the source on the JVM, no JS runtime):
     1. preload-impl.react-19 REQUIRES react-dom/client (the whole point).
     2. The explicit preload.react-19 entry requires shared + react-19 impl
        and the public ns (every preload entry must load public, see
        preload_loads_public_test.clj).
     3. The default preload must NOT require preload-impl.react-19
        (would transitively require react-dom/client and re-introduce
        #425 for React-17 consumers).
     4. The default preload dispatch has an explicit \"19\" branch that
        points at the explicit variant (no silent no-op mount)."
  (:require [clojure.java.io :as io]
            [clojure.test    :refer [deftest is testing]]))

(def ^:private react-19-impl-source
  (io/file "src/day8/re_frame_10x/preload_impl/react_19.cljs"))

(def ^:private react-19-preload-source
  (io/file "src/day8/re_frame_10x/preload/react_19.cljs"))

(def ^:private default-preload-source
  (io/file "src/day8/re_frame_10x/preload.cljs"))

(defn- read-source [path]
  (slurp path))

(defn- read-ns-form [path]
  (with-open [r (java.io.PushbackReader. (io/reader path))]
    (read r)))

(defn- require-clause [ns-form]
  (some (fn [x]
          (when (and (sequential? x) (= :require (first x)))
            (rest x)))
        ns-form))

(defn- requires-ns? [require-body target]
  (boolean
    (some (fn [spec]
            (let [sym (cond
                        (symbol? spec)     spec
                        (sequential? spec) (first spec))]
              (= target sym)))
          require-body)))

(defn- string-spec? [spec]
  ;; A JS :require entry, e.g. `["react-dom/client" :as ...]`.
  (cond
    (string? spec) spec
    (sequential? spec) (when (string? (first spec)) (first spec))))

(deftest preload-impl-react-19-requires-react-dom-client
  (testing "react_19.cljs requires [\"react-dom/client\" ...] — React 19 removed createRoot from the top-level react-dom exports, the subpath is the only way to reach it"
    (let [required (require-clause (read-ns-form react-19-impl-source))
          js-strs  (keep string-spec? required)]
      (is (some #(re-matches #"react-dom/client(/.*)?" %) js-strs)
          (str (.getPath react-19-impl-source) " must (:require [\"react-dom/client\" ...]). "
               "On react-dom@19 the top-level module has no createRoot.")))))

(deftest preload-impl-react-19-exists-and-registers-popup-mounter
  (testing "react_19.cljs registers a createRoot-based popout mounter (React 19 removed ReactDOM.render used by the legacy popout path)"
    (let [src (read-source react-19-impl-source)]
      (is (re-find #"popup-mounter" src)
          (str (.getPath react-19-impl-source) " must reset! fx.window/popup-mounter "
               "so navigation.views/mount avoids the legacy ReactDOM.render path.")))))

(deftest preload-react-19-entry-requires-shared-and-impl
  (testing "preload/react_19.cljs requires the shared setup, the react-19 impl, and the public ns"
    (let [required (require-clause (read-ns-form react-19-preload-source))]
      (is (requires-ns? required 'day8.re-frame-10x.preload-impl.shared)
          (str (.getPath react-19-preload-source) " must require preload-impl.shared."))
      (is (requires-ns? required 'day8.re-frame-10x.preload-impl.react-19)
          (str (.getPath react-19-preload-source) " must require preload-impl.react-19."))
      (is (requires-ns? required 'day8.re-frame-10x.public)
          (str (.getPath react-19-preload-source) " must require day8.re-frame-10x.public "
               "like every other preload entry point.")))))

(deftest default-preload-does-not-require-react-19-impl
  (testing "preload.cljs must NOT require preload-impl.react-19 — it transitively requires react-dom/client, which react-dom@17 lacks (#425)"
    (let [required (require-clause (read-ns-form default-preload-source))]
      (is (not (requires-ns? required 'day8.re-frame-10x.preload-impl.react-19))
          (str (.getPath default-preload-source) " must not require preload-impl.react-19. "
               "React-19 consumers opt in via day8.re-frame-10x.preload.react-19 instead.")))))

(deftest default-preload-warns-and-points-at-react-19-variant
  (testing "preload.cljs dispatch has an explicit \"19\" branch directing users to the explicit variant (no silent no-op mount)"
    (let [src (read-source default-preload-source)]
      (is (re-find #"\"19\"" src)
          (str (.getPath default-preload-source) " must have an explicit \"19\" case branch."))
      (is (re-find #"day8\.re-frame-10x\.preload\.react-19" src)
          (str (.getPath default-preload-source) " must point React-19 users at "
               "day8.re-frame-10x.preload.react-19.")))))
