(ns day8.re-frame-10x.preload-impl.react-17
  "React-17 mount fn — uses `reagent.dom/render` (the legacy ReactDOM.render
   path). Pure: no top-level side effects so this can be co-loaded with
   the React-18 mount path in the auto-detecting default preload."
  (:require
   [day8.re-frame-10x                                         :as re-frame-10x]
   [day8.re-frame-10x.inlined-deps.reagent.v1v2v0.reagent.dom :as rdom]))

(defn mount!
  [shadow-root]
  (rdom/render (re-frame-10x/create-style-container shadow-root)
               shadow-root))
