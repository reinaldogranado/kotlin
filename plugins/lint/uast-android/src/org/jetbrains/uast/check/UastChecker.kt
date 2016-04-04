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
package org.jetbrains.uast.check

import com.android.tools.klint.detector.api.Issue
import com.android.tools.klint.detector.api.JavaContext
import com.android.tools.klint.detector.api.Location
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import org.jetbrains.uast.*
import org.jetbrains.uast.UastCallKind.Companion.CONSTRUCTOR_CALL
import org.jetbrains.uast.UastCallKind.Companion.FUNCTION_CALL
import org.jetbrains.uast.java.JavaUastLanguagePlugin
import org.jetbrains.uast.visitor.UastVisitor
import java.io.File

interface UastAndroidContext : UastContext {
    val lintContext: JavaContext
    fun report(issue: Issue, element: UElement, location: Location?, message: String)

    fun getLocation(element: UElement?) = element?.getLocation()
}

object UastChecker {
    fun checkWithCustomHandler(
            project: Project,
            file: File,
            context: UastAndroidContext,
            visitor: UastVisitor) {
        check(project, file, context, UastCallback { visitor.handle(it) })
    }

    fun check(project: Project, file: File, context: UastAndroidContext, callback: UastCallback) {
        val vfile = VirtualFileManager.getInstance().findFileByUrl("file://" + file.absolutePath) ?: return

        val plugins = context.languagePlugins
        val additionalCheckers = plugins.fold(mutableListOf<UastAdditionalChecker>()) { list, plugin ->
            for (checker in plugin.additionalCheckers) {
                if (checker is AndroidUastAdditionalChecker) list += checker
            }
            list
        }

        val handlerWrapper = CallbackWrapper(callback, additionalCheckers, context)

        ApplicationManager.getApplication().runReadAction {
            val psiFile = PsiManager.getInstance(project).findFile(vfile)

            if (psiFile != null) {
                when (psiFile) {
                    is PsiJavaFile -> {
                        val ufile = JavaUastLanguagePlugin.converter.convertWithParent(psiFile)
                        ufile?.handleTraverse(handlerWrapper)
                    }
                    else -> for (plugin in plugins) {
                        val ufile = plugin.converter.convertWithParent(psiFile)
                        if (ufile != null) {
                            ufile.handleTraverse(handlerWrapper)
                            break
                        }
                    }
                }
            }
        }
    }

    private class CallbackWrapper(
            val original: UastCallback,
            val additionalCheckers: List<UastAdditionalChecker>,
            val context: UastAndroidContext
    ) : UastCallback {
        override fun invoke(element: UElement) {
            original(element)
            for (checker in additionalCheckers) {
                checker(element, this, context)
            }
        }
    }

    fun check(project: Project, file: File, scanner: UastScanner, context: UastAndroidContext) {
        val applicableFunctionNames = scanner.applicableFunctionNames ?: emptyList()
        val applicableSuperClasses = scanner.applicableSuperClasses ?: emptyList()
        val applicableConstructorTypes = scanner.applicableConstructorTypes ?: emptyList()

        val appliesToResourcesRefs = scanner.appliesToResourceRefs()

        var callback: UastCallback?
        callback = UastCallback { element ->
            when (element) {
                is UCallExpression -> {
                    if (applicableFunctionNames.isNotEmpty()) {
                        if (element.kind == FUNCTION_CALL && element.functionName in applicableFunctionNames) {
                            scanner.visitFunctionCall(context, element)
                        }
                    }

                    if (applicableConstructorTypes.isNotEmpty()) {
                        if (element.kind == CONSTRUCTOR_CALL) {
                            element.resolve(context)?.let { constructor ->
                                if (constructor.getContainingClass()?.fqName in applicableConstructorTypes) {
                                    scanner.visitConstructor(context, element, constructor)
                                }
                            }
                        }
                    }
                }
                is UClass -> if (applicableSuperClasses.isNotEmpty()) {
                    if (applicableSuperClasses.any { element.isSubclassOf(it) }) {
                        scanner.visitClass(context, element)
                    }
                }
                is UQualifiedExpression -> {
                    if (appliesToResourcesRefs && element.receiver is UQualifiedExpression) {
                        val parentQualifiedExpr = element.receiver as UQualifiedExpression
                        val resourceName = element.selector
                        val resourceType = parentQualifiedExpr.selector
                        val receiver = parentQualifiedExpr.receiver

                        val receiverIsResourceClass = when (receiver) {
                            is USimpleReferenceExpression -> receiver.identifier == "R"
                            is UQualifiedExpression -> receiver.selectorMatches("R")
                            else -> false
                        }

                        if (resourceName is USimpleReferenceExpression && resourceType is USimpleReferenceExpression
                                && receiverIsResourceClass && receiver is UResolvable) {
                            val resolvedReceiver = receiver.resolve(context)
                            val isFramework = (resolvedReceiver as? UClass)?.matchesFqName("android.R") ?: false

                            scanner.visitResourceReference(context, element, resourceType.identifier, resourceName.identifier, isFramework)
                        }
                    }
                }
            }
        }

        check(project, file, context, callback)
    }

}