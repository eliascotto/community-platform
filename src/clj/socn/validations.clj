(ns socn.validations
  (:require [clojure.spec.alpha :as s]))

;;=======================
;; Regex
;;=======================

;; source: https://emailregex.com/
(def re-email #"(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])")

;; source: https://stackoverflow.com/a/26987741/15232490
(def re-domain #"^(((?!-))(xn--|_{1,1})?[a-z0-9-]{0,61}[a-z0-9]{1,1}\.)*(xn--)?([a-z0-9][a-z0-9\-]{0,60}|[a-z0-9-]{1,30}\.[a-z]{2})$")

(def re-int #"^\d+$")

;; source: https://regexr.com/39nr7
(def re-url #"[(http(s)?):\/\/(www\.)?a-zA-Z0-9@:%._\+~#=]{2,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\+.~#?&//=]*)")

;;=======================
;; Validation specs
;;=======================

;; Use them like (s/valid? :socn.validation/domain "wikipedia.org")
(s/def ::domain (s/and string? #(re-matches re-domain %)))
(s/def ::email  (s/and string? #(re-matches re-email %)))
(s/def ::url    (s/and string? #(re-matches re-url %)))
(s/def ::item   (s/and string? #(re-matches re-int %)))
(s/def ::type   (s/and char? (s/or :item    #(= % \i)
                                   :comment #(= % \c))))


(s/def :user/id       (s/and string? #(<= 2 (count %) 18)))
(s/def :user/email    (s/or :empty empty? :email ::email))
(s/def :user/about    string?)
(s/def :user/password string?)
(s/def :user/created  inst?)
(s/def :user/karma    pos-int?)
(s/def :user/showall  boolean?)
(s/def :user/get
  (s/keys :req-un [:user/id]))
(s/def :user/create!
  (s/keys
   :req-un [:user/id
            :user/password
            :user/created
            :user/karma
            :user/showall]))
(s/def :user/update!
  (s/keys
   :req-un [:user/id
            :user/email
            :user/about
            :user/showall]))

(s/def :comment/id        int?)
(s/def :comment/author    string?)
(s/def :comment/item      int?)
(s/def :comment/content   string?)
(s/def :comment/score     int?)
(s/def :comment/parent    (s/or :nil nil? :parent int?))
(s/def :comment/submitted inst?)
(s/def :comment/edited    inst?)
(s/def :comment/get
  (s/keys :req-un [:comment/id]))
(s/def :comment/create!
  (s/keys
   :req-un [:comment/author
            :comment/item
            :comment/content
            :comment/score
            :comment/parent
            :comment/submitted]))
(s/def :comment/update!
  (s/keys
   :req-un [:comment/id
            :comment/content
            :comment/edited]))
(s/def :comment/delete
  (s/keys :req-un [:comment/id]))


(s/def :item/id        int?)
(s/def :item/author    string?)
(s/def :item/score     int?)
(s/def :item/content   string?)
(s/def :item/title     string?)
(s/def :item/url       ::url)
(s/def :item/domain    ::domain)
(s/def :item/submitted inst?)
(s/def :item/edited    inst?)
(s/def :item/get
  (s/keys :req-un [:item/id]))
(s/def :item/create!
  (s/keys
   :req-un [:item/author
            :item/score
            :item/title
            :item/domain
            :item/submitted]
   :opt-un [:item/content
            :item/url]))
(s/def :item/update!
  (s/keys
   :req-un [:item/id
            :item/content
            :item/edited]))
(s/def :item/delete
  (s/keys :req-un [:item/id]))


(s/def :vote/author    string?)
(s/def :vote/item      int?)
(s/def :vote/type      ::type)
(s/def :vote/submitted inst?)
(s/def :vote/direction
  (s/and string? (s/or :up   #(= % "up")
                       :down #(= % "down"))))
(s/def :vote/create!
  (s/keys
   :req-un [:vote/author
            :vote/item
            :vote/submitted
            :vote/type]))
(s/def :vote/delete!
  (s/keys
   :req-un [:vote/author
            :vote/item]))
(s/def :vote/exists?
  (s/keys
   :req-un [:vote/author
            :vote/item]))