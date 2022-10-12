package com.lmdjl.passwordgenerator.server.service

import com.lucasmdjl.passwordgenerator.common.dto.server.UserServerDto
import com.lucasmdjl.passwordgenerator.server.model.Session
import com.lucasmdjl.passwordgenerator.server.model.User
import com.lucasmdjl.passwordgenerator.server.repository.UserRepository
import com.lucasmdjl.passwordgenerator.server.service.impl.UserServiceImpl
import com.lucasmdjl.passwordgenerator.server.tables.Sessions
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

    private lateinit var dummyUserServerDto: UserServerDto
    private lateinit var dummyUser: User
    private var dummyUserId = 0
    private lateinit var dummySessionId: UUID
    private lateinit var dummySessionFromId: UUID
    private lateinit var dummySessionToId: UUID
    private lateinit var dummySessionFrom: Session
    private lateinit var dummySessionTo: Session

    @BeforeAll
    override fun initMocks() {
        userRepositoryMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummyUserServerDto = UserServerDto("user123")
        dummyUser = User(EntityID(2, Users))
        dummyUserId = 7
        dummySessionId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        dummySessionFromId = UUID.fromString("123e4567-e89b-13d2-a456-426614174000")
        dummySessionToId = UUID.fromString("123e4567-eb98-12d3-a456-426614174000")
        dummySessionFrom = Session(EntityID(dummySessionFromId, Sessions))
        dummySessionTo = Session(EntityID(dummySessionToId, Sessions))
    }

    @Nested
    inner class Create {

        @Test
        fun `create user when it doesn't exist yet`() {
            every { userRepositoryMock.createAndGetId(dummyUserServerDto.username, dummySessionId) } returns dummyUserId
            every { userRepositoryMock.getById(dummyUserId) } returns dummyUser
            mockTransaction()

            val userService = UserServiceImpl(userRepositoryMock)

            val userResult = userService.create(dummyUserServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userRepositoryMock.createAndGetId(dummyUserServerDto.username, dummySessionId)
                userRepositoryMock.getById(dummyUserId)
            }
            assertEquals(dummyUser, userResult)
        }

        @Test
        fun `create user when it already exists`() {
            every { userRepositoryMock.createAndGetId(dummyUserServerDto.username, dummySessionId) } returns null
            mockTransaction()

            val userService = UserServiceImpl(userRepositoryMock)

            val userResult = userService.create(dummyUserServerDto, dummySessionId)

            verify(exactly = 0) {
                userRepositoryMock.getById(any())
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userRepositoryMock.createAndGetId(dummyUserServerDto.username, dummySessionId)
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
            mockTransaction()

            val userService = UserServiceImpl(userRepositoryMock)

            val userResult = userService.find(dummyUserServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId)
            }
            assertEquals(dummyUser, userResult)
        }

        @Test
        fun `find user when it doesn't exist`() {
            every { userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId) } returns null
            mockTransaction()

            val userService = UserServiceImpl(userRepositoryMock)

            val userResult = userService.find(dummyUserServerDto, dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userRepositoryMock.getByNameAndSession(dummyUserServerDto.username, dummySessionId)
            }
            assertNull(userResult)
        }

    }

    @Nested
    inner class Move {

        @Test
        fun `move all users`() {
            mockTransaction()
            every { userRepositoryMock.moveAll(dummySessionFromId, dummySessionToId) } just Runs

            val userService = UserServiceImpl(userRepositoryMock)

            userService.moveAllUsers(dummySessionFrom, dummySessionTo)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userRepositoryMock.moveAll(dummySessionFromId, dummySessionToId)
            }

        }

    }

}
