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

package org.jetbrains.kotlin.codegen

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.types.KotlinType

class ErrorTypeEncounteredException(
        type: KotlinType, descriptor: DeclarationDescriptor
) : IllegalStateException(generateExceptionMessage(type, descriptor)) {
    companion object {
        private fun generateExceptionMessage(type: KotlinType, descriptor: DeclarationDescriptor): String {
            val declarationElement = DescriptorToSourceUtils.descriptorToDeclaration(descriptor)
                                     ?: return String.format("Error type encountered: %s (%s).", type, type.javaClass.simpleName)

            val containingDeclaration = descriptor.containingDeclaration
            val parentDeclarationElement =
                    if (containingDeclaration != null) DescriptorToSourceUtils.descriptorToDeclaration(containingDeclaration) else null

            return String.format(
                    "Error type encountered: %s (%s). Descriptor: %s. For declaration %s:%s in %s:%s",
                    type,
                    type.javaClass.simpleName,
                    descriptor,
                    declarationElement,
                    declarationElement.text,
                    parentDeclarationElement,
                    if (parentDeclarationElement != null) parentDeclarationElement.text else "null"
            )
        }
    }
}
