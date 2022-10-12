package com.lmdjl.passwordgenerator.server.service

import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.model.Session
import com.lucasmdjl.passwordgenerator.server.model.User
import com.lucasmdjl.passwordgenerator.server.repository.SessionRepository
import com.lucasmdjl.passwordgenerator.server.repository.UserRepository
import com.lucasmdjl.passwordgenerator.server.service.impl.SessionServiceImpl
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

class SessionServiceTest : ServiceTestParent() {

    private lateinit var sessionRepositoryMock: SessionRepository
    private lateinit var userRepositoryMock: UserRepository

    private lateinit var dummySessionDto: SessionDto
    private lateinit var dummySessionId: UUID
    private lateinit var dummySessionId1: UUID
    private lateinit var dummySession: Session
    private lateinit var dummySessionIdNew: UUID
    private lateinit var dummySessionNew: Session
    private lateinit var dummyUser: User

    private lateinit var dummySessionFromId: UUID
    private lateinit var dummySessionToId: UUID
    private lateinit var dummySessionFrom: Session
    private lateinit var dummySessionTo: Session

    @BeforeAll
    override fun initMocks() {
        sessionRepositoryMock = mockk()
        userRepositoryMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummySessionId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        dummySessionDto = SessionDto(dummySessionId)
        dummySessionId1 = UUID.fromString("123e4583-e89b-12d3-a456-426614174000")
        dummySession = Session(EntityID(dummySessionId1, Sessions))
        dummySessionIdNew = UUID.fromString("3c084a2e-b46e-45dd-a08e-cbba4ce17d49")
        dummySessionNew = Session(EntityID(dummySessionIdNew, Sessions))
        dummyUser = User(EntityID(4, Users))

        dummySessionFromId = UUID.fromString("123e4567-e89b-13d2-a456-426614174000")
        dummySessionToId = UUID.fromString("123e4567-eb98-12d3-a456-426614174000")
        dummySessionFrom = Session(EntityID(dummySessionFromId, Sessions))
        dummySessionTo = Session(EntityID(dummySessionToId, Sessions))
    }

    @Nested
    inner class AssignNew {

        @Test
        fun `assign new session from existing session`() {
            every { sessionRepositoryMock.create() } returns dummySessionNew
            every { sessionRepositoryMock.getById(dummySessionId) } returns dummySession
            every { sessionRepositoryMock.delete(dummySession) } just Runs
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock)
            val sessionServiceSpy = spyk(sessionService)
            every { sessionServiceSpy.moveAllUsers(dummySession, dummySessionNew) } just Runs

            sessionServiceSpy.assignNew(dummySessionDto)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.create()
                sessionServiceSpy.moveAllUsers(dummySession, dummySessionNew)
                sessionRepositoryMock.delete(dummySession)
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getById(dummySessionId)
                sessionServiceSpy.moveAllUsers(dummySession, dummySessionNew)
                sessionRepositoryMock.delete(dummySession)
            }
        }

        @Test
        fun `assign new session from non-existing session`() {
            every { sessionRepositoryMock.create() } returns dummySessionNew
            every { sessionRepositoryMock.getById(dummySessionId) } returns null
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock)

            sessionService.assignNew(dummySessionDto)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.create()
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getById(dummySessionId)
            }
            verify(exactly = 0) {
                sessionRepositoryMock.delete(any())
            }
            verify {
                userRepositoryMock wasNot Called
            }
        }

        @Test
        fun `assign new session from no session`() {
            every { sessionRepositoryMock.create() } returns dummySessionNew
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock)

            sessionService.assignNew(null)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.create()
            }
            verify(exactly = 0) {
                sessionRepositoryMock.delete(any())
            }
            verify {
                userRepositoryMock wasNot Called
            }
        }
    }

    @Nested
    inner class Find {

        @Test
        fun `find session when session exist`() {
            every { sessionRepositoryMock.getById(dummySessionId) } returns dummySession
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock)

            val sessionResult = sessionService.find(dummySessionDto)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getById(dummySessionId)
            }
            verify {
                userRepositoryMock wasNot Called
            }
            assertEquals(dummySession, sessionResult)
        }

        @Test
        fun `find session when session doesn't exist`() {
            every { sessionRepositoryMock.getById(dummySessionId) } returns null
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock)

            val sessionResult = sessionService.find(dummySessionDto)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getById(dummySessionId)
            }
            verify {
                userRepositoryMock wasNot Called
            }
            assertNull(sessionResult)
        }

    }

    @Nested
    inner class Delete {

        @Test
        fun `delete session when session exists`() {
            every { sessionRepositoryMock.getById(dummySessionId) } returns dummySession
            every { sessionRepositoryMock.delete(dummySession) } just Runs
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock)

            val result = sessionService.delete(dummySessionDto)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getById(dummySessionId)
                sessionRepositoryMock.delete(dummySession)
            }
            verify {
                userRepositoryMock wasNot Called
            }
            assertEquals(Unit, result)
        }

        @Test
        fun `delete session when session doesn't exist`() {
            every { sessionRepositoryMock.getById(dummySessionId) } returns null
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock)

            val result = sessionService.delete(dummySessionDto)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getById(dummySessionId)
            }
            verify(exactly = 0) {
                sessionRepositoryMock.delete(any())
            }
            verify {
                userRepositoryMock wasNot Called
            }
            assertNull(result)
        }

    }

    @Nested
    inner class LastUser {

        @Test
        fun `set last user when session found`() {
            every { sessionRepositoryMock.getById(dummySessionId) } returns dummySession
            every { sessionRepositoryMock.setLastUser(dummySession, dummyUser) } just Runs
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock)

            sessionService.setLastUser(dummySessionId, dummyUser)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getById(dummySessionId)
                sessionRepositoryMock.setLastUser(dummySession, dummyUser)
            }
        }

        @Test
        fun `set last user null when session found`() {
            every { sessionRepositoryMock.getById(dummySessionId) } returns dummySession
            every { sessionRepositoryMock.setLastUser(dummySession, null) } just Runs
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock)

            sessionService.setLastUser(dummySessionId, null)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getById(dummySessionId)
                sessionRepositoryMock.setLastUser(dummySession, null)
            }
        }

        @Test
        fun `logout user when session not found`() {
            every { sessionRepositoryMock.getById(dummySessionId) } returns null
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock)

            sessionService.setLastUser(dummySessionId, dummyUser)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getById(dummySessionId)
            }
            verify(exactly = 0) {
                sessionRepositoryMock.setLastUser(dummySession, dummyUser)
            }
        }

        @Test
        fun `logout user null when session not found`() {
            every { sessionRepositoryMock.getById(dummySessionId) } returns null
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock)

            sessionService.setLastUser(dummySessionId, null)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getById(dummySessionId)
            }
            verify(exactly = 0) {
                sessionRepositoryMock.setLastUser(dummySession, null)
            }
        }

    }

    @Nested
    inner class Move {

        @Test
        fun `move all users`() {
            mockTransaction()
            every { userRepositoryMock.moveAll(dummySessionFromId, dummySessionToId) } just Runs

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock)

            sessionService.moveAllUsers(dummySessionFrom, dummySessionTo)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userRepositoryMock.moveAll(dummySessionFromId, dummySessionToId)
            }

        }

    }

}
