package com.lmdjl.passwordgenerator.server.service

import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.model.Session
import com.lucasmdjl.passwordgenerator.server.repository.SessionRepository
import com.lucasmdjl.passwordgenerator.server.service.UserService
import com.lucasmdjl.passwordgenerator.server.service.impl.SessionServiceImpl
import com.lucasmdjl.passwordgenerator.server.tables.Sessions
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
    private lateinit var userServiceMock: UserService

    private lateinit var dummySessionDto: SessionDto
    private lateinit var dummySessionId: UUID
    private lateinit var dummySessionId1: UUID
    private lateinit var dummySession: Session
    private lateinit var dummySessionIdNew: UUID
    private lateinit var dummySessionNew: Session

    @BeforeAll
    override fun initMocks() {
        sessionRepositoryMock = mockk()
        userServiceMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummySessionId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        dummySessionDto = SessionDto(dummySessionId)
        dummySessionId1 = UUID.fromString("123e4583-e89b-12d3-a456-426614174000")
        dummySession = Session(EntityID(dummySessionId1, Sessions))
        dummySessionIdNew = UUID.fromString("3c084a2e-b46e-45dd-a08e-cbba4ce17d49")
        dummySessionNew = Session(EntityID(dummySessionIdNew, Sessions))
    }

    @Nested
    inner class AssignNew {

        @Test
        fun `assign new session from existing session`() {
            every { sessionRepositoryMock.create() } returns dummySessionNew
            every { sessionRepositoryMock.getById(dummySessionId) } returns dummySession
            every { userServiceMock.moveAllUsers(dummySession, dummySessionNew) } just Runs
            every { sessionRepositoryMock.delete(dummySession) } just Runs
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userServiceMock)

            sessionService.assignNew(dummySessionDto)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.create()
                userServiceMock.moveAllUsers(dummySession, dummySessionNew)
                sessionRepositoryMock.delete(dummySession)
            }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getById(dummySessionId)
                userServiceMock.moveAllUsers(dummySession, dummySessionNew)
                sessionRepositoryMock.delete(dummySession)
            }
        }

        @Test
        fun `assign new session from non-existing session`() {
            every { sessionRepositoryMock.create() } returns dummySessionNew
            every { sessionRepositoryMock.getById(dummySessionId) } returns null
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userServiceMock)

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
                userServiceMock wasNot Called
            }
        }

        @Test
        fun `assign new session from no session`() {
            every { sessionRepositoryMock.create() } returns dummySessionNew
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userServiceMock)

            sessionService.assignNew(null)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.create()
            }
            verify(exactly = 0) {
                sessionRepositoryMock.delete(any())
            }
            verify {
                userServiceMock wasNot Called
            }
        }
    }

    @Nested
    inner class Find {

        @Test
        fun `find session when session exist`() {
            every { sessionRepositoryMock.getById(dummySessionId) } returns dummySession
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userServiceMock)

            val sessionResult = sessionService.find(dummySessionDto)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getById(dummySessionId)
            }
            verify {
                userServiceMock wasNot Called
            }
            assertEquals(dummySession, sessionResult)
        }

        @Test
        fun `find session when session doesn't exist`() {
            every { sessionRepositoryMock.getById(dummySessionId) } returns null
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userServiceMock)

            val sessionResult = sessionService.find(dummySessionDto)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getById(dummySessionId)
            }
            verify {
                userServiceMock wasNot Called
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

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userServiceMock)

            val result = sessionService.delete(dummySessionDto)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getById(dummySessionId)
                sessionRepositoryMock.delete(dummySession)
            }
            verify {
                userServiceMock wasNot Called
            }
            assertEquals(Unit, result)
        }

        @Test
        fun `delete session when session doesn't exist`() {
            every { sessionRepositoryMock.getById(dummySessionId) } returns null
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userServiceMock)

            val result = sessionService.delete(dummySessionDto)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getById(dummySessionId)
            }
            verify(exactly = 0) {
                sessionRepositoryMock.delete(any())
            }
            verify {
                userServiceMock wasNot Called
            }
            assertNull(result)
        }

    }

}
