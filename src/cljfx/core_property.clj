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

;;============== Deprecated ==============

;  JavaFX Property を扱う関数。
(defn- ^:deprecated properties-fn-old
  "指定 JavaFX UI インスタンスのプロパティ情報を取得する。
   プロパティ名をキーとしたマップを返す。

   マップエントリの構成。
     キー→ :<property-name> Java プロパティ名をベースとした Clojure キーワード。
     値  → :name            プロパティの Java 名称文字列。
            :return-type     プロパティ値のデータ型。
            :flags           宣言時のアクセス修飾子。多分デバッグ時のみ使用。

   エントリの一例:
   :scale-x {:name \"scaleX\", :return-type javafx.beans.property.DoubleProperty, :flags #{:public :final}}

   注意事項:
     impl_XXX や private なプロパティも取得できてしまっているが、使っても多分例外になるので使用しない事。"
  [obj]
  (deprecated)
  (let [base-props
        (->> (:members (r/reflect obj :ancestors true))
             (map #(dissoc % :declaring-class :parameter-types :exception-types))
             (map #(update-in % [:name] str))
             ;(map #(assoc % :return-type-str (str (:return-type %))))
             (map #(if (contains? % :return-type)
                    (update-in % [:return-type] str)
                    %))
             (filter #(re-find #".*Property$" (:name %)))
             (map #(update-in % [:name] (fn [s] (s/replace s #"(.*)Property$" "$1"))))
             (map #(if (contains? % :return-type)
                    (update-in % [:return-type] (fn [s] (Class/forName s)))
                    %)))]
    (zipmap (map (comp keyword camel->dash :name) base-props) base-props)))



(def ^:private ^:deprecated properties-old (memoize properties-fn-old))

;; TODO: ここエラーハンドリングきっちりしときたいが
(defn- clj-invoke
  [target meth & args]
  (deprecated)
  (prn '**clj-invoke** '< target '> meth args)
  (try (clojure.lang.Reflector/invokeInstanceMethod target meth (to-array args))
       (catch Exception e
         (.printStackTrace e)
         (.getMessage e))))
;         (throw (IllegalArgumentException. (str "No matching method: " meth " on "
;                                                (class target))))
;         (clojure.repl/pst))))

(defn- ^:deprecated getter-str-old
  [target prop]
  (deprecated)
  (str (if (isa? (-> (properties-old target) prop :return-type) BooleanExpression) "is" "get")
       (-> (properties-old target) prop :name upper-case-1st)))

(defn ^:deprecated v-old
  "JavaFX UI インスタンスのプロパティ値を取得する。"
  [target prop]
  (deprecated)
  (clj-invoke target (getter-str-old target prop)))

(defn- ^:deprecated setter-str-old
  [target prop]
  (deprecated)
  (str "set" (-> (properties-old target) prop :name upper-case-1st)))

(defn ^:deprecated v!-old
  "JavaFX UI インスタンスのプロパティ値を変更する。"
  ([target prop value]
   (deprecated)
   (clj-invoke target (setter-str-old target prop) value))
  ([target prop value & prop-values]
   {:pre [(even? (count prop-values))]}
   (v!-old target prop value)
   (doseq [pvs (partition 2 prop-values)]
     (v!-old target (first pvs) (second pvs)))))

(defn- ^:deprecated prop-str-old
  [target prop]
  (deprecated)
  (str (-> (properties-old target) prop :name) "Property"))

(defn ^:deprecated p-old
  "JavaFX UI インスタンスのプロパティそのものを取得する。主に bind 用。"
  [target prop]
  (deprecated)
  (clj-invoke target (prop-str-old target prop)))

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