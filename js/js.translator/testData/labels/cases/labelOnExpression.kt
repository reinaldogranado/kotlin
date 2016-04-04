// CHECK_LABELS_COUNT: function=test0 count=0

package foo

fun <R> myRun(f: () -> R) = f()

fun test0() {
    val a = aa@ 1

    assertEquals(1, a)
    assertEquals(3, l1@ a + l2@ 2)

    val b = bb@ if (true) t@ "then block" else e@ "else block"

    assertEquals("then block", b)
}

class Foo {
    inline fun iter(body: ()->Boolean) {
        for (i in 0 .. 10) {
            if (!body()) break
        }
    }
}

fun box(): String {
    test0()

    return "OK"
}
