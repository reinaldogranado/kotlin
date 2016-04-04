/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.uast

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isBooleanOrNullableBoolean
import org.jetbrains.uast.*

class KotlinUType(
        val type: KotlinType,
        val project: Project,
        override val parent: UElement?
) : UType {
    override val name: String
        get() = type.toString()

    override val fqName: String?
        get() = type.constructor.declarationDescriptor?.fqNameSafe?.asString()

    override val isInt = name == "Int"

    override fun resolve(context: UastContext): UClass? {
        val descriptor = type.constructor.declarationDescriptor ?: return null
        val sourceElement = descriptor.toSource(project) ?: return null
        return context.convert(sourceElement) as? UClass
    }

    override val isBoolean: Boolean
        get() = type.isBooleanOrNullableBoolean()

    //TODO
    override val annotations = emptyList<UAnnotation>()
}

object KotlinUErrorType : UType, NoAnnotations {
    override val isInt = false
    override val parent = null
    override val name = "<error>"
    override val fqName = null
    override val isBoolean = false
    override fun resolve(context: UastContext) = null
}