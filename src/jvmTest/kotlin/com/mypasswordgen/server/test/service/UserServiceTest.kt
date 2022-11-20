package com.mypasswordgen.server.test.service

import com.mypasswordgen.common.dto.EmailIDBDto
import com.mypasswordgen.common.dto.FullEmailServerDto
import com.mypasswordgen.common.dto.FullUserClientDto
import com.mypasswordgen.common.dto.FullUserServerDto
import com.mypasswordgen.common.dto.client.UserClientDto
import com.mypasswordgen.common.dto.server.UserServerDto
import com.mypasswordgen.server.crypto.encode
import com.mypasswordgen.server.mapper.UserMapper
import com.mypasswordgen.server.model.User
import com.mypasswordgen.server.plugins.DataConflictException
import com.mypasswordgen.server.plugins.DataNotFoundException
import com.mypasswordgen.server.repository.SessionRepository
import com.mypasswordgen.server.repository.UserRepository
import com.mypasswordgen.server.service.EmailService
import com.mypasswordgen.server.service.impl.UserServiceImpl
import com.mypasswordgen.server.tables.Users
import io.mockk.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import java.util.*
import kotlin.test.assertEquals

class UserServiceTest : ServiceTestParent() {

    private lateinit var emailServiceMock: EmailService
    private lateinit var userRepositoryMock: UserRepository
    private lateinit var sessionRepositoryMock: SessionRepository
    private lateinit var userMapperMock: UserMapper

    private lateinit var dummyUserServerDto: UserServerDto
    private lateinit var dummyUser: User
    private lateinit var dummyUserId: UUID
    private lateinit var dummySessionId: UUID
    private lateinit var dummyUserClientDto: UserClientDto
    private lateinit var dummyFullUserServerDto: FullUserServerDto
    private lateinit var dummyUsernameEncoded: String
    private lateinit var dummyEmailIDBDtoList: List<EmailIDBDto>
    private lateinit var dummyFullUserClientDto: FullUserClientDto

    @BeforeAll
    override fun initMocks() {
        emailServiceMock = mockk()
        userRepositoryMock = mockk()
        sessionRepositoryMock = mockk()
        userMapperMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummyUserServerDto = UserServerDto("user123")
        dummyUserClientDto = UserClientDto(
            "0544fc3d-f169-431e-83b9-3a240404e8cd",
            listOf("81d35212-d8aa-4d51-b556-92ba0b0b7b36", "1278a8d9-c5e8-4abd-bbfa-68f29e04094c")
        )
        dummyUser = User(EntityID(UUID.fromString("ea83b232-af3d-4f5c-a7fe-8d10da8db6ba"), Users))
        dummyUserId = UUID.fromString("712c2153-80e4-4c29-b08e-71ac72facaa0")
        dummySessionId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        dummyFullUserServerDto = FullUserServerDto(
            "user1", mutableListOf(
                FullEmailServerDto("email1"), FullEmailServerDto("email2")
            )
        )
        dummyUsernameEncoded = "UserXXX"
        dummyEmailIDBDtoList = listOf(EmailIDBDto("id1", "email1x"), EmailIDBDto("id2", "email2x"))
        dummyFullUserClientDto = FullUserClientDto()
    }

