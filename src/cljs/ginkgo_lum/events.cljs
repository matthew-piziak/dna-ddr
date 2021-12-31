(ns ginkgo-lum.events
  (:require
    [re-frame.core :as rf]
    [ajax.core :as ajax]
    [reitit.frontend.easy :as rfe]
    [reitit.frontend.controllers :as rfc]))

;;dispatchers

(rf/reg-event-db
  :common/navigate
  (fn [db [_ match]]
    (let [old-match (:common/route db)
          new-match (assoc match :controllers
                                 (rfc/apply-controllers (:controllers old-match) match))]
      (assoc db :common/route new-match))))

(rf/reg-event-db
  :common/search-dna
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

(rf/reg-event-db
  :set-docs
  (fn [db [_ docs]]
    (assoc db :docs docs)))

(rf/reg-event-fx
  :fetch-docs
  (fn [_ _]
    {:http-xhrio {:method          :get
                  :uri             "/docs"
                  :response-format (ajax/raw-response-format)
                  :on-success       [:set-docs]}}))

(rf/reg-event-fx
  :fetch-dna
  (fn [_ [_ dna]]
    {:http-xhrio {:method          :get
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
  (fn [_ _]
    {:dispatch [:fetch-docs]}))

;;; Wire this up to the response
(rf/reg-event-fx
  :common/search-dna
  (fn [_ [_ dna]]
    {:dispatch [:fetch-dna dna]}))

;; (rf/reg-event-db
;;   :common/search-dna
;;   (fn [db [_ dna]]
;;     (assoc db :last-search dna)))

;;subscriptions

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
    (let [ddrs (str "A" (:ddr-search (:db cofx)))]
      {:db (assoc-in (:db cofx) [:ddr-search] ddrs)
       :fx [[:dispatch [:fetch-dna ddrs]]]})))

(rf/reg-event-fx
  :nc-t
  (fn [cofx [_ _]]
    (let [ddrs (str "T" (:ddr-search (:db cofx)))]
      {:db (assoc-in (:db cofx) [:ddr-search] ddrs)
       :fx [[:dispatch [:fetch-dna ddrs]]]})))

(rf/reg-event-fx
  :nc-c
  (fn [cofx [_ _]]
    (let [ddrs (str "C" (:ddr-search (:db cofx)))]
      {:db (assoc-in (:db cofx) [:ddr-search] ddrs)
       :fx [[:dispatch [:fetch-dna ddrs]]]})))

(rf/reg-event-fx
  :nc-g
  (fn [cofx [_ _]]
    (let [ddrs (str "G" (:ddr-search (:db cofx)))]
      {:db (assoc-in (:db cofx) [:ddr-search] ddrs)
       :fx [[:dispatch [:fetch-dna ddrs]]]})))

(rf/reg-event-fx
  :nc-esc
  (fn [cofx [_ _]]
    (let [ddrs ""]
      {:db (assoc-in (:db cofx) [:ddr-search] ddrs)
       :fx [[:dispatch [:fetch-dna ddrs]]]})))

;; (rf/reg-event-fx
;;   :nc-t
;;   (fn [cofx [_ dna]]
;;     (update-in db [:ddr-search] (fn [s] (str "T" s)))))

;; (rf/reg-event-fx
;;   :nc-c
;;   (fn [cofx [_ dna]]
;;     (update-in db [:ddr-search] (fn [s] (str "C" s)))))

;; (rf/reg-event-fx
;;   :nc-g
;;   (fn [cofx [_ dna]]
;;     (update-in db [:ddr-search] (fn [s] (str "G" s)))))
