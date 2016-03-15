package foo

/* This tests checks, that lambda fabric invocation is not extracted.

   An example:
       fun foo() {
         val status = "OK"
         run { println(status) }
       }

   It's compiled to something like:
        function foo() {
          var status = "OK";
          run(foo$(status));
        }

        function foo$(status) {
          return function() {
            console.log(status);
          };
        }

   Thus, we need to be sure, that foo$() is not extracted to some temporary var.
 */

// CHECK_NOT_CALLED: max
// CHECK_NOT_CALLED: box$f
// CHECK_NOT_CALLED: box$f_0

inline fun max(getA: ()->Int, b: Int): Int {
    val a = getA()
    log("max($a, $b)")

    if (a > b) return a

    return b
}

fun box(): String {
    val one = 1
    val two = 2
    val three = 3

    val test = max({ fizz(one) }, max({ fizz(two) }, buzz(three)))
    if (test != 3) return "fail1: $test"

    val log = pullLog()
    if (log != "buzz(3);fizz(2);max(2, 3);fizz(1);max(1, 3);") return "fail2: $log"

    return "OK"
}