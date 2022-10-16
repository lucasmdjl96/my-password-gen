package com.lucasmdjl.passwordgenerator.server.test.mapper

import com.lucasmdjl.passwordgenerator.common.dto.client.UserClientDto
import com.lucasmdjl.passwordgenerator.server.mapper.impl.UserMapperImpl
import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.User
import io.mockk.*
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.emptySized
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserMapperTest : MapperTestParent() {

    private lateinit var userMock: User
    private lateinit var dummyUsername: String
    private lateinit var emailListMock: List<Email>
    private lateinit var dummyEmailAddressList: List<String>
    private lateinit var dummyUserClientDto: UserClientDto

    @BeforeAll
    override fun initMocks() {
        userMock = mockk()
        emailListMock = listOf(mockk(), mockk())
    }

    @BeforeEach
    override fun initDummies() {
        dummyUsername = "user123"
        dummyEmailAddressList = listOf("email1", "email2")
        dummyUserClientDto = UserClientDto(dummyUsername)
    }

    @Nested
    inner class UserToUserClientDto {

        @Test
        fun `with no emails`() {
            mockTransaction()
            mockkStatic("org.jetbrains.exposed.dao.ReferencesKt")
            every { userMock.load(any()) } returns userMock
            every { userMock.username } returns dummyUsername
            every { userMock.emails } returns emptySized()
            val userMapper = UserMapperImpl()
            val userDto = userMapper.userToUserClientDto(userMock)
            assertEquals(dummyUsername, userDto.username)
            assertTrue(userDto.emailList.isEmpty())
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userMock.load(User::emails)
            }
        }

        @Test
        fun `with emails`() {
            mockTransaction()
            mockkStatic("org.jetbrains.exposed.dao.ReferencesKt")
            every { userMock.load(any()) } returns userMock
            every { userMock.username } returns dummyUsername
            every { userMock.emails } returns SizedCollection(emailListMock)
            emailListMock.forEachIndexed { index, emailMock ->
                every { emailMock.emailAddress } returns dummyEmailAddressList[index]
            }
            val userMapper = UserMapperImpl()
            val userDto = userMapper.userToUserClientDto(userMock)
            assertEquals(dummyUsername, userDto.username)
            assertEquals(dummyEmailAddressList, userDto.emailList)
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userMock.load(User::emails)
            }
        }

        @Test
        fun `with receiver`() {
            val userMapper = UserMapperImpl()
            val userMapperSpy = spyk(userMapper)
            every { userMapperSpy.userToUserClientDto(userMock) } returns dummyUserClientDto
            with(userMapperSpy) {
                userMock.toUserClientDto()
            }
            verifySequence {
                with(userMapperSpy) {
                    userMock.toUserClientDto()
                }
                userMapperSpy.userToUserClientDto(userMock)
            }
        }

    }


}
