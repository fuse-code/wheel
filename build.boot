(def project 'thedavidmeister/wheel)
(def version "0.2.0-SNAPSHOT")

(set-env! :source-paths   #{"src"}
          :dependencies   '[[org.clojure/clojure "1.9.0-alpha15"]
                            [org.clojure/clojurescript "1.9.521"]
                            [adzerk/boot-test "RELEASE" :scope "test"]
                            [adzerk/bootlaces "RELEASE" :scope "test"]
                            [crisptrutski/boot-cljs-test "0.2.2-SNAPSHOT" :scope "test"]
                            [adzerk/boot-test "1.1.1" :scope "test"]
                            [hoplon "7.0.1"]

                            ; Other util libs
                            [medley "1.0.0"]

                            ; Strings
                            [funcool/cuerdas "2.0.3"]

                            ; Data validation
                            [prismatic/schema "1.1.6"]

                            ; Routing
                            [bidi "2.1.1"]])

(task-options!
 pom {:project     project
      :version     version
      :description "Don't re-invent it! simple and basic components for clj(s)/hoplon web development."
      :url         "https://github.com/thedavidmeister/wheel"
      :scm         {:url "https://github.com/thedavidmeister/wheel"}})

(deftask build
  "Build and install the project locally."
  []
  (comp (pom) (jar) (install)))

(require '[crisptrutski.boot-cljs-test :refer [test-cljs]]
         '[adzerk.bootlaces :refer :all]
         '[adzerk.boot-test :refer [test]])

(bootlaces! version)

(def cljs-compiler-options {:foreign-libs [
                                           ; ResizeObserver polyfill.
                                           {:file "https://raw.githubusercontent.com/que-etc/resize-observer-polyfill/master/dist/ResizeObserver.js"
                                            :provides ["polyfill.ResizeObserver"]}]
                            :externs ["externs.js"]})

(deftask tests-cljs
  "Run all the CLJS tests"
  [w watch? bool "Watches the filesystem and reruns tests when changes are made."]
  ; Run the JS tests
  (comp
    (if watch?
        (comp
          (watch)
          (speak :theme "woodblock"))
        identity)
    (test-cljs :exit? (not watch?)
               ; :js-env :chrome
               :cljs-opts (-> cljs-compiler-options
                              (merge {:load-tests true}))
               :namespaces [#".*"])))

(defn test-filter-for-wip
 [wip?]
 (if wip?
  '(:wip (meta %))
  '(not (or (:broken (meta %))
            (:wip (meta %))))))

(deftask tests-clj
 "Run all the CLJ tests"
 [w watch? bool "Watches the filesystem and reruns tests when changes are made."
  W wip? bool "true to only run WIP tests. WIP tests will not run if false."]
 (comp
   (if watch?
       (comp
         (watch)
         (speak :theme "woodblock"))
       identity)
   (test
     :filters [(test-filter-for-wip wip?)])))
