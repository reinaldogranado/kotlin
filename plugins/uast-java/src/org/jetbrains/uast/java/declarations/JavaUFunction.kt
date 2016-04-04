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
package org.jetbrains.uast.java

import com.intellij.psi.PsiMethod
import org.jetbrains.uast.*
import org.jetbrains.uast.psi.PsiElementBacked

class JavaUFunction(
        override val psi: PsiMethod,
        override val parent: UElement
) : JavaAbstractUElement(), UFunction, PsiElementBacked {
    override val kind: UastFunctionKind
        get() = if (psi.isConstructor) UastFunctionKind.CONSTRUCTOR else UastFunctionKind.FUNCTION

    override val name: String
        get() = if (psi.isConstructor) "<init>" else psi.name

    override val nameElement by lz { JavaDumbUElement(psi.nameIdentifier, this) }

    override val valueParameters by lz { psi.parameterList.parameters.map { JavaConverter.convert(it, this) } }

    override val valueParameterCount: Int
        get() = psi.parameterList.parametersCount

    override val typeParameters by lz { psi.typeParameters.map { JavaConverter.convert(it, this) } }

    override val typeParameterCount: Int
        get() = psi.typeParameters.size

    override val returnType by lz { psi.returnType?.let { JavaConverter.convert(it, this) } }

    override fun hasModifier(modifier: UastModifier) = psi.hasModifier(modifier)

    val thrownExceptions: List<UType> by lz {
        psi.throwsList.referencedTypes.map { JavaConverter.convert(it, this) }
    }

    override val annotations by lz { psi.modifierList.getAnnotations(this) }

    override val visibility: UastVisibility
        get() = psi.getVisibility()

    override val body by lz { JavaConverter.convertOrEmpty(psi.body, this) }

    override fun getSuperFunctions(context: UastContext): List<UFunction> {
        return psi.findSuperMethods().map { context.convert(it) as? UFunction }.filterNotNull()
    }
}