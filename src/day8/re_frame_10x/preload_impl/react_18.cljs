(ns day8.re-frame-10x.preload-impl.react-18
  "React-18 mount fn — uses React 18's createRoot API. Pure: no
   top-level side effects so this can be co-loaded with the React-17
   mount path in the auto-detecting default preload.

   Avoids requiring `react-dom/client` directly (and the inlined
   `reagent.dom.client` that does so transitively): that subpath only
   exists in `react-dom@18+`, and shadow-cljs resolves all JS
   `:require`s at compile time regardless of the runtime dispatch
   branch — so requiring `react-dom/client` would break compile for
   React-17 consumers using the auto-detect default preload (closes
   #425).

   Instead, require the top-level `react-dom` (works on both React 17
   and 18) and reach for `createRoot` via interop. `createRoot` is
   undefined on React 17; `preload.cljs`'s dispatch keeps React-17
   consumers off this code path, and the `when-let` guard below adds
   defense in depth — if mount! were ever invoked on React 17, it
   returns nil instead of throwing.

   Uses `reagent.core/as-element` (React-version-agnostic) to convert
   hiccup to a React element before handing it to `root.render`."
  (:require
   ["react-dom" :as react-dom]
   [day8.re-frame-10x                                            :as re-frame-10x]
   [day8.re-frame-10x.inlined-deps.reagent.v1v2v0.reagent.core   :as r]))

(defn mount!
  [shadow-root]
  (when-let [create-root (.-createRoot react-dom)]
    (let [root (create-root shadow-root)]
      (.render root (r/as-element
                      (re-frame-10x/create-style-container shadow-root))))))
