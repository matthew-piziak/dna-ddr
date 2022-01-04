(ns ginkgo-lum.events
  (:require
    [re-frame.core :as rf]
    [ajax.core :as ajax]
    [reitit.frontend.easy :as rfe]
    [reitit.frontend.controllers :as rfc]
    [akiroz.re-frame.storage :refer [reg-co-fx!]]
    [ginkgo-lum.codons :as codons]))

;;; support HTML5 Web Storage

;; both :fx and :cofx keys are optional, they will not be registered if unspecified.
(reg-co-fx! :my-app
            {:fx :store
             :cofx :store})

;; Event dispatchers

(rf/reg-event-db
  :common/navigate
  (fn [db [_ match]]
    (let [old-match (:common/route db)
          new-match (assoc match :controllers
                                 (rfc/apply-controllers (:controllers old-match) match))]
      (assoc db :common/route new-match))))

(rf/reg-fx
  :common/navigate-fx!
  (fn [[k & [params query]]]
    (rfe/push-state k params query)))

(rf/reg-event-fx
  :common/navigate!
  (fn [_ [_ url-key params query]]
    {:common/navigate-fx! [url-key params query]}))

(rf/reg-event-fx
 :fetch-dna
 [(rf/inject-cofx :store)]
 (fn [{:keys [store]} [_ dna]]
   {:store (assoc store :dna dna)
    :http-xhrio {:method          :get
                 :uri             (str "/api/" dna)
                 :timeout         1000
                 :response-format (ajax/raw-response-format)
                 :on-success       [:set-dna]}}))

(rf/reg-event-db
  :set-dna
  (fn [db [_ dna]]
    (assoc db :dna dna)))

(rf/reg-event-db
  :common/set-error
  (fn [db [_ error]]
    (assoc db :common/error error)))

(rf/reg-event-fx
  :page/init-home
  [(rf/inject-cofx :store)]
  (fn [{:keys [store]} _]
    {:dispatch [:fetch-dna (:dna store)]}))

(rf/reg-event-fx
  :common/search-dna
  (fn [cofx [_ dna]]
    {:db (assoc-in (:db cofx) [:ddr-search] dna)
     :dispatch [:fetch-dna dna]}))

;; Event subscriptions

(rf/reg-sub
  :common/route
  (fn [db _]
    (-> db :common/route)))

(rf/reg-sub
  :common/page-id
  :<- [:common/route]
  (fn [route _]
    (-> route :data :name)))

(rf/reg-sub
  :common/page
  :<- [:common/route]
  (fn [route _]
    (-> route :data :view)))

(rf/reg-sub
  :docs
  (fn [db _]
    (:docs db)))

(rf/reg-sub
  :dna
  (fn [db _]
    (or (:dna db) "NO RESPONSE")))

(rf/reg-sub
  :ddr-search
  (fn [db _]
    (:ddr-search db)))

(rf/reg-sub
  :common/error
  (fn [db _]
    (:common/error db)))

(rf/reg-event-fx
  :nc-a
  (fn [cofx [_ _]]
    (let [ddrs (str (:ddr-search (:db cofx)) "A")]
      {:db (assoc-in (:db cofx) [:ddr-search] ddrs)
       :fx [[:dispatch [:fetch-dna ddrs]]]})))

(rf/reg-event-fx
  :nc-t
  (fn [cofx [_ _]]
    (let [ddrs (str (:ddr-search (:db cofx)) "T")]
      {:db (assoc-in (:db cofx) [:ddr-search] ddrs)
       :fx [[:dispatch [:fetch-dna ddrs]]]})))

(rf/reg-event-fx
  :nc-c
  (fn [cofx [_ _]]
    (let [ddrs (str (:ddr-search (:db cofx)) "C")]
      {:db (assoc-in (:db cofx) [:ddr-search] ddrs)
       :fx [[:dispatch [:fetch-dna ddrs]]]})))

(rf/reg-event-fx
  :nc-g
  (fn [cofx [_ _]]
    (let [ddrs (str (:ddr-search (:db cofx)) "G")]
      {:db (assoc-in (:db cofx) [:ddr-search] ddrs)
       :fx [[:dispatch [:fetch-dna ddrs]]]})))

(rf/reg-event-fx
  :nc-del
  (fn [cofx [_ _]]
    (let [ddrs (apply str (butlast (:ddr-search (:db cofx))))]
      {:db (assoc-in (:db cofx) [:ddr-search] ddrs)
       :fx [[:dispatch [:fetch-dna ddrs]]]})))

(rf/reg-event-fx
  :nc-esc
  (fn [cofx [_ _]]
    (let [ddrs ""]
      {:db (assoc-in (:db cofx) [:ddr-search] ddrs)
       :fx [[:dispatch [:fetch-dna "X"]]]})))

(rf/reg-sub
  :amino-acid
  (fn [_]
    [(rf/subscribe [:ddr-search])])
  (fn [[dna] _]
    (if (< (count dna) 3) ""
        (let [triplet (last (partition 3 dna))]
          (str (codons/codons (apply str triplet)) "!")))))
