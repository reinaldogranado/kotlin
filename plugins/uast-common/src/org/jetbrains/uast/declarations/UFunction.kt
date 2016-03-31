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

import org.jetbrains.uast.visitor.UastVisitor

interface UFunction : UDeclaration, UModifierOwner, UAnnotated {
    val kind: UastFunctionKind
    val valueParameters: List<UVariable>
    val valueParameterCount: Int
    val typeParameters: List<UTypeReference>
    val typeParameterCount: Int
    val returnType: UType?
    val body: UExpression
    val visibility: UastVisibility

    open val bytecodeDescriptor: String?
        get() = null

    fun getSuperFunctions(context: UastContext): List<UFunction>

    override fun accept(visitor: UastVisitor) {
        if (visitor.visitFunction(this)) return
        nameElement?.accept(visitor)
        valueParameters.acceptList(visitor)
        body.accept(visitor)
        annotations.acceptList(visitor)
        typeParameters.acceptList(visitor)
        returnType?.accept(visitor)
    }

    override fun renderString(): String = buildString {
        appendWithSpace(visibility.name)
        appendWithSpace(renderModifiers())
        append("fun ")
        if (typeParameterCount > 0) {
            append('<').append(typeParameters.joinToString { it.renderString() }).append("> ")
        }
        append(name)
        append('(')
        append(valueParameters.joinToString() { it.renderString() })
        append(')')
        returnType?.let { append(": " + it.renderString()) }

        val body = when (body) {
            is UBlockExpression -> " " + body.renderString()
            else -> " = " + body.renderString()
        }
        append(body)
    }

    override fun logString() = "UFunction ($name, kind = ${kind.text}, " +
            "paramCount = $valueParameterCount)\n" + body.logString().withMargin
}

object UFunctionNotResolved : UFunction {
    override val kind = UastFunctionKind(ERROR_NAME)
    override val valueParameters = emptyList<UVariable>()
    override val valueParameterCount = 0
    override val typeParameters = emptyList<UTypeReference>()
    override val typeParameterCount = 0
    override val returnType = null
    override val body = EmptyExpression(this)
    override val visibility = UastVisibility.PRIVATE
    override val nameElement = null
    override val parent = null
    override val name = ERROR_NAME

    override fun hasModifier(modifier: UastModifier) = false
    override fun getSuperFunctions(context: UastContext) = emptyList<UFunction>()
    override val annotations = emptyList<UAnnotation>()
}