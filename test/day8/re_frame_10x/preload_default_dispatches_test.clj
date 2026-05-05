(ns day8.re-frame-10x.preload-default-dispatches-test
  "Regression test for the default preload's React-version auto-detection.

   The default `day8.re-frame-10x.preload` ns dispatches at runtime to
   either the React-17 or React-18 mount path based on the React version
   actually loaded. The dispatch is what eliminates the
   `Re-frame-10x expects React 17, but React 18 is loaded.` warning for
   consumers who set the default preload — they no longer have to know
   which variant matches their React.

   Static structural check (read the source on the JVM, no JS runtime
   needed): assert the default preload requires both impl mount fns
   plus the shared setup ns, and references `react/version` so the
   dispatch is wired. A regression that drops one of the impl requires
   or the version reference would silently fall back to the catch-all
   (probably React 18 mount), making this test the early-warning."
  (:require [clojure.java.io :as io]
            [clojure.test    :refer [deftest is testing]]))

(def ^:private preload-source
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

(deftest default-preload-requires-both-impl-mounts
  (testing "preload.cljs requires day8.re-frame-10x.preload-impl.shared"
    (let [required (require-clause (read-ns-form preload-source))]
      (is (requires-ns? required 'day8.re-frame-10x.preload-impl.shared)
          (str (.getPath preload-source) " must require preload-impl.shared "
               "for the patch/dispatch/cache/shadow-root setup."))
      (is (requires-ns? required 'day8.re-frame-10x.preload-impl.react-17)
          (str (.getPath preload-source) " must require preload-impl.react-17 "
               "so its mount fn is in the bundle for React-17 consumers."))
      (is (requires-ns? required 'day8.re-frame-10x.preload-impl.react-18)
          (str (.getPath preload-source) " must require preload-impl.react-18 "
               "so its mount fn is in the bundle for React-18 consumers.")))))

(deftest default-preload-references-react-version
  (testing "preload.cljs reads react/version for the dispatch"
    (let [src (read-source preload-source)]
      (is (re-find #"react/version" src)
          (str (.getPath preload-source) " must reference react/version — "
               "without it the auto-detect dispatch can't choose between "
               "the React-17 and React-18 mount paths.")))))

;; ---------------------------------------------------------------------------
;; Tests for #425: preload-impl.react-18 must NOT statically require
;; `react-dom/client` (or the inlined reagent.dom.client that does so
;; transitively). shadow-cljs resolves JS :require deps at compile time
;; regardless of runtime branch, so React-17 consumers using the
;; auto-detect default preload would hit a missing-subpath compile failure
;; if either of those requires returned. The fix bypasses the subpath via
;; runtime interop on the top-level `react-dom`.
;; ---------------------------------------------------------------------------

(def ^:private react-18-impl-source
  (io/file "src/day8/re_frame_10x/preload_impl/react_18.cljs"))

(defn- string-spec? [spec]
  ;; A JS :require entry, e.g. `["react-dom/client" :as ...]`.
  (cond
    (string? spec) spec
    (sequential? spec) (when (string? (first spec)) (first spec))))

(deftest preload-impl-react-18-does-not-require-react-dom-client-subpath
  (testing "react_18.cljs must NOT (:require [\"react-dom/client\" ...]) — that subpath only exists on react-dom@18+ and breaks compile for React-17 consumers using the auto-detect default preload"
    (let [required (require-clause (read-ns-form react-18-impl-source))
          js-strs  (keep string-spec? required)]
      (is (not-any? #(re-matches #"react-dom/client(/.*)?" %) js-strs)
          (str (.getPath react-18-impl-source) " must not (:require [\"react-dom/client\" ...]). "
               "Use [\"react-dom\" :as react-dom] and reach for createRoot via interop instead. "
               "See #425.")))))

(deftest preload-impl-react-18-does-not-require-inlined-reagent-dom-client
  (testing "react_18.cljs must NOT require day8.re-frame-10x.inlined-deps.reagent.v1v2v0.reagent.dom.client — it transitively pulls in react-dom/client and re-introduces #425"
    (let [required (require-clause (read-ns-form react-18-impl-source))]
      (is (not (requires-ns? required 'day8.re-frame-10x.inlined-deps.reagent.v1v2v0.reagent.dom.client))
          (str (.getPath react-18-impl-source) " must not require reagent.dom.client. "
               "That ns transitively requires [\"react-dom/client\" ...], which fails to compile "
               "on React 17. Use reagent.core/as-element + interop on top-level react-dom. See #425.")))))
