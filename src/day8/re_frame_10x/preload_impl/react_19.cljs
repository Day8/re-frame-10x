(ns day8.re-frame-10x.preload-impl.react-19
  "React-19 mount fn — uses `react-dom/client`'s createRoot API. Pure: no
   top-level side effects beyond registering the popout mounter, so this can
   be co-loaded with the other mount paths.

   Unlike preload-impl.react-18 (which reaches `createRoot` via interop on
   the top-level `react-dom` module), this namespace REQUIRES the
   `react-dom/client` subpath directly: React 19 removed `createRoot` from
   the top-level `react-dom` exports (it exists on 18, gone on 19), so the
   interop trick no longer works.

   That subpath only exists in `react-dom@18+`, and shadow-cljs resolves all
   JS `:require`s at compile time — so this namespace must ONLY be loaded by
   consumers on React 18+ via the explicit `day8.re-frame-10x.preload.react-19`
   entry point. The auto-detecting default `day8.re-frame-10x.preload` does
   NOT require this namespace (it would break compile for React-17
   consumers, see #425); its dispatch warns React-19 users to opt in.

   Registers a createRoot-based popout mounter so the external-window
   debugger (navigation.views/mount) also works on React 19, where the
   legacy `reagent.dom/render` path it falls back to is gone.

   Uses `reagent.core/as-element` (React-version-agnostic) to convert
   hiccup to a React element before handing it to `root.render`."
  (:require
   ["react-dom/client" :as react-dom-client]
   [day8.re-frame-10x                                            :as re-frame-10x]
   [day8.re-frame-10x.fx.window                                  :as fx.window]
   [day8.re-frame-10x.inlined-deps.reagent.v1v2v0.reagent.core   :as r]))

(defn mount!
  [shadow-root]
  (let [root (react-dom-client/createRoot shadow-root)]
    (.render root (r/as-element
                    (re-frame-10x/create-style-container shadow-root)))
    root))

(defn popup-mounter
  "Mount fn for the popout debugger window. Same shape as
   navigation.views/mount's legacy `rdom/render` call site: takes the
   hiccup element and the shadow-root, returns the React root (which
   carries `.unmount`). Converts hiccup via `reagent.core/as-element` —
   root.render takes a React element, not hiccup."
  [element shadow-root]
  (let [root (react-dom-client/createRoot shadow-root)]
    (.render root (r/as-element element))
    root))

;; Register so navigation.views/mount prefers this over the legacy
;; `rdom/render` fallback (removed in React 19).
(reset! fx.window/popup-mounter popup-mounter)
