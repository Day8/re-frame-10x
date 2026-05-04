(ns day8.re-frame-10x.preload-impl.react-18
  "React-18 mount fn — uses `reagent.dom.client/create-root` + `render`
   (React's createRoot API). Pure: no top-level side effects so this can
   be co-loaded with the React-17 mount path in the auto-detecting default
   preload. See https://react.dev/blog/2022/03/29/react-v18#react-dom-client."
  (:require
   [day8.re-frame-10x                                                :as re-frame-10x]
   [day8.re-frame-10x.inlined-deps.reagent.v1v2v0.reagent.dom.client :as rdc]))

(defn mount!
  [shadow-root]
  (rdc/render (rdc/create-root shadow-root)
              (re-frame-10x/create-style-container shadow-root)))
