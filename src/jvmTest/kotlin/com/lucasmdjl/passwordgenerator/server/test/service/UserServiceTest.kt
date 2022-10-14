package com.lucasmdjl.passwordgenerator.server.test.service

import com.lucasmdjl.passwordgenerator.common.dto.server.UserServerDto
import com.lucasmdjl.passwordgenerator.server.model.User
import com.lucasmdjl.passwordgenerator.server.repository.UserRepository
import com.lucasmdjl.passwordgenerator.server.service.SessionService
import com.lucasmdjl.passwordgenerator.server.service.impl.UserServiceImpl
import com.lucasmdjl.passwordgenerator.server.tables.Users
import io.mockk.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserServiceTest : ServiceTestParent() {

    private lateinit var userRepositoryMock: UserRepository
    private lateinit var sessionServiceMock: SessionService

    private lateinit var dummyUserServerDto: UserServerDto
    private lateinit var dummyUser: User
    private var dummyUserId = 0
    private lateinit var dummySessionId: UUID

    @BeforeAll
    override fun initMocks() {
        userRepositoryMock = mockk()
        sessionServiceMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummyUserServerDto = UserServerDto("user123")
        dummyUser = User(EntityID(2, Users))
        dummyUserId = 7
        dummySessionId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
    }

    @Nested
    inner class Create {

        @Test
        fun `create user when it doesn't exist yet`() {
            every { userRepositoryMock.createAndGetId(dummyUserServerDto.username, dummySessionId) } returns dummyUserId
            every { userRepositoryMock.getById(dummyUserId) } returns dummyUser
            every { sessionServiceMock.setLastUser(dummySessionId, dummyUser) } just Runs
            mockTransaction()

            val userService = UserServiceImpl(userRepositoryMock, sessionServiceMock)

            val userResult = userService.create(dummyUserServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userRepositoryMock.createAndGetId(dummyUserServerDto.username, dummySessionId)
                userRepositoryMock.getById(dummyUserId)
                sessionServiceMock.setLastUser(dummySessionId, dummyUser)
            }
            assertEquals(dummyUser, userResult)
        }

        @Test
        fun `create user when it already exists`() {
            every { userRepositoryMock.createAndGetId(dummyUserServerDto.username, dummySessionId) } returns null
            every { sessionServiceMock.setLastUser(dummySessionId, null) } just Runs
            mockTransaction()

            val userService = UserServiceImpl(userRepositoryMock, sessionServiceMock)

            val userResult = userService.create(dummyUserServerDto, dummySessionId)

            verify(exactly = 0) {
                userRepositoryMock.getById(any())
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userRepositoryMock.createAndGetId(dummyUserServerDto.username, dummySessionId)
                sessionServiceMock.setLastUser(dummySessionId, null)
            }
            assertNull(userResult)
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
            every { sessionServiceMock.setLastUser(dummySessionId, null) } just Runs
            mockTransaction()

            val userService = UserServiceImpl(userRepositoryMock, sessionServiceMock)

            val userResult = userService.find(dummyUserServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId)
                sessionServiceMock.setLastUser(dummySessionId, null)
            }
            assertNull(userResult)
        }

    }

    @Nested
    inner class Logout {

        @Test
        fun `logout when user exists`() {
            every {
                userRepositoryMock.getByNameAndSession(
                    dummyUserServerDto.username,
                    dummySessionId
                )
            } returns dummyUser
            every { sessionServiceMock.setLastUser(dummySessionId, dummyUser) } just Runs
            every { userRepositoryMock.setLastEmail(dummyUser, null) } just Runs
            every { sessionServiceMock.setLastUser(dummySessionId, null) } just Runs
            mockTransaction()

            val userService = UserServiceImpl(userRepositoryMock, sessionServiceMock)

            userService.logout(dummyUserServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId)
                sessionServiceMock.setLastUser(dummySessionId, dummyUser)
                userRepositoryMock.setLastEmail(dummyUser, null)
                sessionServiceMock.setLastUser(dummySessionId, null)
            }

        }

        @Test
        fun `logout when user doesn't exist`() {
            every { userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId) } returns null
            every { sessionServiceMock.setLastUser(dummySessionId, null) } just Runs
            mockTransaction()

            val userService = UserServiceImpl(userRepositoryMock, sessionServiceMock)

            userService.logout(dummyUserServerDto, dummySessionId)

            verify(exactly = 0) {
                sessionServiceMock.setLastUser(dummySessionId, matchNullable { it != null })
                userRepositoryMock.setLastEmail(any(), any())
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId)
                sessionServiceMock.setLastUser(dummySessionId, null)
            }

        }

    }

}
