/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.platform

import org.jetbrains.kotlin.builtins.BuiltInsInitializer
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.PackageFragmentProvider
import org.jetbrains.kotlin.load.kotlin.BuiltInClassesAreSerializableOnJvm
import org.jetbrains.kotlin.serialization.deserialization.AdditionalSupertypes
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor
import org.jetbrains.kotlin.types.KotlinType
import javax.inject.Inject

class JvmBuiltIns private constructor() : KotlinBuiltIns() {
    companion object {
        private val initializer = BuiltInsInitializer {
            JvmBuiltIns()
        }

        @JvmStatic
        val Instance: KotlinBuiltIns
            get() = initializer.get()
    }

    override fun getAdditionalSupertypesProvider(): AdditionalSupertypes {
        return BuiltInClassesAreSerializableOnJvm(builtInsModule)
    }
}

class NewJvmBuiltins() : KotlinBuiltIns() {

    private var packageFragmentProvider: PackageFragmentProvider? = null

    @Inject
    fun setPackageFragmentProvider(packageFragmentProvider: PackageFragmentProvider) {
        this.packageFragmentProvider = packageFragmentProvider
    }

    // Used in container
    @Suppress("unused")
    val isInitialized: Boolean get() = packageFragmentProvider != null

    override fun getAdditionalSupertypesProvider(): AdditionalSupertypes {
        return object : BuiltInClassesAreSerializableOnJvm(builtInsModule) {
            override fun forClass(classDescriptor: DeserializedClassDescriptor): Collection<KotlinType> {
                assert(packageFragmentProvider != null)
                return super.forClass(classDescriptor)
            }
        }
    }
}