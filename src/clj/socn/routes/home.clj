(ns socn.routes.home
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [socn.db.core :as db]
            [socn.middleware :as middleware]
            [ring.util.response :refer [redirect]]
            [socn.routes.common :refer [default-page]]
            [socn.config :refer [env]]
            [socn.session :as session]
            [socn.utils :as utils]
            [socn.controllers.core :as controller]
            [socn.controllers.items :as item-controller]))

(defn- item-vote
  "Return a new map with the boolean key :vote
  if the user has voted."
  [item user]
  (assoc item :voted (controller/exists? "vote" {:author user
                                                 :item (:id item)})))

(defn- items-vote
  "Assignt the key :vote for all the items if
  the user is authenticated."
  [items req]
  (if (session/authenticated? req)
    (let [user (session/auth :id req)]
      (map #(item-vote % user) items))
    items))

(defn home-page [req]
  (let [{{:keys [site]} :params} req
        page-size (:items-per-page env)
        items     (if (s/valid? :socn.validations/domain site)
                    (db/get-items-with-comments-by-domain {:domain site
                                                           :offset 0
                                                           :limit  page-size})
                    (db/get-items-with-comments {:offset 0
                                                 :limit  page-size}))
        items     (items-vote items req)]
    (default-page req "home" :items items)))

(defn item-page [req]
  (let [{{:keys [id]} :params} req
        user (session/auth :id req)]
    (if (string/blank? id)
      (redirect "/")
      (try
        (let [id        (utils/parse-int id)
              item      (db/get-item {:id id})
              comments  (db/get-comments-by-item {:item id :offset 0
                                                  :limit  100})
              sorted        (item-controller/sort-comments comments)
              comments-vote (items-vote sorted req)]
          (default-page req "item"
            :item     (item-vote item user)
            :comments comments-vote))
        (catch Exception _
          (when-not (:dev env)
            (redirect "/")))))))

(defn user-page [req]
  (let [{{:keys [id]}       :params
         {:keys [identity]} :session} req]
    (if (string/blank? id)
      (redirect "/")
      (let [is-user (= id (:id identity))
            user (if is-user
                   identity
                   (dissoc (controller/get "user" {:id id}) :password))]
        (default-page req "user" :user user :is-user is-user)))))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/"       {:get home-page}]
   ["/news"   {:get home-page}]
   ["/item"   {:get item-page}]
   ["/user"   {:get user-page}]])

