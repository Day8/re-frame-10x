(ns day8.re-frame-10x.preload
  "Default preload entry-point. Auto-detects React's major version at
   runtime and dispatches to the matching mount path — no manual
   `:preloads [day8.re-frame-10x.preload.react-18]` needed for React-18
   consumers, no `ReactDOM.render is no longer supported` deprecation,
   no React-17/18 mismatch warning.

   Use:

       {:compiler {:preloads [day8.re-frame-10x.preload] ...}}

   Consumers who prefer to pin a specific mount path can still set
   `day8.re-frame-10x.preload.react-17` or `day8.re-frame-10x.preload.react-18`
   explicitly — both keep the same behavior they had before.

   React-19 consumers MUST use `day8.re-frame-10x.preload.react-19`
   instead: react-dom@19 removed `createRoot` from the top-level
   `react-dom` exports, and this auto-detecting preload cannot require
   `react-dom/client` itself (shadow-cljs resolves JS `:require`s at
   compile time, so that would break React-17 consumers, #425). The
   dispatch below warns when it sees React 19 and leaves the mounting
   to that explicit entry point."
  (:require
   ["react"        :as react]
   [clojure.string :as str]
   ;; Keep the public namespace loaded from every preload entry point so
   ;; standard preload-only consumers can probe
   ;; goog.global.day8.re_frame_10x.public.*. See preload_loads_public_test.clj.
   [day8.re-frame-10x.public]
   [day8.re-frame-10x.preload-impl.shared   :as shared]
   [day8.re-frame-10x.preload-impl.react-17 :as r17]
   [day8.re-frame-10x.preload-impl.react-18 :as r18]))

(let [major (first (str/split react/version #"\."))]
  (case major
    "17"   (shared/setup-and-mount! r17/mount!)
    "18"   (shared/setup-and-mount! r18/mount!)
    "19"   (js/console.warn
             "re-frame-10x: detected React" react/version
             "— react-dom@19 removed createRoot from the top-level react-dom"
             "exports, so the auto-detecting preload cannot mount the panel."
             "Set :preloads explicitly to day8.re-frame-10x.preload.react-19.")
    (do
      (js/console.warn
        "re-frame-10x: unrecognized React version" react/version
        "— falling back to React-18 mount path. If this is wrong, set"
        ":preloads explicitly to day8.re-frame-10x.preload.react-17 or"
        "day8.re-frame-10x.preload.react-18 (React 19:"
        "day8.re-frame-10x.preload.react-19).")
      (shared/setup-and-mount! r18/mount!))))
