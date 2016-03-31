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

interface UCallableReferenceExpression : UExpression, UResolvable {
    /* Can be null if the qualifier is a type */
    val qualifierExpression: UExpression?

    /* Can be null if the qualifier is an expression */
    val qualifierType: UType?

    val callableName: String

    override fun accept(visitor: UastVisitor) {
        if (visitor.visitCallableReferenceExpression(this)) return
        qualifierExpression?.accept(visitor)
        qualifierType?.accept(visitor)
    }

    override fun logString() = "UCallableReferenceExpression"
    override fun renderString() = buildString {
        qualifierExpression?.let {
            append(it.renderString())
        } ?: qualifierType?.let {
            append(it.name)
        }
        append("::")
        append(callableName)
    }
}