    @Nested
    inner class Create {

        @Test
        fun `create user when it doesn't exist yet`() {
            every { userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId) } returns null
            every { userRepositoryMock.createAndGetId(dummyUserServerDto.username, dummySessionId) } returns dummyUserId
            every { userRepositoryMock.getById(dummyUserId) } returns dummyUser
            every { sessionRepositoryMock.setLastUser(dummySessionId, dummyUser) } just Runs
            with(userMapperMock) {
                every { dummyUser.toUserClientDto() } returns dummyUserClientDto
            }
            mockTransaction()

            val userService = UserServiceImpl(emailServiceMock, userRepositoryMock, sessionRepositoryMock, userMapperMock)

            val userResult = userService.create(dummyUserServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId)
                userRepositoryMock.createAndGetId(dummyUserServerDto.username, dummySessionId)
                userRepositoryMock.getById(dummyUserId)
                sessionRepositoryMock.setLastUser(dummySessionId, dummyUser)
                with(userMapperMock) {
                    dummyUser.toUserClientDto()
                }
            }
            assertEquals(dummyUserClientDto, userResult)
        }

        @Test
        fun `create user when it already exists`() {
            every {
                userRepositoryMock.getByNameAndSession(
                    dummyUserServerDto.username,
                    dummySessionId
                )
            } returns dummyUser
            mockTransaction()

            val userService = UserServiceImpl(emailServiceMock, userRepositoryMock, sessionRepositoryMock, userMapperMock)

            assertThrows<DataConflictException> {
                userService.create(dummyUserServerDto, dummySessionId)
            }

            verify(exactly = 0) {
                userRepositoryMock.createAndGetId(dummyUserServerDto.username, dummySessionId)
                userRepositoryMock.getById(any())
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId)
            }
            verify {
                sessionRepositoryMock wasNot Called
            }
        }

    }

    @Nested
    inner class Find {

        @Test
        fun `find user when it already exist`() {
            every {
                userRepositoryMock.getByNameAndSession(
                    dummyUserServerDto.username,
                    dummySessionId
                )
            } returns dummyUser
            every { sessionRepositoryMock.setLastUser(dummySessionId, dummyUser) } just Runs
            with(userMapperMock) {
                every { dummyUser.toUserClientDto() } returns dummyUserClientDto
            }
            mockTransaction()

            val userService = UserServiceImpl(emailServiceMock, userRepositoryMock, sessionRepositoryMock, userMapperMock)

            val userResult = userService.find(dummyUserServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId)
                sessionRepositoryMock.setLastUser(dummySessionId, dummyUser)
                with(userMapperMock) {
                    dummyUser.toUserClientDto()
                }
            }
            assertEquals(dummyUserClientDto, userResult)
        }

        @Test
        fun `find user when it doesn't exist`() {
            every { userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId) } returns null
            mockTransaction()

            val userService = UserServiceImpl(emailServiceMock, userRepositoryMock, sessionRepositoryMock, userMapperMock)

            assertThrows<DataNotFoundException> {
                userService.find(dummyUserServerDto, dummySessionId)
            }

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId)
            }
            verify {
                sessionRepositoryMock wasNot Called
            }
        }

    }

    @Nested
    inner class Logout {

        @Test
        fun `logout when user exists`() {
            val userMock = mockk<User>()
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns userMock
            every { userMock.username } returns dummyUserServerDto.username
            every { userRepositoryMock.setLastEmail(userMock, null) } just Runs
            every { sessionRepositoryMock.setLastUser(dummySessionId, null) } just Runs
            mockTransaction()

            val userService = UserServiceImpl(emailServiceMock, userRepositoryMock, sessionRepositoryMock, userMapperMock)

            userService.logout(dummyUserServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
                userMock.username
                userRepositoryMock.setLastEmail(userMock, null)
                sessionRepositoryMock.setLastUser(dummySessionId, null)
            }

        }

        @Test
        fun `logout when user doesn't is not last user`() {
            val userMock = mockk<User>()
            val otherUsername = "UserXYZ"
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns userMock
            every { userMock.username } returns otherUsername
            mockTransaction()

            val userService = UserServiceImpl(emailServiceMock, userRepositoryMock, sessionRepositoryMock, userMapperMock)

            assertThrows<DataNotFoundException> {
                userService.logout(dummyUserServerDto, dummySessionId)
            }

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
                userMock.username
            }
            verify(exactly = 0) {
                userRepositoryMock.setLastEmail(userMock, null)
                sessionRepositoryMock.setLastUser(dummySessionId, null)
            }

        }

        @Test
        fun `logout when no last user`() {
            every { sessionRepositoryMock.getLastUser(dummySessionId) } returns null
            mockTransaction()

            val userService = UserServiceImpl(emailServiceMock, userRepositoryMock, sessionRepositoryMock, userMapperMock)

            assertThrows<DataNotFoundException> {
                userService.logout(dummyUserServerDto, dummySessionId)
            }

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getLastUser(dummySessionId)
            }
            verify(exactly = 0) {
                userRepositoryMock.setLastEmail(any(), null)
                sessionRepositoryMock.setLastUser(dummySessionId, null)
            }

        }

    }

    @Nested
    inner class CreateFullUser {

        @Test
        fun `create when it already exists`() {
            mockkStatic("com.mypasswordgen.server.crypto.Sha256Kt")
            every { dummyFullUserServerDto.username.encode() } returns dummyUsernameEncoded
            every {
                userRepositoryMock.getByNameAndSession(
                    dummyUsernameEncoded, dummySessionId
                )
            } returns dummyUser
            mockTransaction()

            val userService = UserServiceImpl(
                emailServiceMock,
                userRepositoryMock,
                sessionRepositoryMock,
                userMapperMock
            )
            assertThrows<DataConflictException> {
                userService.createFullUser(dummyFullUserServerDto, dummySessionId)
            }

            verifyOrder {
                dummyFullUserServerDto.username.encode()
                userRepositoryMock.getByNameAndSession(dummyUsernameEncoded, dummySessionId)
            }
        }

        @Test
        fun `create when a email already exists`() {
            mockkStatic("com.mypasswordgen.server.crypto.Sha256Kt")
            every { dummyFullUserServerDto.username.encode() } returns dummyUsernameEncoded
            every {
                userRepositoryMock.getByNameAndSession(
                    dummyUsernameEncoded, dummySessionId
                )
            } returns null
            every {
                userRepositoryMock.createAndGetId(
                    dummyUsernameEncoded, dummySessionId
                )
            } returns dummyUserId
            every {
                emailServiceMock.createFullEmail(
                    dummyFullUserServerDto.emails[0], dummyUserId
                )
            } throws DataConflictException()
            mockTransaction()

            val userService = UserServiceImpl(
                emailServiceMock,
                userRepositoryMock,
                sessionRepositoryMock,
                userMapperMock
            )
            assertThrows<DataConflictException> {
                userService.createFullUser(dummyFullUserServerDto, dummySessionId)
            }

            verifyOrder {
                dummyFullUserServerDto.username.encode()
                userRepositoryMock.getByNameAndSession(dummyUsernameEncoded, dummySessionId)
                userRepositoryMock.createAndGetId(dummyUsernameEncoded, dummySessionId)
                emailServiceMock.createFullEmail(dummyFullUserServerDto.emails[0], dummyUserId)
            }
        }

        @Test
        fun `create when nothing already exists`() {
            mockkStatic("com.mypasswordgen.server.crypto.Sha256Kt")
            every { dummyFullUserServerDto.username.encode() } returns dummyUsernameEncoded
            every {
                userRepositoryMock.getByNameAndSession(
                    dummyUsernameEncoded, dummySessionId
                )
            } returns null
            every {
                userRepositoryMock.createAndGetId(
                    dummyUsernameEncoded, dummySessionId
                )
            } returns dummyUserId
            dummyFullUserServerDto.emails.forEachIndexed { index, email ->
                every {
                    emailServiceMock.createFullEmail(
                        email, dummyUserId
                    )
                } returns dummyEmailIDBDtoList[index]
            }
            mockTransaction()

            val userService = UserServiceImpl(
                emailServiceMock,
                userRepositoryMock,
                sessionRepositoryMock,
                userMapperMock
            )
            val result = userService.createFullUser(dummyFullUserServerDto, dummySessionId)

            assertEquals(dummyEmailIDBDtoList, result.emails)
            verifyOrder {
                dummyFullUserServerDto.username.encode()
                userRepositoryMock.getByNameAndSession(dummyUsernameEncoded, dummySessionId)
                userRepositoryMock.createAndGetId(dummyUsernameEncoded, dummySessionId)
                for (email in dummyFullUserServerDto.emails) {
                    emailServiceMock.createFullEmail(email, dummyUserId)
                }
            }
        }

    }

    @Nested
    inner class GetFullUser {

        @Test
        fun `with existing user`() {
            mockTransaction()
            val userService = UserServiceImpl(emailServiceMock, userRepositoryMock, sessionRepositoryMock, userMapperMock)
            every { userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId) } returns dummyUser
            with(userMapperMock) {
                every { dummyUser.toFullUserClientDto() } returns dummyFullUserClientDto
            }

            val result = userService.getFullUser(dummyUserServerDto, dummySessionId)

            assertEquals(dummyFullUserClientDto, result)
            verifyOrder {
                userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId)
                with(userMapperMock) {
                    dummyUser.toFullUserClientDto()
                }
            }
        }

        @Test
        fun `with non-existing user`() {
            mockTransaction()
            val userService = UserServiceImpl(emailServiceMock, userRepositoryMock, sessionRepositoryMock, userMapperMock)
            every { userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId) } returns null

            assertThrows<DataNotFoundException> {
                userService.getFullUser(dummyUserServerDto, dummySessionId)
            }

            verify {
                userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId)
            }
        }
    }

}
