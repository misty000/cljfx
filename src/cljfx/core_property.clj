(in-ns 'cljfx.core)

(import javafx.beans.binding.BooleanExpression)
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
(defn- properties-fn-old
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
  (binding [*out* *err*] (println "the function was deprecated"))
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



(def ^:private properties-old (memoize properties-fn-old))

;; TODO: ここエラーハンドリングきっちりしときたいが
(defn- clj-invoke
  [target meth & args]
  (binding [*out* *err*] (println "the function was deprecated"))
  (prn '**clj-invoke** '< target '> meth args)
  (try (clojure.lang.Reflector/invokeInstanceMethod target meth (to-array args))
       (catch Exception e
         (.printStackTrace e)
         (.getMessage e))))
;         (throw (IllegalArgumentException. (str "No matching method: " meth " on "
;                                                (class target))))
;         (clojure.repl/pst))))

(defn- getter-str-old
  [target prop]
  (binding [*out* *err*] (println "the function was deprecated"))
  (str (if (isa? (-> (properties-old target) prop :return-type) BooleanExpression) "is" "get")
       (-> (properties-old target) prop :name upper-case-1st)))

(defn v-old
  "JavaFX UI インスタンスのプロパティ値を取得する。"
  [target prop]
  (binding [*out* *err*] (println "the function was deprecated"))
  (clj-invoke target (getter-str-old target prop)))

(defn- setter-str-old
  [target prop]
  (binding [*out* *err*] (println "the function was deprecated"))
  (str "set" (-> (properties-old target) prop :name upper-case-1st)))

(defn v!-old
  "JavaFX UI インスタンスのプロパティ値を変更する。"
  ([target prop value]
   (binding [*out* *err*] (println "the function was deprecated"))
   (clj-invoke target (setter-str-old target prop) value))
  ([target prop value & prop-values]
   {:pre [(even? (count prop-values))]}
   (v!-old target prop value)
   (doseq [pvs (partition 2 prop-values)]
     (v!-old target (first pvs) (second pvs)))))

(defn- prop-str-old
  [target prop]
  (binding [*out* *err*] (println "the function was deprecated"))
  (str (-> (properties-old target) prop :name) "Property"))

(defn p-old
  "JavaFX UI インスタンスのプロパティそのものを取得する。主に bind 用。"
  [target prop]
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
  (println "getter-fn*")
  (let [meth (symbol (getter-str cls prop))
        tag (symbol (.getName cls))
        arg0 (gensym)]
    (eval
      `(fn [~(with-meta arg0 {:tag tag})]
         (. ~arg0 ~meth)))))

(def ^:private getter-fn (memoize getter-fn*))

(defn v [target prop]
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