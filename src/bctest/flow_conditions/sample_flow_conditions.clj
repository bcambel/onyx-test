(ns bctest.flow-conditions.sample-flow-conditions)

(def flow-conditions
  [
  ; {:flow/from :parse-line
  ;   :flow/to [:parse-data]
  ;   :flow/short-circuit? true
  ;   :flow/thrown-exception? true
  ;   :flow/post-transform :bctest.flow-conditions.sample-flow-conditions/substitute-segment
  ;   :flow/predicate :bctest.flow-conditions.sample-flow-conditions/npe?
  ;   :flow/doc "Send a canned value if this segment threw a NullPointerException."}

   ; {:flow/from :parse-data
   ;  :flow/to [:selector2]
   ;  :param/disallow-char \B
   ;  :param/max-line-length 6000
   ;  :flow/predicate [:and
   ;                   [:not [:bctest.flow-conditions.sample-flow-conditions/starts-with? :param/disallow-char]]
   ;                   [:bctest.flow-conditions.sample-flow-conditions/within-length? :param/max-line-length]]
   ;  :flow/doc "Output the line if it doesn't start with 'B' and is less than 60 characters"}
   ])

(defn starts-with? [event old {:keys [line]} all-new disallowed-char]
  (= (first line) disallowed-char))

(defn within-length? [event old {:keys [line]} all-new max-length]
  (<= (count line) max-length))

(defn npe? [event old ex all-new]
  (= (class ex) java.lang.NullPointerException))

(defn substitute-segment [event ex]
  {:line "<<< Blank line was here >>>"})

