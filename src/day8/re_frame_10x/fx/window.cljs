(ns day8.re-frame-10x.fx.window
  (:require
   [goog.object                                                  :as gobj]
   [goog.string                                                  :as gstring]
   [clojure.string                                               :as string]
   [day8.re-frame-10x.inlined-deps.reagent.v1v2v0.reagent.core   :as r]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]))

(def popout-window (r/atom nil))

(defonce popup-mounter
  ;; Mount fn for the popout debugger window, shape [element shadow-root]
  ;; -> mounted-handle (with .unmount where the API provides one).
  ;; nil means navigation.views/mount uses its legacy `reagent.dom/render`
  ;; path (React <= 18). preload-impl.react-19 registers a createRoot-based
  ;; mounter here at load, because React 19 removed ReactDOM.render.
  (atom nil))

(defn mount-with-registry
  "Mounts `element` into `shadow-root` via the registered popup-mounter,
   falling back to `legacy-render` (shape [element shadow-root]) when no
   mounter is registered. React 19 removed ReactDOM.render, so React-19
   consumers get a createRoot-based mounter from preload-impl.react-19;
   older Reacts fall through to `reagent.dom/render`."
  [element shadow-root legacy-render]
  (if-let [mounter @popup-mounter]
    (mounter element shadow-root)
    (legacy-render element shadow-root)))

(defn m->str
  [m]
  (->> m
       (reduce (fn [ret [k v]]
                 (let [k (if (keyword? k) (name k) k)
                       v (if (keyword? v) (name v) v)]
                   (conj ret (str k "=" v))))
               [])
       (string/join ",")))

(defn open-debugger-window
  "Originally copied from re-frisk.devtool/open-debugger-window"
  [{:keys [width height top left on-load on-success on-failure]}]
  (let [document-title  js/document.title
        window-title    (gstring/escapeString (str "re-frame-10x | " document-title))
        window-html     (str "<head><title>"
                             window-title
                             "</title></head><body style=\"margin: 0px;\"></body>")
        window-features (m->str
                         {:width       width
                          :height      height
                          :left        left
                          :top         top
                          :resizable   :yes
                          :scrollbars  :yes
                          :status      :no
                          :directories :no
                          :toolbar     :no
                          :menubar     :no})]
    ;; We would like to set the windows left and top positions to match the monitor that it was on previously, but Chrome doesn't give us
    ;; control over this, it will only position it within the same display that it was popped out on.
    (if-let [w (js/window.open "about:blank" "re-frame-10x-popout" window-features)]

      (let [d (.-document w)]
        ;; We had to comment out the following unmountComponentAtNode as it causes a React exception we assume
        ;; because React says el is not a root container that it knows about.
        ;; In theory by not freeing up the resources associated with this container (e.g. event handlers) we may be
        ;; creating memory leaks. However with observation of the heap in developer tools we cannot see any significant
        ;; unbounded growth in memory usage.
        ;(when-let [el (.getElementById d "--re-frame-10x--")]
        ;  (r/unmount-component-at-node el)))
        (.open d)
        (.write d window-html)
        (gobj/set w "onload" (partial on-load w d))
        (.close d)
        (rf/dispatch on-success)
        (reset! popout-window w))
      (rf/dispatch on-failure))))

(rf/reg-fx
 ::open-debugger-window
 open-debugger-window)

(rf/reg-fx
 ::close-debugger-window
 (fn [] (reset! popout-window nil)))
