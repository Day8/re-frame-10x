(ns day8.re-frame-10x.preload-impl.shared
  "Shared setup for every preload entry-point. Extracted so the default
   `day8.re-frame-10x.preload` (which auto-detects React version) and the
   explicit React-version variants (`preload.react-17`, `preload.react-18`)
   converge on a single source of truth — patch the userland reagent
   batching/trace seams, init re-frame state, build the panel's shadow-root,
   and hand it to a React-version-specific mount fn.

   No top-level side effects so this can be safely co-loaded by callers
   that expose multiple mount paths in the same bundle."
  (:require
   [day8.re-frame-10x                                            :as re-frame-10x]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.events                                     :as events]))

(defn setup-and-mount!
  "Run the React-version-agnostic preload setup and hand the freshly
   created shadow-root to `mount-fn`. The mount-fn is the only piece
   that varies between React 17 (`reagent.dom/render`) and React 18
   (`reagent.dom.client/create-root` + `render`)."
  [mount-fn]
  (re-frame-10x/patch!)
  (rf/dispatch-sync [::events/init re-frame-10x/project-config])
  (rf/clear-subscription-cache!)
  (let [shadow-root (re-frame-10x/create-shadow-root)]
    (mount-fn shadow-root)
    shadow-root))
