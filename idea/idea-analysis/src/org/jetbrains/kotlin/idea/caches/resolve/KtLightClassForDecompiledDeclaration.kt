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

package org.jetbrains.kotlin.idea.caches.resolve

import com.intellij.psi.*
import com.intellij.psi.impl.compiled.ClsClassImpl
import org.jetbrains.kotlin.asJava.KtLightFieldImpl
import org.jetbrains.kotlin.asJava.KtLightMethodImpl
import org.jetbrains.kotlin.asJava.KtWrappingLightClass
import org.jetbrains.kotlin.asJava.LightMemberOrigin
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.idea.decompiler.classFile.KtClsFile
import org.jetbrains.kotlin.idea.decompiler.textBuilder.DecompiledTextIndexer
import org.jetbrains.kotlin.load.java.JvmAbi
import org.jetbrains.kotlin.load.kotlin.MemberSignature
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOriginKind
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedPropertyDescriptor
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedSimpleFunctionDescriptor
import org.jetbrains.kotlin.serialization.jvm.JvmProtoBuf
import org.jetbrains.kotlin.serialization.jvm.JvmProtoBufUtil

class KtLightClassForDecompiledDeclaration(
        private val clsClass: ClsClassImpl,
        private val origin: KtClassOrObject?,
        private val file: KtClsFile
) : KtWrappingLightClass(clsClass.manager) {
    private val fqName = origin?.fqName ?: FqName(clsClass.qualifiedName)

    override fun copy() = this

    override fun getOwnInnerClasses(): List<PsiClass> {
        val nestedClasses = origin?.declarations?.filterIsInstance<KtClassOrObject>() ?: emptyList()
        return clsClass.ownInnerClasses.map { innerClsClass ->
            KtLightClassForDecompiledDeclaration(innerClsClass as ClsClassImpl,
                                                 nestedClasses.firstOrNull { innerClsClass.name == it.name }, file)
        }
    }

    override fun getOwnFields(): List<PsiField> {
        return delegate.ownFields.map {
            val desc = descForType(it.type)
            val signature = MemberSignature.fromFieldNameAndDesc(it.name!!, desc)
            val declarationBySignature = file.getDeclaration(ByJvmSignatureIndexer, signature)
            KtLightFieldImpl.create(declarationBySignature, it, this)
        }
    }

    override fun getNavigationElement() = origin?.navigationElement ?: super.getNavigationElement()

    override fun getDelegate() = clsClass

    override fun getOrigin() = origin

    override fun getFqName() = fqName

    override fun getOwnMethods(): List<PsiMethod> {
        return delegate.ownMethods.map { KtLightMethodImpl.create(it, LightMemberOriginForCompiledMethod(it, file), this) }
    }

    override fun getParent() = clsClass.parent

    override fun equals(other: Any?): Boolean =
            other is KtLightClassForDecompiledDeclaration &&
            getFqName() == other.getFqName()

    override fun hashCode(): Int =
            getFqName().hashCode()
}

interface LightMemberOriginForCompiled : LightMemberOrigin {
    override val originKind: JvmDeclarationOriginKind
        get() = JvmDeclarationOriginKind.OTHER
}


//TODO_R: USE
data class LightMemberOriginForCompiledField(val field: PsiField, val file: KtClsFile) : LightMemberOriginForCompiled {
    override fun copy(): LightMemberOrigin {
        return LightMemberOriginForCompiledField(field.copy() as PsiField, file)
    }

    override val originalElement: KtDeclaration?
        get() {
            val desc = descForType(this.field.type)
            val signature = MemberSignature.fromFieldNameAndDesc(this.field.name!!, desc)
            val declarationBySignature = file.getDeclaration(ByJvmSignatureIndexer, signature)
            return declarationBySignature
        }
}

data class LightMemberOriginForCompiledMethod(val method: PsiMethod, val file: KtClsFile) : LightMemberOriginForCompiled {
    override val originalElement: KtDeclaration?
        get() {
            val desc = descFromPsiMethod(method)
            val signature = MemberSignature.fromMethodNameAndDesc(method.name, desc)
            val declarationBySignature = file.getDeclaration(ByJvmSignatureIndexer, signature)
            return declarationBySignature
        }

