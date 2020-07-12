(ns idle.multiset.api-test
  (:require [idle.multiset.api :as mset]
            [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest]
            [orchestra.spec.test :as st]
            [expectations.clojure.test :as e]))

(deftest multiset
  (testing "a multiset can contain the same value multiple times"
    (e/expect #mset [1 1 2 2 3 3]
              (mset/multiset [1 1 2 2 3 3])))
  (testing "you can add a value to a multiset"
    (e/expect #mset [1]
              (conj (mset/multiset) 1)))
  (testing "you can remove a value from a multiset"
    (e/expect #mset [1]
              (disj (mset/multiset [1 1]) 1)))
  (testing "you can count a multiset"
    (e/expect 3
              (count (mset/multiset [1 1 1]))))
  (testing "you can get a key from a multiset, nil if it's not there, default if supplied"
    (e/expect 1
              (get (mset/multiset [1 1 1]) 1))
    (e/expect nil
              (get (mset/multiset [1 1 1]) 0))
    (e/expect :default
              (get (mset/multiset [1 1 1]) 0 :default)))
  (testing "you can invoke a multiset"
    (e/expect 1
              ((mset/multiset [1 1 1]) 1)))
  (testing "you can seq a multiset"
    (e/expect [1 1 1]
              (seq (mset/multiset [1 1 1]))))
  (testing "you can empty a multiset"
    (e/expect #mset []
              (empty (mset/multiset [1 1 1]))))
  (testing "you can compare multisets"
    (e/expect true
              (= (mset/multiset [1 1 1])
                 (mset/multiset [1 1 1])))
    (e/expect false
              (= (mset/multiset [1 1])
                 (mset/multiset [1 1 1]))))
  (testing "you can attach metadata to multisets"
    (e/expect {:foo :bar}
              (meta (with-meta (mset/multiset [1 1 1]) {:foo :bar})))))

(deftest mset
  (testing "mset can take un-contained values"
    (e/expect #mset [1 1 2 3]
              (mset/mset 1 2 3 1))))

(deftest multiset?
  (testing "multiset? can tell multisets from not-multisets"
    (e/expect true
              (mset/multiset? (mset/multiset [1])))
    (e/expect false
              (mset/multiset? {}))))

(deftest mset-tag
  (testing "the mset tagged literal can create a multiset from a sequence"
    (e/expect #mset [1 1 2 2 3 3]
              #mset [3 2 1 3 2 1])))

(deftest union
  (testing "unioning empty sets gives an empty set"
    (e/expect #mset []
              (mset/union #mset [] #mset [])))
  (testing "unioning sets gives union of sets"
    (e/expect #mset [1 1 1 2 3 4]
              (mset/union #mset [1 1 2] #mset [1 3 4])))
  (testing "unioning n sets gives union of sets"
    (e/expect #mset [1 1 2 3]
              (mset/union #mset [1] #mset [1] #mset [2] #mset [3]))))

(deftest subset?
  (testing "empty sets are subsets of everything"
    (e/expect true
              (mset/subset? #mset [] #mset []))
    (e/expect true
              (mset/subset? #mset [] #mset [1 2 3 4])))
  (testing "disjoint sets are not subsets"
    (e/expect false
              (mset/subset? #mset [1 2 3 4] #mset []))
    (e/expect false
              (mset/subset? #mset [1 1] #mset [1]))))

(deftest intersection
  (testing "intersection returns only the common elements of all sets"
    (e/expect #mset [1]
              (mset/intersection #mset [1 2] #mset [1 3]))
    (e/expect #mset [1]
              (mset/intersection #mset [1 2] #mset [1 3] #mset [1 1]))
    (e/expect #mset []
              (mset/intersection #mset [1 2] #mset [1 3] #mset []))))


(deftest difference
  (testing "returns the keys in ms1 that are not in the following sets"
    (e/expect #mset [1]
              (mset/difference #mset [1 1 2] #mset [1 2 3] #mset []))
    (e/expect #mset []
              (mset/difference #mset [] #mset [1 2 3] #mset [1 1 2]))))

(deftest product
  (testing "returns the cartesian product of the given two sets"
    (e/expect #mset [[1 :green] [1 :green] [1 :red] [1 :red]
                     [1 :white] [1 :white] [3 :green] [3 :red] [3 :white]]
              (mset/product #mset [1 1 3] #mset [:red :white :green]))))

(comment
    #_(s/exercise-fn `mset/difference)
  #_(gen/generate
     (gen/fmap #(mset/multiset %) (s/gen vector?)))

  #_(gen/generate ((fn [] (gen/fmap #(mset/multiset %) (s/gen vector?)))))
  #_(gen/sample (s/gen ::mset/multiset))
  #_(s/explain ::mset/multiset #mset [1])

  #_(stest/check `mset/difference {::stest/opts {:num-tests 1}})

  )
