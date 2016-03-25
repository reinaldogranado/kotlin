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

package org.jetbrains.kotlin.idea.quickfix

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticWithParameters3
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifierType

class ExposedVisibilityModifierFix(element: KtDeclaration, val visibility1: EffectiveVisibility, val visibility2: EffectiveVisibility) : KotlinQuickFixAction<KtDeclaration>(element) {
    override fun getFamilyName() = "Replace ${visibility1.name} with ${visibility2.name} for ${element.name}"

    override fun getText() = familyName

    public override fun invoke(project: Project, editor: Editor?, file: KtFile) {
        element.removeModifier(element.visibilityModifierType()!!)
        element.addModifier(KtModifierKeywordToken.keywordModifier(visibility1.name))
    }

    companion object : KotlinSingleIntentionActionFactory() {
        override fun createAction(diagnostic: Diagnostic): KotlinQuickFixAction<KtDeclaration>? {
            val element = diagnostic.psiElement as? KtDeclaration ?: return null
            if (diagnostic is DiagnosticWithParameters3<*, *, *, *>) {
                val visibility1 = diagnostic.a as? EffectiveVisibility
                val visibility2 = diagnostic.c as? EffectiveVisibility
                if (visibility1 != null && visibility2 != null) {
                    return ExposedVisibilityModifierFix(element, visibility1, visibility2)
                }
            }
            return null
        }
    }
}
