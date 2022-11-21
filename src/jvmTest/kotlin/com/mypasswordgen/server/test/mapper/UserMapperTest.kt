package com.mypasswordgen.server.test.mapper

import com.mypasswordgen.common.dto.FullEmailClientDto
import com.mypasswordgen.common.dto.FullUserClientDto
import com.mypasswordgen.common.dto.client.UserClientDto
import com.mypasswordgen.server.mapper.EmailMapper
import com.mypasswordgen.server.mapper.impl.UserMapperImpl
import com.mypasswordgen.server.model.Email
import com.mypasswordgen.server.model.User
import io.mockk.*
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
    private lateinit var emailMapperMock: EmailMapper
    private lateinit var dummyFullEmailClientList: List<FullEmailClientDto>
    private lateinit var dummyFullUserClientDto: FullUserClientDto

    @BeforeAll
    override fun initMocks() {
        userMock = mockk()
        emailListMock = listOf(mockk(), mockk())
        emailMapperMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummyUserId = "user123"
        dummyEmailAddressList = listOf("email1", "email2")
        dummyEmailIdsList = mutableListOf("id1", "id2")
        dummyUserClientDto = UserClientDto(dummyUserId, listOf())
        dummyFullEmailClientList = listOf(FullEmailClientDto("email1"), FullEmailClientDto("email2"))
        dummyFullUserClientDto = FullUserClientDto()
    }

    @Nested
    inner class UserToUserClientDto {

        @Test
        fun `with no emails`() {
            mockTransaction()
            every { userMock.id.value.toString() } returns dummyUserId
            every { userMock.emails } returns emptySized()
            val userMapper = UserMapperImpl(emailMapperMock)
            val userDto = userMapper.userToUserClientDto(userMock)
            assertEquals(dummyUserId, userDto.id)
            assertTrue(userDto.emailIdList.isEmpty())
            verify {
                transaction(statement = any<Transaction.() -> Any>())
            }
        }

        @Test
        fun `with emails`() {
            mockTransaction()
            every { userMock.id.value.toString() } returns dummyUserId
            every { userMock.emails } returns SizedCollection(emailListMock)
            emailListMock.forEachIndexed { index, userMock ->
                every { userMock.id.value.toString() } returns dummyEmailAddressList[index]
            }
            val userMapper = UserMapperImpl(emailMapperMock)
            val userDto = userMapper.userToUserClientDto(userMock)
            assertEquals(dummyUserId, userDto.id)
            assertEquals(dummyEmailAddressList, userDto.emailIdList)
            verify {
                transaction(statement = any<Transaction.() -> Any>())
            }
        }

        @Test
        fun `with receiver`() {
            val userMapper = UserMapperImpl(emailMapperMock)
            val userMapperSpy = spyk(userMapper)
            every { userMapperSpy.userToUserClientDto(userMock) } returns dummyUserClientDto
            val result = with(userMapperSpy) {
                userMock.toUserClientDto()
            }
            assertEquals(dummyUserClientDto, result)
            verifySequence {
                with(userMapperSpy) {
                    userMock.toUserClientDto()
                }
                userMapperSpy.userToUserClientDto(userMock)
            }
        }

    }

    @Nested
    inner class UserToFullUserClientDto {
        @Test
        fun `with argument`() {
            mockTransaction()
            every { userMock.emails } returns SizedCollection(emailListMock)
            emailListMock.forEachIndexed { index, email ->
                every {
                    with(emailMapperMock) {
                        email.toFullEmailClientDto()
                    }
                } returns dummyFullEmailClientList[index]
            }

            val userMapper = UserMapperImpl(emailMapperMock)

            val result = userMapper.userToFullUserClientDto(userMock)

            assertEquals(2, result.emails.size)
            for (i in 0..1) {
                assertEquals(dummyFullEmailClientList[i], result.emails[i])
            }
            verifyOrder {
                userMock.emails
                for (user in emailListMock) {
                    with(emailMapperMock) {
                        user.toFullEmailClientDto()
                    }
                }
            }
        }

        @Test
        fun `with receiver`() {
            val userMapper = UserMapperImpl(emailMapperMock)
            val sessionMapperSpy = spyk(userMapper)
            every { sessionMapperSpy.userToFullUserClientDto(userMock) } returns dummyFullUserClientDto
            val result = with(sessionMapperSpy) {
                userMock.toFullUserClientDto()
            }

            assertEquals(dummyFullUserClientDto, result)
            verifySequence {
                with(sessionMapperSpy) {
                    userMock.toFullUserClientDto()
                }
                sessionMapperSpy.userToFullUserClientDto(userMock)
            }
        }
    }

}
