package foo

enum class Foo {
    A;

    companion object {
        val a = A
    }
}

fun box(): String {
    assertEquals("A", Foo.a.name)
    return "OK"
}