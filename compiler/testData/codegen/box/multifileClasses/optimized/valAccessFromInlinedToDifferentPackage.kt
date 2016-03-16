// WITH_RUNTIME
// KOTLIN_CONFIGURATION_FLAGS: +JVM.INHERIT_MULTIFILE_PARTS
// FILE: box.kt

import a.*

fun box(): String =
        if (ok {} == 42) "OK" else "hmmm?"

// FILE: part1.kt
@file:[JvmName("MultifileClass") JvmMultifileClass]
package a

val magic1 = 40
const val magic2 = 2

inline fun ok(block: () -> Unit): Int {
    block()
    return magic1 + magic2
}