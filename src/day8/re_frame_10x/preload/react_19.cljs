(ns day8.re-frame-10x.preload.react-19
  "Use this namespace with the :preloads compiler option to perform the necessary setup for enabling
   re-frame-10x; e.g.

       {:compiler {:preloads [day8.re-frame-10x.preload.react-19] ...}}

   This namespace is intended for use with React 19.
   It launches re-frame-10x with react's dom client API
   (react-dom@19 removed createRoot from the top-level react-dom exports,
   so this is the only entry point that requires react-dom/client).
   See https://react.dev/blog/2024/04/25/react-19-upgrade-guide.

   React-17 and React-18 consumers should keep using the auto-detecting
   default `day8.re-frame-10x.preload` — requiring this namespace there
   would break their compile, because react-dom@17 has no /client subpath
   and shadow-cljs resolves JS :requires at compile time (#425)."
  (:require
   ;; Keep the public namespace loaded from every preload entry point so
   ;; standard preload-only consumers can probe
   ;; goog.global.day8.re_frame_10x.public.*. See preload_loads_public_test.clj.
   [day8.re-frame-10x.public]
   [day8.re-frame-10x.preload-impl.shared   :as shared]
   [day8.re-frame-10x.preload-impl.react-19 :as r19]))

(shared/setup-and-mount! r19/mount!)
