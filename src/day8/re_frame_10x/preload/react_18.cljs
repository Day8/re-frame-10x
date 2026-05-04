(ns day8.re-frame-10x.preload.react-18
  "Use this namespace with the :preloads compiler option to perform the necessary setup for enabling
   re-frame-10x; e.g.

       {:compiler {:preloads [day8.re-frame-10x.preload.react-18] ...}}

   This namespace is intended for use with React 18.
   It launches re-frame-10x with react's new dom client API.
   See https://react.dev/blog/2022/03/29/react-v18#react-dom-client.

   For most consumers the auto-detecting default `day8.re-frame-10x.preload`
   is preferable — it picks the right mount path based on the React
   version actually loaded. This explicit variant remains for consumers
   that want to pin behavior."
  (:require
   ;; Keep the public namespace loaded from every preload entry point so
   ;; standard preload-only consumers can probe
   ;; goog.global.day8.re_frame_10x.public.*. See preload_loads_public_test.clj.
   [day8.re-frame-10x.public]
   [day8.re-frame-10x.preload-impl.shared   :as shared]
   [day8.re-frame-10x.preload-impl.react-18 :as r18]))

(shared/setup-and-mount! r18/mount!)
