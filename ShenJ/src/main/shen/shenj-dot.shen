(define to-java-part
  Symbol -> (let String (str Symbol)
              (substring (string-length "shenj.dot/") (string-length String) String)))

(define parse-java-call-symbol
  Symbol -> (let Str (to-java-part Symbol)
                 Length (string-length Str)
                 Last-index (- Length 1)
                 Last-char (pos Str Last-index)
              (cond ((= "." Last-char)
                      (@p constructor (substring 0 Last-index Str)))
                    ((= "." (hdstr Str))
                      (if (= "$" Last-char)
                        (@p instance-field (substring 1 Last-index Str))
                        (@p instance-method (substring 1 Length Str))))
                    ((= ".class" (substring (- Length 6) Length Str))
                      (@p class Str))
                    ((= "$" Last-char)
                      (@p static-field (substring 0 Last-index Str)))
                    ((substring? "." Str)
                      (@p static-method Str))
                    (true ())
                    )))

\*
(assert-equals (@p constructor "Font") (parse-java-call-symbol shenj.dot/Font.))
(assert-equals (@p instance-method "setFont") (parse-java-call-symbol shenj.dot/.setFont))
(assert-equals (@p instance-field "first") (parse-java-call-symbol shenj.dot/.first$))
(assert-equals (@p class "Font.class") (parse-java-call-symbol shenj.dot/Font.class))
(assert-equals (@p static-field "Font.BOLD") (parse-java-call-symbol shenj.dot/Font.BOLD$))
(assert-equals (@p static-method "Math.round") (parse-java-call-symbol shenj.dot/Math.round))
*\