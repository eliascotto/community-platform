(ns socn.routes.common
  (:require [clojure.string :as string]
            [socn.layout :as layout]
            [socn.views.common :as common]
            [socn.views.login :as login]
            [socn.views.home :as home]
            [socn.views.item :as item]
            [socn.views.user :as user]
            [socn.views.edit :as edit]
            [socn.views.reply :as reply]
            [socn.views.submit :as submit]))

(defn with-template
  "Render the view using the default page template.
  Includes a parameter :req into the view args."
  [req page-name & args]
  (let [ext-args    (concat args (list :req req))
        view-render (case page-name
                      "home"   (apply home/view   ext-args)
                      "login"  (apply login/view  ext-args)
                      "submit" (apply submit/view ext-args)
                      "item"   (apply item/view   ext-args)
                      "user"   (apply user/view   ext-args)
                      "edit"   (apply edit/view   ext-args)
                      "reply"  (apply reply/view  ext-args))]
    (when view-render
      (string/join [(common/navbar req page-name)
                    view-render
                    (common/footer)]))))

(defn default-page
  "Default page template for the entire app."
  [req route & args]
  (layout/render-page
   "home.html"
   {:content (apply with-template req route args)}))
