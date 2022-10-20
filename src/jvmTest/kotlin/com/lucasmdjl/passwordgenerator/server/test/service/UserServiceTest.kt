package com.lucasmdjl.passwordgenerator.server.test.service

import com.lucasmdjl.passwordgenerator.common.dto.server.UserServerDto
import com.lucasmdjl.passwordgenerator.server.model.User
import com.lucasmdjl.passwordgenerator.server.plugins.DataConflictException
import com.lucasmdjl.passwordgenerator.server.plugins.DataNotFoundException
import com.lucasmdjl.passwordgenerator.server.repository.UserRepository
import com.lucasmdjl.passwordgenerator.server.service.SessionService
import com.lucasmdjl.passwordgenerator.server.service.impl.UserServiceImpl
import com.lucasmdjl.passwordgenerator.server.tables.Users
import io.mockk.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import java.util.*
import kotlin.test.assertEquals

class UserServiceTest : ServiceTestParent() {

    private lateinit var userRepositoryMock: UserRepository
    private lateinit var sessionServiceMock: SessionService

    private lateinit var dummyUserServerDto: UserServerDto
    private lateinit var dummyUser: User
    private lateinit var dummyUserId: UUID
    private lateinit var dummySessionId: UUID

    @BeforeAll
    override fun initMocks() {
        userRepositoryMock = mockk()
        sessionServiceMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummyUserServerDto = UserServerDto("user123")
        dummyUser = User(EntityID(UUID.fromString("ea83b232-af3d-4f5c-a7fe-8d10da8db6ba"), Users))
        dummyUserId = UUID.fromString("712c2153-80e4-4c29-b08e-71ac72facaa0")
        dummySessionId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
    }

    @Nested
    inner class Create {

        @Test
        fun `create user when it doesn't exist yet`() {
            every { userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId) } returns null
            every { userRepositoryMock.createAndGetId(dummyUserServerDto.username, dummySessionId) } returns dummyUserId
            every { userRepositoryMock.getById(dummyUserId) } returns dummyUser
            every { sessionServiceMock.setLastUser(dummySessionId, dummyUser) } just Runs
            mockTransaction()

            val userService = UserServiceImpl(userRepositoryMock, sessionServiceMock)

            val userResult = userService.create(dummyUserServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId)
                userRepositoryMock.createAndGetId(dummyUserServerDto.username, dummySessionId)
                userRepositoryMock.getById(dummyUserId)
                sessionServiceMock.setLastUser(dummySessionId, dummyUser)
            }
            assertEquals(dummyUser, userResult)
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

            val userService = UserServiceImpl(userRepositoryMock, sessionServiceMock)

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
                sessionServiceMock wasNot Called
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
            every { sessionServiceMock.setLastUser(dummySessionId, dummyUser) } just Runs
            mockTransaction()

            val userService = UserServiceImpl(userRepositoryMock, sessionServiceMock)

            val userResult = userService.find(dummyUserServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId)
                sessionServiceMock.setLastUser(dummySessionId, dummyUser)
            }
            assertEquals(dummyUser, userResult)
        }

        @Test
        fun `find user when it doesn't exist`() {
            every { userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId) } returns null
            mockTransaction()

            val userService = UserServiceImpl(userRepositoryMock, sessionServiceMock)

            assertThrows<DataNotFoundException> {
                userService.find(dummyUserServerDto, dummySessionId)
            }

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId)
            }
            verify {
                sessionServiceMock wasNot Called
            }
        }

    }

    @Nested
    inner class Logout {

        @Test
        fun `logout when user exists`() {
            val userMock = mockk<User>()
            every { sessionServiceMock.getLastUser(dummySessionId) } returns userMock
            every { userMock.username } returns dummyUserServerDto.username
            every { userRepositoryMock.setLastEmail(userMock, null) } just Runs
            every { sessionServiceMock.setLastUser(dummySessionId, null) } just Runs
            mockTransaction()

            val userService = UserServiceImpl(userRepositoryMock, sessionServiceMock)

            userService.logout(dummyUserServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionServiceMock.getLastUser(dummySessionId)
                userMock.username
                userRepositoryMock.setLastEmail(userMock, null)
                sessionServiceMock.setLastUser(dummySessionId, null)
            }

        }

        @Test
        fun `logout when user doesn't is not last user`() {
            val userMock = mockk<User>()
            val otherUsername = "UserXYZ"
            every { sessionServiceMock.getLastUser(dummySessionId) } returns userMock
            every { userMock.username } returns otherUsername
            mockTransaction()

            val userService = UserServiceImpl(userRepositoryMock, sessionServiceMock)

            assertThrows<DataNotFoundException> {
                userService.logout(dummyUserServerDto, dummySessionId)
            }

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionServiceMock.getLastUser(dummySessionId)
                userMock.username
            }
            verify(exactly = 0) {
                userRepositoryMock.setLastEmail(userMock, null)
                sessionServiceMock.setLastUser(dummySessionId, null)
            }

        }

        @Test
        fun `logout when no last user`() {
            every { sessionServiceMock.getLastUser(dummySessionId) } returns null
            mockTransaction()

            val userService = UserServiceImpl(userRepositoryMock, sessionServiceMock)

            assertThrows<DataNotFoundException> {
                userService.logout(dummyUserServerDto, dummySessionId)
            }

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionServiceMock.getLastUser(dummySessionId)
            }
            verify(exactly = 0) {
                userRepositoryMock.setLastEmail(any(), null)
                sessionServiceMock.setLastUser(dummySessionId, null)
            }

        }

    }

}
