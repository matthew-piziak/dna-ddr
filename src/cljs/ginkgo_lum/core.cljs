(ns ginkgo-lum.core
  (:require
    [day8.re-frame.http-fx]
    [reagent.dom :as rdom]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [re-pressed.core :as rp]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [ginkgo-lum.ajax :as ajax]
    [ginkgo-lum.events]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe]
    [clojure.string :as s])
  (:import goog.History))

(defn nav-link [uri title page]
  [:a.navbar-item
   {:href   uri
    :class (when (= page @(rf/subscribe [:common/page-id])) :is-active)}
   title])

(defn navbar []
  (r/with-let [expanded? (r/atom false)]
              [:nav.navbar.is-info>div.container
               [:div.navbar-brand
                [:a.navbar-item {:href "/" :style {:font-weight :bold}} "DNA DDR"]
                [:span.navbar-burger.burger
                 {:data-target :nav-menu
                  :on-click #(swap! expanded? not)
                  :class (when @expanded? :is-active)}
                 [:span][:span][:span]]]
               ;; [:div#nav-menu.navbar-menu
               ;;  {:class (when @expanded? :is-active)}
               ;;  [:div.navbar-start
               ;;   [nav-link "#/" "Home" :home]
               ;;   [nav-link "#/about" "About" :about]
               ;;   [nav-link "#/dna" "DNA" :dna]]]
               ]))

(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]])

(defn home-page []
  [:section.section>div.container>div.content
   (when-let [docs @(rf/subscribe [:docs])]
     [:div {:dangerouslySetInnerHTML {:__html (md->html docs)}}])])

(def nucleotides (r/atom ""))

(defonce key-rules (rf/dispatch-sync [::rp/add-keyboard-event-listener "keydown"]))
(rf/dispatch
 [::rp/set-keydown-rules
  {:event-keys
   [
    ;; Use arrow keys to move
    [[:nc-a] [{:keyCode 37}]]
    [[:nc-t] [{:keyCode 39}]]
    [[:nc-c] [{:keyCode 38}]]
    [[:nc-g] [{:keyCode 40}]]

    ;; ESC to clear
    [[:nc-esc] [{:keyCode 27}]]

    ;; In case of confusion, also permit typing ACTG
    [[:nc-a] [{:keyCode 65}]]
    [[:nc-t] [{:keyCode 84}]]
    [[:nc-c] [{:keyCode 67}]]
    [[:nc-g] [{:keyCode 71}]]]
   }])

;;; input validation
(defn dna-page []
  (let [dna @(rf/subscribe [:dna])
        ddr-search @(rf/subscribe [:ddr-search])
        amino-acid @(rf/subscribe [:amino-acid])
        ]
    [:section.section>div.container>div.content
     [:h1 "DNA DDR"]
     [:h2 "Use arrow keys to move!"]
     [:h2 "←A ↑C ↓G →T (ESC to clear)"]
     [:h3 ddr-search]
     [:h3 amino-acid]
     [:div
      [:input {:type "text"
               :value @nucleotides
               :on-change #(reset! nucleotides (.-value (.-target %)))
               }]
      [:button {:on-click #(rf/dispatch [:common/search-dna @nucleotides])} "Search Manually"]]
     [:hr]
     (if (s/includes? dna "PBCV-1") [:img {:src "/img/PBCV-1.jpg" :style {:max-width 100}}])
     (if (s/includes? dna "EhV-86") [:img {:src "/img/EhV-86.jpg" :style {:max-width 100}}])
     (if (s/includes? dna "ATCV-1") [:img {:src "/img/ATCV-1.jpg" :style {:max-width 100}}])
     (if (s/includes? dna "PBCV-AR158") [:img {:src "/img/PBCV-AR158.jpg" :style {:max-width 100}}])
     (if (s/includes? dna "Megavirus") [:img {:src "/img/Megavirus.jpg" :style {:max-width 100}}])
     (if (s/includes? dna "APMV") [:img {:src "/img/APMV.jpg" :style {:max-width 100}}])
     (if (s/includes? dna "Pithovirus") [:img {:src "/img/Pithovirus.jpg" :style {:max-width 100}}])
     (if (s/includes? dna "Phage-G") [:img {:src "/img/Phage-G.jpg" :style {:max-width 100}}])
     (if (s/includes? dna "Mollivirus") [:img {:src "/img/Mollivirus.jpg" :style {:max-width 100}}])
     (if (s/includes? dna "BV-PW1") [:img {:src "/img/BV-PW1.jpg" :style {:max-width 100}}])
     [:div (map (fn [d] [:div d]) (into [] (re-seq #"\[[^\[\]]*\]" dna)))]
     [:hr]]))

(defn page []
  [:div
   [navbar]
   [dna-page]])

(defn navigate! [match _]
  (rf/dispatch [:common/navigate match]))

(def router
  (reitit/router
    [["/" {:name        :home
           :view        #'home-page
           :controllers [{:start (fn [_] (rf/dispatch [:page/init-home]))}]}]
     ["/about" {:name :about
                :view #'about-page}]
     ["/dna" {:name :dna
              :view #'dna-page}]]))

(defn start-router! []
  (rfe/start!
    router
    navigate!
    {}))

;; -------------------------
;; Initialize app
(defn ^:dev/after-load mount-components []
  (rf/clear-subscription-cache!)
  (rdom/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (start-router!)
  (ajax/load-interceptors!)
  (mount-components))
