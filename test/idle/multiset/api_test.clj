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

#_(deftest difference
  #_(s/exercise-fn `mset/difference)
  #_(gen/generate
     (gen/fmap #(mset/multiset %) (s/gen vector?)))

  #_(gen/generate ((fn [] (gen/fmap #(mset/multiset %) (s/gen vector?)))))
  #_(gen/sample (s/gen ::mset/multiset))
  #_(s/explain ::mset/multiset #mset [1])

  #_(stest/check `mset/difference {::stest/opts {:num-tests 1}})






  )
#_(deftest intersection)
#_(deftest union)
#_(deftest subset?)
#_(deftest product)
