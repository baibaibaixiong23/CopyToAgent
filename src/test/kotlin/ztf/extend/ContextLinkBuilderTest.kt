package ztf.extend

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContextLinkBuilderTest {

    @Test
    fun `no selection returns path without line reference`() {
        val result = ContextLinkBuilder.buildContextLink("src/Foo.kt", -1, -1, "claude")
        assertTrue(result is ContextLinkBuilder.Result.Success)
        assertEquals("@src/Foo.kt", (result as ContextLinkBuilder.Result.Success).reference)
    }

    @Test
    fun `single line selection claude format`() {
        val result = ContextLinkBuilder.buildContextLink("src/Foo.kt", 5, 5, "claude")
        assertEquals("@src/Foo.kt#L5", (result as ContextLinkBuilder.Result.Success).reference)
    }

    @Test
    fun `multi line selection claude format`() {
        val result = ContextLinkBuilder.buildContextLink("src/Foo.kt", 10, 20, "claude")
        assertEquals("@src/Foo.kt#L10-20", (result as ContextLinkBuilder.Result.Success).reference)
    }

    @Test
    fun `single line selection opencode format`() {
        val result = ContextLinkBuilder.buildContextLink("src/Foo.kt", 5, 5, "opencode")
        assertEquals("@src/Foo.kt#5", (result as ContextLinkBuilder.Result.Success).reference)
    }

    @Test
    fun `multi line selection opencode format`() {
        val result = ContextLinkBuilder.buildContextLink("src/Foo.kt", 10, 20, "opencode")
        assertEquals("@src/Foo.kt#10-20", (result as ContextLinkBuilder.Result.Success).reference)
    }

    @Test
    fun `blank file path returns failure`() {
        val result = ContextLinkBuilder.buildContextLink("", 1, 1, "claude")
        assertTrue(result is ContextLinkBuilder.Result.Failure)
        assertEquals("notification.cannotGetFilePath", (result as ContextLinkBuilder.Result.Failure).messageKey)
    }

    @Test
    fun `absolute path works correctly`() {
        val result = ContextLinkBuilder.buildContextLink("/Users/dev/project/Foo.kt", 1, 3, "claude")
        assertEquals("@/Users/dev/project/Foo.kt#L1-3", (result as ContextLinkBuilder.Result.Success).reference)
    }
}
