package com.mypasswordgen.server.test.repository.crypto

import com.mypasswordgen.server.repository.crypto.encode
import com.mypasswordgen.server.test.TestParent
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class Sha256Test : TestParent() {
    override fun initMocks() {}

    override fun initDummies() {}

    @Test
    fun `same strings`() {
        val string = "string1234".encode()
        val stringSame = "string1234".encode()

        assertEquals(string, stringSame)
    }

    @Test
    fun `different strings`() {
        val string = "string1234".encode()
        val stringOther = "string1432".encode()

        assertNotEquals(string, stringOther)
    }

}
