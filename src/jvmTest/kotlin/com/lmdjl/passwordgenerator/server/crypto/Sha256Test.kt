package com.lmdjl.passwordgenerator.server.crypto

import com.lmdjl.passwordgenerator.server.TestParent
import com.lucasmdjl.passwordgenerator.common.dto.server.UserServerDto
import com.lucasmdjl.passwordgenerator.server.crypto.encode
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class Sha256Test : TestParent() {

    private lateinit var dummyUserServerDto: UserServerDto
    private lateinit var dummyUserServerDtoSame: UserServerDto
    private lateinit var dummyUserServerDtoOther: UserServerDto

    @BeforeAll
    override fun initMocks() {

    }

    @BeforeEach
    override fun initDummies() {
        dummyUserServerDto = UserServerDto("user123")
        dummyUserServerDtoSame = UserServerDto("user123")
        dummyUserServerDtoOther = UserServerDto("123user")
    }

    @Test
    fun `users with same username`() {
        val resultUserServerDto = dummyUserServerDto.encode()
        val resultUserServerDtoSame = dummyUserServerDtoSame.encode()

        assertEquals(resultUserServerDto, resultUserServerDtoSame)
    }

    @Test
    fun `users with different usernam`() {
        val resultUserServerDto = dummyUserServerDto.encode()
        val resultUserServerDtoOther = dummyUserServerDtoOther.encode()

        assertNotEquals(resultUserServerDto, resultUserServerDtoOther)
    }

}
