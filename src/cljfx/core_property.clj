(in-ns 'cljfx.core)

(import '[javafx.beans.binding BooleanExpression]
        '[javafx.beans.value ObservableValue])
(use 'cljfx.util)
(require '[clojure.reflect :as r]
         '[clojure.string :as s])


;--------------- Utils
(defn- upper-case-1st [s]
  (if s
    (apply str (cons (Character/toUpperCase ^char (first s)) (rest s)))
    str))

;;==========================================
;;================ New Apis ================
;;==========================================
(defn- properties-fn
  [cls]
  (let [base-props
        (->> (r/type-reflect cls :ancestors true)
             :members
             (map #(update-in % [:name] str))
             (filter #(contains? (:flags %) :public))
             (filter #(not (.startsWith ^String (:name %) "impl_")))
             (filter #(re-find #".*Property$" (:name %)))
             (map #(dissoc % :declaring-class :parameter-types :exception-types))
             (map #(update-in % [:name] (fn [s] (s/replace s #"(.*)Property$" "$1"))))
             (map #(update-in % [:return-type] (fn [sym] (Class/forName (str sym))))))
        keyed-props (map (comp keyword camel->dash :name) base-props)]
    (zipmap keyed-props base-props)))

(def ^:private properties (memoize properties-fn))

;------------------ Getter
(defn- getter-str
  [cls prop]
  (str (if (isa? (-> (properties cls) prop :return-type) BooleanExpression) "is" "get")
       (-> (properties cls) prop :name upper-case-1st)))

(defn- getter-fn*
  [^Class cls prop]
  (let [meth (symbol (getter-str cls prop))
        tag (symbol (.getName cls))
        arg0 (gensym)]
    (eval
      `(fn [~(with-meta arg0 {:tag tag})]
         (. ~arg0 ~meth)))))

(def ^:private getter-fn (memoize getter-fn*))

(defn v
  [target prop]
  (let [cls (class target)
        getter (getter-fn cls prop)]
    (getter target)))

;------------------- Setter
(defn- setter-str
  [cls prop]
  (str "set" (-> (properties cls) prop :name upper-case-1st)))

(defn- setter-fn*
  [^Class cls prop]
  (let [meth (symbol (setter-str cls prop))
        tag (symbol (.getName cls))
        arg0 (gensym)
        arg1 (gensym)]
    (eval
      `(fn [~(with-meta arg0 {:tag tag}) ~arg1]
         (. ~arg0 (~meth ~arg1))))))

(def ^:private setter-fn (memoize setter-fn*))

(defn v!
  ([target prop value]
   (let [cls (class target)
         setter (setter-fn cls prop)]
     (setter target value)))
  ([target prop value & prop-values]
   {:pre [(even? (count prop-values))]}
   (v! target prop value)
   (doseq [pvs (partition 2 prop-values)]
     (v! target (first pvs) (second pvs)))))

;------------------- Property
(defn- prop-str
  [^Class cls prop]
  (str (-> (properties cls) prop :name) "Property"))

(defn- prop-fn*
  [^Class cls prop]
  (let [meth (symbol (prop-str cls prop))
        tag (symbol (.getName cls))
        arg0 (gensym)]
    (eval
      `(fn [~(with-meta arg0 {:tag tag})]
         (. ~arg0 ~meth)))))

(def ^:private prop-fn (memoize prop-fn*))

(defn p
  ^"javafx.beans.value.ObservableValue" [target prop]
  (let [cls (class target)
        propfn (prop-fn cls prop)]
    (propfn target)))