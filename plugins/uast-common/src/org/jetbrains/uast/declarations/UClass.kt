/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.uast

import org.jetbrains.uast.kinds.UastClassKind

interface UClass : UDeclaration, UFqNamed, UModifierOwner, UAnnotated {
    /* The simple class name is only for the debug purposes. Do not check against it in the production code */
    override val name: String

    val isAnonymous: Boolean
    val visibility: UastVisibility
    val kind: UastClassKind
    val defaultType: UType

    val companions: List<UClass>

    open val internalName: String?
        get() = null

    val superTypes: List<UType>
    val declarations: List<UDeclaration>

    val nestedClasses: List<UClass>
        get() = declarations.filterIsInstance<UClass>()

    val functions: List<UFunction>
        get() = declarations.filterIsInstance<UFunction>()

    val properties: List<UVariable>
        get() = declarations.filterIsInstance<UVariable>()

    @Suppress("UNCHECKED_CAST")
    val constructors: List<UFunction>
        get() = declarations.filter { it is UFunction && it.kind == UastFunctionKind.CONSTRUCTOR } as List<UFunction>

    fun isSubclassOf(fqName: String) : Boolean

    fun getSuperClass(context: UastContext): UClass?

    override fun traverse(callback: UastCallback) {
        nameElement?.handleTraverse(callback)
        declarations.handleTraverseList(callback)
        annotations.handleTraverseList(callback)
    }

    override fun renderString(): String {
        val modifiers = listOf(UastModifier.ABSTRACT, UastModifier.FINAL, UastModifier.STATIC)
                .filter { hasModifier(it) }.joinToString(" ") { it.name }.let { if (it.isBlank()) it else "$it " }

        val name = if (isAnonymous) "" else " $name"

        val declarations = if (declarations.isEmpty()) "" else buildString {
            appendln("{")
            append(declarations.joinToString("\n") { it.renderString() }.withMargin)
            append("\n}")
        }

        return "${visibility.name} " + modifiers + kind.text + name + " " + declarations
    }

    override fun logString() = "UClass ($name, kind = ${kind.text})\n" + declarations.logString()
}

object UClassNotResolved : UClass {
    override val isAnonymous = true
    override val kind = UastClassKind.CLASS
    override val visibility = UastVisibility.PRIVATE
    override val superTypes = emptyList<UType>()
    override val declarations = emptyList<UDeclaration>()
    override fun isSubclassOf(fqName: String) = false
    override val companions = emptyList<UClass>()
    override val defaultType = UastErrorType
    override val nameElement = null
    override val parent = null
    override val name = "<class not resolved>"
    override val fqName = null
    override val internalName = null

    override fun hasModifier(modifier: UastModifier) = false
    override val annotations = emptyList<UAnnotation>()

    override fun getSuperClass(context: UastContext) = null
}