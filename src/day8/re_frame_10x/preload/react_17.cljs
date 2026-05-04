(ns day8.re-frame-10x.preload.react-17
  "Use this namespace with the :preloads compiler option to perform the necessary setup for enabling
   re-frame-10x; e.g.

       {:compiler {:preloads [day8.re-frame-10x.preload.react-17] ...}}

   This namespace pins the React-17 mount path. For most consumers the
   auto-detecting default `day8.re-frame-10x.preload` is preferable —
   it picks the right mount path based on the React version actually
   loaded. This explicit variant remains for consumers that want to
   pin behavior, and for the redundancy of warning when its assumption
   (React 17) doesn't match the React actually loaded."
  (:require
   ["react"        :as react]
   [clojure.string :as str]
   ;; Keep the public namespace loaded from every preload entry point so
   ;; standard preload-only consumers can probe
   ;; goog.global.day8.re_frame_10x.public.*. See preload_loads_public_test.clj.
   [day8.re-frame-10x.public]
   [day8.re-frame-10x.preload-impl.shared   :as shared]
   [day8.re-frame-10x.preload-impl.react-17 :as r17]))

(let [react-major-version (first (str/split react/version #"\."))]
  (when-not (= "17" react-major-version)
    (js/console.warn "Re-frame-10x expects React 17, but React"
                     react-major-version
                     "is loaded. This causes deprecation warnings."
                     (when (= "18" react-major-version)
                       "To fix this, try declaring `day8.re-frame-10x.preload` (the auto-detecting default) or `day8.re-frame-10x.preload.react-18` in your shadow-cljs.edn (instead of `day8.re-frame-10x.preload.react-17`). See https://github.com/day8/re-frame-10x/#compatibility-matrix"))))

(shared/setup-and-mount! r17/mount!)
