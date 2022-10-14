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

class UserMapperTest : MapperTestParent() {

    private lateinit var userMock: User
    private lateinit var dummyUserName: String
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
        dummyUserName = "user123"
        dummyEmailAddressList = listOf("email1", "email2")
        dummyUserClientDto = UserClientDto(dummyUserName)
    }

    @Nested
    inner class UserToUserClientDto {

        @Test
        fun `with no emails`() {
            mockTransaction()
            mockkStatic("org.jetbrains.exposed.dao.ReferencesKt")
            every { userMock.load(any()) } returns userMock
            every { userMock.username } returns dummyUserName
            every { userMock.emails } returns emptySized()
            val userMapper = UserMapperImpl()
            val userDto = userMapper.userToUserClientDto(userMock)
            assertEquals(dummyUserName, userDto.username)
            assert(userDto.emailList.isEmpty())
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
            every { userMock.username } returns dummyUserName
            every { userMock.emails } returns SizedCollection(emailListMock)
            emailListMock.forEachIndexed { index, email ->
                every { email.emailAddress } returns dummyEmailAddressList[index]
            }
            val userMapper = UserMapperImpl()
            val userDto = userMapper.userToUserClientDto(userMock)
            assertEquals(dummyUserName, userDto.username)
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
            with (userMapperSpy) {
                userMock.toUserClientDto()
            }
            verifySequence {
                with (userMapperSpy) {
                    userMock.toUserClientDto()            }
                userMapperSpy.userToUserClientDto(userMock)
            }
        }

    }


}
