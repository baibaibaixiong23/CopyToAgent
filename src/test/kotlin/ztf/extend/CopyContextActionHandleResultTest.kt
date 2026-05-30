/*
 * Copyright 2026 ztf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ztf.extend

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

class CopyContextActionHandleResultTest {

    @Test
    fun `success writes reference to clipboard writer`() {
        val written = AtomicReference<String>()
        CopyContextAction(Consumer { written.set(it) })
            .handleResult(mockProject(), ContextLinkBuilder.Result.Success("@src/Foo.kt#L10-20"))

        assertEquals("@src/Foo.kt#L10-20", written.get())
    }

    @Test
    fun `failure does not write to clipboard`() {
        val called = AtomicBoolean(false)
        CopyContextAction(Consumer { called.set(true) })
            .handleResult(mockProject(), ContextLinkBuilder.Result.Failure("notification.cannotGetFilePath"))

        assertFalse(called.get())
    }

    @Test
    fun `clipboard writer exception is swallowed`() {
        val action = CopyContextAction(Consumer { throw RuntimeException("boom") })
        // Should not throw
        action.handleResult(mockProject(), ContextLinkBuilder.Result.Success("@src/Foo.kt#L1"))
    }

    private fun mockProject(): com.intellij.openapi.project.Project {
        return java.lang.reflect.Proxy.newProxyInstance(
            com.intellij.openapi.project.Project::class.java.classLoader,
            arrayOf(com.intellij.openapi.project.Project::class.java)
        ) { _, method, _ ->
            if (method.returnType.isPrimitive) {
                if (method.returnType == Boolean::class.javaPrimitiveType) return@newProxyInstance false
                if (method.returnType == Int::class.javaPrimitiveType) return@newProxyInstance 0
                return@newProxyInstance 0
            }
            null
        } as com.intellij.openapi.project.Project
    }
}
