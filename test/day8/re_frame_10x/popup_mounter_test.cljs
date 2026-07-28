(ns day8.re-frame-10x.popup-mounter-test
  "cljs-side proof for the popout mount registry.

   Scope note: this ns deliberately does NOT require
   day8.re-frame-10x.preload-impl.react-19 — that chain reaches the panel
   UI namespaces, which touch js/document at load and crash a bare-node
   test runner. The react-dom/client resolution proof for react-dom@19 is
   carried by (a) this build compiling the react-19 impl at all, and
   (b) the examples/todomvc browser build, which preloads
   day8.re-frame-10x.preload.react-19. The JVM-side
   preload_react_19_test.clj pins the registration wiring structurally."
  (:require [cljs.test :refer [deftest is testing]]
            [day8.re-frame-10x.fx.window :as fx.window]))

(deftest mount-with-registry-prefers-registered-mounter
  (testing "registered mounter receives [element shadow-root]; legacy fallback not called"
    (let [calls (atom [])]
      (with-redefs [fx.window/popup-mounter (atom (fn [el root]
                                                    (swap! calls conj [:mounter el root])
                                                    :mounted))]
        (is (= :mounted
               (fx.window/mount-with-registry [:div] :fake-shadow-root
                                              (fn [& _] (swap! calls conj [:legacy])))))
        (is (= [[:mounter [:div] :fake-shadow-root]] @calls))))))

(deftest mount-with-registry-falls-back-to-legacy
  (testing "nil registry -> legacy render fn called (React <= 18 path)"
    (with-redefs [fx.window/popup-mounter (atom nil)]
      (is (= :legacy
             (fx.window/mount-with-registry [:div] :fake-shadow-root
                                            (fn [& _] :legacy)))))))

(deftest popup-mounter-default-is-nil
  (testing "fresh fx.window load leaves the registry empty (legacy path stays default for React <= 18)"
    ;; popup-mounter is defonce'd to (atom nil); nothing in the node-test
    ;; bundle registers a mounter (that happens in preload-impl.react-19).
    (is (nil? @fx.window/popup-mounter))))