    override fun copy(): LightMemberOrigin {
        return LightMemberOriginForCompiledMethod(method.copy() as PsiMethod, file)
    }
}


private fun descForClass(psiClass: PsiClass): String {
    return "L" + psiClass.qualifiedName!!.replace(".", "/") + ";"
}


private fun descForType(type: PsiType): String = when (type) {
    PsiType.BOOLEAN -> "b" //TODO_R: other primitives
    PsiType.CHAR -> "C"
    PsiType.VOID -> "V"
    PsiType.INT -> "I"
    is PsiClassType -> {
        val resolved = type.resolve()
        when (resolved) {
            is PsiTypeParameter -> {
                val superType = resolved.superTypes.firstOrNull()?.let { return descForType(it) }
                "Ljava/lang/Object;"
            }
            is PsiClass -> {
                descForClass(resolved)
            }
            else -> error("TODO")
        }

    }
    else -> error("TODO")
}

private fun descFromPsiMethod(psiMethod: PsiMethod): String = buildString {


    append("(")
    psiMethod.parameterList.parameters.forEach {
        append(descForType(it.type))
    }
    append(")")

    //TODO_R:
    append(descForType(psiMethod.returnType ?: return ""))
}


object ByJvmSignatureIndexer : DecompiledTextIndexer<MemberSignature> {
    override fun indexDescriptor(descriptor: DeclarationDescriptor): Collection<MemberSignature> {
        val signatures = arrayListOf<MemberSignature>()
        fun saveSignature(signature: MemberSignature) {
            signatures.add(signature)
        }

        if (descriptor is ClassDescriptor) {
            when (descriptor.kind) {
                ClassKind.ENUM_ENTRY -> {
                    val signature = MemberSignature.fromFieldNameAndDesc(descriptor.name.asString(), "L" + (descriptor.containingDeclaration as ClassDescriptor).classId.asSingleFqName().asString().replace(".", "/") + ";")
                    saveSignature(signature)
                    //TODO_R: ....
                }
                ClassKind.OBJECT -> {
                    val signature = MemberSignature.fromFieldNameAndDesc(JvmAbi.INSTANCE_FIELD, "L" + descriptor.classId.asSingleFqName().asString().replace(".", "/") + ";")
                    saveSignature(signature)
                    if (descriptor.isCompanionObject) {
                        val signature = MemberSignature.fromFieldNameAndDesc(descriptor.name.asString(), "L" + descriptor.classId.asSingleFqName().asString().replace(".", "/") + ";")
                        saveSignature(signature)
                    }
                }
            }
        }

        if (descriptor is DeserializedSimpleFunctionDescriptor) {
            JvmProtoBufUtil.getJvmMethodSignature(descriptor.proto, descriptor.nameResolver, descriptor.typeTable)?.let {
                val signature = MemberSignature.fromMethodNameAndDesc(it)
                saveSignature(signature)
            }
        }
        if (descriptor is DeserializedPropertyDescriptor) {
            val proto = descriptor.proto
            if (proto.hasExtension(JvmProtoBuf.propertySignature)) {
                val signature = proto.getExtension(JvmProtoBuf.propertySignature)
                if (signature.hasField()) {
                    val field = signature.field
                    saveSignature(MemberSignature.fromFieldNameAndDesc(descriptor.name.asString(), descriptor.nameResolver.getString(field.desc))) //TODO_R: test this line
                }
                if (signature.hasGetter()) {
                    saveSignature(MemberSignature.fromMethod(descriptor.nameResolver, signature.getter))
                }
                if (signature.hasSetter()) {
                    saveSignature(MemberSignature.fromMethod(descriptor.nameResolver, signature.setter))
                }
                if (signature.hasSyntheticMethod()) {
                    saveSignature(MemberSignature.fromMethod(descriptor.nameResolver, signature.syntheticMethod))
                }
            }
            if (DescriptorUtils.isAnnotationClass(descriptor.containingDeclaration)) {
//                saveSignature(MemberSignature.fromMethodNameAndDesc(descriptor.name.asString(), "()"))
            }
        }
        return signatures
    }
}
