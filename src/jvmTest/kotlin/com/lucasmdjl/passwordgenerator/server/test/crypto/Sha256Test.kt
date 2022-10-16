package com.lucasmdjl.passwordgenerator.server.test.crypto

import com.lucasmdjl.passwordgenerator.common.dto.server.UserServerDto
import com.lucasmdjl.passwordgenerator.server.crypto.encode
import com.lucasmdjl.passwordgenerator.server.test.TestParent
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class Sha256Test : TestParent() {

    private lateinit var dummyUserServerDto: UserServerDto
    private lateinit var dummyUserServerDtoSame: UserServerDto
    private lateinit var dummyUserServerDtoOther: UserServerDto

    override fun initMocks() {}

    @BeforeEach
    override fun initDummies() {
        dummyUserServerDto = UserServerDto("user123")
        dummyUserServerDtoSame = UserServerDto("user123")
        dummyUserServerDtoOther = UserServerDto("123user")
    }

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

    @Test
    fun `users with same username`() {
        val resultUserServerDto = dummyUserServerDto.encode()
        val resultUserServerDtoSame = dummyUserServerDtoSame.encode()

        assertEquals(resultUserServerDto, resultUserServerDtoSame)
    }

    @Test
    fun `users with different username`() {
        val resultUserServerDto = dummyUserServerDto.encode()
        val resultUserServerDtoOther = dummyUserServerDtoOther.encode()

        assertNotEquals(resultUserServerDto, resultUserServerDtoOther)
    }

}
