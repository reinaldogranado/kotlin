// WITH_RUNTIME
fun foo(list: List<String>): String? {
    <caret>for (s in list) {
        if (s.isEmpty()) continue
        if (s.length < 10 && s != "abc") {
            if (s == "def") continue
            return s
        }
    }
    return null
}