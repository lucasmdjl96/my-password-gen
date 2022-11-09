package com.mypasswordgen.server.test.mapper

import com.mypasswordgen.common.dto.client.UserClientDto
import com.mypasswordgen.server.mapper.impl.UserMapperImpl
import com.mypasswordgen.server.model.Email
import com.mypasswordgen.server.model.User
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
    private lateinit var dummyUserId: String
    private lateinit var emailListMock: List<Email>
    private lateinit var dummyEmailAddressList: List<String>
    private lateinit var dummyEmailIdsList: MutableList<String>
    private lateinit var dummyUserClientDto: UserClientDto

    @BeforeAll
    override fun initMocks() {
        userMock = mockk()
        emailListMock = listOf(mockk(), mockk())
    }

    @BeforeEach
    override fun initDummies() {
        dummyUserId = "user123"
        dummyEmailAddressList = listOf("email1", "email2")
        dummyEmailIdsList = mutableListOf("id1", "id2")
        dummyUserClientDto = UserClientDto(dummyUserId, listOf())
    }

    @Nested
    inner class UserToUserClientDto {

        @Test
        fun `with no emails`() {
            mockTransaction()
            mockkStatic("org.jetbrains.exposed.dao.ReferencesKt")
            every { userMock.load(any()) } returns userMock
            every { userMock.id.value.toString() } returns dummyUserId
            every { userMock.emails } returns emptySized()
            val userMapper = UserMapperImpl()
            val userDto = userMapper.userToUserClientDto(userMock)
            assertEquals(dummyUserId, userDto.id)
            assertTrue(userDto.emailIdList.isEmpty())
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
            every { userMock.id.value.toString() } returns dummyUserId
            every { userMock.emails } returns SizedCollection(emailListMock)
            emailListMock.forEachIndexed { index, userMock ->
                every { userMock.id.value.toString() } returns dummyEmailAddressList[index]
            }
            val userMapper = UserMapperImpl()
            val userDto = userMapper.userToUserClientDto(userMock)
            assertEquals(dummyUserId, userDto.id)
            assertEquals(dummyEmailAddressList, userDto.emailIdList)
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
