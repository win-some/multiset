(ns idle.multiset.api
  (:require [clojure.pprint :as pprint]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            #_[orchestra.spec.test :as st])
  (:import (java.util Collection)))

(defprotocol Multiplicities
  ;; multiplicities is just a frequencies map
  (multiplicities [this]))

(deftype Multiset [^clojure.lang.IPersistentMap meta
                   ^clojure.lang.IPersistentMap multiplicities
                   ^int size]
  clojure.lang.IPersistentSet (get [this k]
                                (if-let [e (find multiplicities k)]
                                  (key e)))
  (contains [this k]
    (boolean (find multiplicities k)))
  (disjoin [this k]
    (if-let [v (get multiplicities k)]
      (Multiset.
       meta
       (if (= 1 v)
         (dissoc multiplicities k)
         (update multiplicities k dec))
       (dec size))
      this)
    )

  clojure.lang.IPersistentCollection
  (cons [this k]
    (Multiset.
     meta
     (update multiplicities k (fnil inc 0))
     (inc size)))
  (empty [this] (with-meta (Multiset. nil {} 0) meta))
  (equiv [this x] (.equals this x))

  clojure.lang.Seqable
  (seq [this]
    (if-let [entry (first (seq multiplicities))]
      (let [k (key entry)]
        (lazy-seq (cons k (.seq (.disjoin this k)))))))

  clojure.lang.Counted
  (count [this] size)

  clojure.lang.IMeta
  (meta [this] meta)

  clojure.lang.IObj
  (withMeta [this m]
    (Multiset. m multiplicities size))

  Object
  (equals [this x]
    (if (instance? Multiset x)
      (.equals multiplicities (.multiplicities ^Multiset x))
      false))
  (hashCode [this]
    (hash-combine (hash multiplicities) Multiset))
  (toString [this] (str "#mset " `[~@(seq this)]))

  clojure.lang.IFn
  (invoke [this k]
    (.get this k))
  (invoke [this k default]
    (if-let [r (.get this k)]
      r
      default))

  java.util.Collection
  (isEmpty [this]
    (zero? size))
  (size [this] size)
  (^objects toArray [this ^objects a]
   (.toArray ^Collection (or (seq this) ()) a))
  (toArray [this]
    (.toArray ^Collection (or (seq this) ())))
  (iterator [this]
    (.iterator ^Collection (or (seq this) ())))
  (containsAll [this coll]
    (.containsAll ^Collection (into #{} this) coll))

  Multiplicities
  (multiplicities [this] multiplicities))

(defn multiset?
  [x]
  (instance? Multiset x))
(s/fdef multiset?
  :args (s/cat :x any?)
  :ret boolean?)

(defmethod print-method Multiset
  [^Multiset mset ^java.io.Writer w]
  (.write w (.toString mset)))

(defmethod pprint/simple-dispatch Multiset
  [^Multiset mset]
  (pr mset))

(defn multiset
  "Create a multiset from a sequence"
  ([]
   (->Multiset nil {} 0))
  ([s]
   (->Multiset nil (frequencies s) (count s))))

(s/def ::multiset (s/with-gen multiset?
                    #(gen/fmap (partial multiset) (s/gen vector?))))

(s/fdef multiset
  :args (s/alt :arity-0 (s/cat)
               :arity-1 (s/cat :items coll?))
  :ret ::multiset)

(defn mset
  "Create a multiset."
  ([]
   (multiset))
  ([& xs]
   (multiset xs)))
(s/fdef mset
  :args (s/cat :items (s/* any?))
  :ret ::multiset)

(defn multiplicity
  "Returns the number of occurences of x, 0 if x is not a member."
  [ms x]
  (get (multiplicities ms) x 0))
(s/fdef multiplicity
  :args (s/cat :ms ::multiset :x any?)
  :ret nat-int?)

(defn multiset-reader
  [x]
  (multiset x))
(s/fdef multiset-reader
  :args (s/cat :x coll?)
  :ret ::multiset)

(defn subset?
  "Returns true if ms1 is a subset of ms2."
  [ms1 ms2]
  (let [a (multiplicities ms1)
        b (multiplicities ms2)]
    (reduce (fn [res x]
              (if res
                (and res (<= (get a x) (get b x 0)))
                (reduced false)))
            true
            ms1)))
(s/fdef subset?
  :args (s/cat :ms1 ::multiset :ms2 ::multiset)
  :ret boolean?)

(comment
  (subset? #mset [1 2 3] #mset [1 1 2 2 ])
  (subset? #mset [1 2 3] #mset [1 1 2 2 3])
  )

(defn union
  "Returns a multiset that is the result of unioning the multisets together."
  ([ms1 ms2]
   (loop [[entry & rest] (multiplicities ms2)
          result ms1]
     (if (seq entry)
       (let [[k v] entry]
         ;; Getting rid of metadata because this is a new collection
         (recur rest (->Multiset nil
                                 (update (multiplicities result) k (fnil (partial + v) 0))
                                 (+ (count result) v))))
       result)))
  ([ms1 ms2 & multisets]
   (reduce union ms1 (clojure.core/conj multisets ms2))))
(s/fdef union
  :args (s/alt :arity-2 (s/cat :ms1 ::multiset :ms2 ::multiset)
               :variadic (s/cat :ms1 ::multiset :ms2 ::multiset :multisets (s/* ::multiset)))
  :ret ::multiset
  ;; each input set must be a subset of the output
  :fn #(let [{:keys [ms1 ms2 multisets]} (-> % :args second)
             inputs (conj (or multisets []) ms1 ms2)
             ret (:ret %)]
         (reduce (fn [acc arg]
                   (if acc
                     (and acc (subset? arg ret))
                     (reduced false)))
                 true
                 inputs)))

(comment
  {:ret #mset [4 1 2 3], :args [:arity-2 {:ms1 #mset [4], :ms2 #mset [1 2 3]}]}
  {:ret #mset [4 1 2 3], :args [:arity-2 {:ms1 #mset [4], :ms2 #mset [1 2 3]}]}
  (union #mset [1] #mset [2] #mset [3] )

)

(defn intersection
  "Returns only the common keys of the multisets."
  ([ms1 ms2]
   (if (< (count ms2) (count ms1))
     (recur ms2 ms1)
     (reduce (fn [result x]
               (if (contains? ms2 x)
                 result
                 (disj result x)))
             ms1
             ms1)))
  ([ms1 ms2 & multisets]
   (let [smallest-first (sort-by count (conj multisets ms1 ms2))]
     (reduce intersection (first smallest-first) (rest smallest-first)))))
(s/fdef intersection
  :args (s/alt :arity-2 (s/cat :ms1 ::multiset :ms2 ::multiset)
               :variadic (s/cat :ms1 ::multiset :ms2 ::multiset :multisets (s/* ::multiset)))
  :ret ::multiset
  ;; the return set must be a subset of each input
  :fn #(let [{:keys [ms1 ms2 multisets]} (-> % :args second)
             inputs (conj (or multisets []) ms1 ms2)
             ret (:ret %)]
         (reduce (fn [acc arg]
                   (if acc
                     (and acc (subset? ret arg))
                     (reduced false)))
                 true
                 inputs)))

(comment
  (count (intersection #mset [1 1 2 3] #mset [1 4]))
  )

(defn difference
  "Returns the keys in ms1 that are not in the following multisets."
  ([ms1 ms2]
   (reduce disj ms1 ms2))
  ([ms1 ms2 & multisets]
   (reduce difference ms1 (conj multisets ms2))))
(defn difference-proof
  "The return set must be a subset of ms1 and not be a subset of any other set (unless
  it is empty)"
  [result]
  (let [{:keys [ms1 ms2 multisets]} (-> result :args second)
        diff-inputs (conj (or multisets []) ms2) ; excluding ms1
        ret (:ret result)]
    (reduce (fn [acc arg]
              (if acc
                (and acc (subset? ret ms1) (or (empty? ret)
                                               (not (subset? ret arg))))
                (reduced false)))
            true
            diff-inputs)))

(s/fdef difference
  :args (s/alt :arity-2 (s/cat :ms1 ::multiset :ms2 ::multiset)
               :variadic (s/cat :ms1 ::multiset :ms2 ::multiset :multisets (s/* ::multiset)))
  :ret ::multiset
  :fn #'difference-proof
  )
(comment

  (difference-proof {:args [:arity-2 {:ms1 #mset [], :ms2 #mset [0]}], :ret #mset []})
  (difference-proof {:args [:arity-2 {:ms1 #mset [], :ms2 #mset []}], :ret #mset []})

  false

  (difference #mset [1 1 2 3] #mset [1 1 3])
  #mset [2]
  #mset [1 2]
  #mset []
  (difference #mset [] #mset [])
  (subset? #mset [] #mset [])
  true
  )

(defn product
  "Returns the cartesian product of two sets."
  [ms1 ms2]
  (multiset (for [a (seq ms1) b (seq ms2)]
              [a b])))
(s/fdef product
  :args (s/cat :ms1 ::multiset :ms2 ::multiset)
  :ret ::multiset)

(comment
  (multiset [1 2 3 1 2 3 nil true false])

  (for [a [1 2] b [:red :white :green]]
    [a b])
  (product (multiset [1 1 2]) (multiset [:red :white :green]))
  #mset [[1 :red] [1 :red] [1 :white] [1 :white] [1 :green] [1 :green] [2 :red] [2 :white] [2 :green]]
  #mset [[1 4] [1 4] [1 4] [1 5] [1 5] [1 5] [1 6] [1 6] [1 6]]



  (union (multiset [1 2 3]) (multiset [4 5 6]))
  (union (multiset [1 2 3]) (multiset [1 4 5])
         (multiset [1 6 7]))



  {1 1, 2 1, 3 1, 4 1, 5 1, 6 1}
  (concat
   {1 5, 3 1}
   {1 3, 3 1})
  ([1 5] [3 1] [1 3] [3 1]))
#_(st/instrument)
