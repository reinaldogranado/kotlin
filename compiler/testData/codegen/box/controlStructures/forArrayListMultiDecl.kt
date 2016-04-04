// WITH_RUNTIME
val alist = arrayListOf(1 to 2, 2 to 3, 3 to 4)


fun main(args: Array<String>) {
    for ((i, z) in alist) { // LOCALVARIABLE i I L4 L10 1
        println(i)
    }
}

fun box(): String {
    var result = 0
    for ((i, z) in alist) { // LOCALVARIABLE i I L4 L10 1

        result += i + z
    }

    return if (result == 15) "OK" else "fail: $result"
}