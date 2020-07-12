# multiset

A simple multiset/bag implementation for Clojure.

## Usage

Currently a work in progress, so if you want to use it you'll need to clone it yourself
and add this to your deps.edn:

`idle/multiset {:local/root "/home/me/path/to/repo"}``

### Example usage

#### Define some multisets
```clojure
user=> (require '[idle.multiset.api :as mset])
;; => nil
user=> (def a (mset/multiset [1 1 2 3]))
;; => #'user/a
user=> (def b (mset/multiset [3 4 5]))
;; => #'user/b
;; you can use the reader literal to create it too
user=> (def c #mset [1 2 4 7])
;; => #'user/c
```
#### Basic functionality
```clojure
user=> a
;; => #mset [1 1 2 3]
user=> (contains? a 4)
;; => false
user=> (contains? a 2)
;; => true
user=> (disj a 1)
;; => #mset [1 2 3]
user=> (conj a 4)
;; => #mset [1 1 2 3 5]
user=> (a 2)
;; => 2
user=> (a "not-a-member" :default)
;; => :default
user=> (seq a)
;; => (1 1 2 3)
```
#### Multiset-specific stuff
```clojure
user=> (mset/multiset? a)
true
user=> (mset/multiplicities a)
{1 2, 2 1, 3 1}
user=> (mset/multiplicity a 5)
0
user=> (mset/multiplicity a 1)
2
```
#### Multiset operators
```clojure
user=> (mset/union #mset [1] #mset [2] #mset [1])
#mset [1 1 2]
user=> (mset/intersection #mset [1 2 3] #mset [3])
#mset [3]
user=> (mset/difference #mset [1 1 2] #mset [1 3])
#mset [1 2]
user=> (mset/difference #mset [1 1 2] #mset [1 3] #mset [1])
#mset [2]
user=> (mset/product #mset [1 2 3] #mset [:red :white :green])
#mset [[2 :green] [1 :red] [2 :white] [1 :green] [3 :red] [3 :white] [3 :green] [1 :white] [2 :red]]
user=> (mset/subset? #mset [] #mset [])
true
user=> (mset/subset? #mset [1] #mset [1 1 2])
true
user=> (mset/subset? #mset [3] #mset [4])
false
```

## License

Copyright (C) 2012–2015 winsome and Achim Passen and
[contributors](https://github.com/achim/multiset/graphs/contributors).

Distributed under the Eclipse Public License. See COPYING.
