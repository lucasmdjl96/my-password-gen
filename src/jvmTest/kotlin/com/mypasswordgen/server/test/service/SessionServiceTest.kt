package com.mypasswordgen.server.test.service

import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.mapper.SessionMapper
import com.mypasswordgen.server.model.Session
import com.mypasswordgen.server.model.User
import com.mypasswordgen.server.repository.SessionRepository
import com.mypasswordgen.server.repository.UserRepository
import com.mypasswordgen.server.service.impl.SessionServiceImpl
import com.mypasswordgen.server.tables.Sessions
import com.mypasswordgen.server.tables.Users
import io.mockk.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SessionServiceTest : ServiceTestParent() {

    private lateinit var sessionRepositoryMock: SessionRepository
    private lateinit var userRepositoryMock: UserRepository
    private lateinit var sessionMapperMock: SessionMapper

    private lateinit var dummySessionDto: SessionDto
    private lateinit var dummySessionDtoNew: SessionDto
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
        sessionMapperMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummySessionId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        dummySessionDto = SessionDto(dummySessionId)
        dummySessionDtoNew = SessionDto(UUID.fromString("8a6c9cfb-c7e2-41e1-946a-244eea37dc85"))
        dummySessionId1 = UUID.fromString("123e4583-e89b-12d3-a456-426614174000")
        dummySession = Session(EntityID(dummySessionId1, Sessions))
        dummySessionIdNew = UUID.fromString("3c084a2e-b46e-45dd-a08e-cbba4ce17d49")
        dummySessionNew = Session(EntityID(dummySessionIdNew, Sessions))
        dummyUser = User(EntityID(UUID.fromString("8af39a64-0a75-4334-bf8e-9a30f8124819"), Users))

        dummySessionFromId = UUID.fromString("123e4567-e89b-13d2-a456-426614174000")
        dummySessionToId = UUID.fromString("123e4567-eb98-12d3-a456-426614174000")
        dummySessionFrom = Session(EntityID(dummySessionFromId, Sessions))
        dummySessionTo = Session(EntityID(dummySessionToId, Sessions))
    }

    @Nested
    inner class AssignNew {

        @Test
        fun `assign new session from existing session with last user`() {
            every { sessionRepositoryMock.create() } returns dummySessionNew
            every { sessionRepositoryMock.getById(dummySessionId) } returns dummySession
            every { sessionRepositoryMock.getLastUser(dummySession) } returns dummyUser
            every { userRepositoryMock.setLastEmail(dummyUser, null) } just Runs
            every { sessionRepositoryMock.delete(dummySession) } just Runs
            with(sessionMapperMock) {
                every { dummySessionNew.toSessionDto() } returns dummySessionDtoNew
            }
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock, sessionMapperMock)
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
                with(sessionMapperMock) {
                    dummySessionNew.toSessionDto()
                }
            }
            verifyOrder {
                sessionRepositoryMock.getById(dummySessionId)
                sessionRepositoryMock.getLastUser(dummySession)
                userRepositoryMock.setLastEmail(dummyUser, null)
            }
        }

        @Test
        fun `assign new session from existing session without last user`() {
            every { sessionRepositoryMock.create() } returns dummySessionNew
            every { sessionRepositoryMock.getById(dummySessionId) } returns dummySession
            every { sessionRepositoryMock.getLastUser(dummySession) } returns null
            every { sessionRepositoryMock.delete(dummySession) } just Runs
            with(sessionMapperMock) {
                every { dummySessionNew.toSessionDto() } returns dummySessionDtoNew
            }
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock, sessionMapperMock)
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
                with(sessionMapperMock) {
                    dummySessionNew.toSessionDto()
                }
            }
            verifyOrder {
                sessionRepositoryMock.getById(dummySessionId)
                sessionRepositoryMock.getLastUser(dummySession)
            }
            verify {
                userRepositoryMock wasNot Called
            }
        }

        @Test
        fun `assign new session from non-existing session`() {
            every { sessionRepositoryMock.create() } returns dummySessionNew
            every { sessionRepositoryMock.getById(dummySessionId) } returns null
            with(sessionMapperMock) {
                every { dummySessionNew.toSessionDto() } returns dummySessionDtoNew
            }
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock, sessionMapperMock)

            sessionService.assignNew(dummySessionDto)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.create()
                with(sessionMapperMock) {
                    dummySessionNew.toSessionDto()
                }
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
            with(sessionMapperMock) {
                every { dummySessionNew.toSessionDto() } returns dummySessionDtoNew
            }
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock, sessionMapperMock)

            sessionService.assignNew(null)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.create()
                with(sessionMapperMock) {
                    dummySessionNew.toSessionDto()
                }
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

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock, sessionMapperMock)

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

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock, sessionMapperMock)

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

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock, sessionMapperMock)

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

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock, sessionMapperMock)

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
    inner class SetLastUser {

        @Test
        fun `set last user when session found`() {
            every { sessionRepositoryMock.getById(dummySessionId) } returns dummySession
            every { sessionRepositoryMock.setLastUser(dummySession, dummyUser) } just Runs
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock, sessionMapperMock)

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

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock, sessionMapperMock)

            sessionService.setLastUser(dummySessionId, null)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getById(dummySessionId)
                sessionRepositoryMock.setLastUser(dummySession, null)
            }
        }

        @Test
        fun `set last user when session not found`() {
            every { sessionRepositoryMock.getById(dummySessionId) } returns null
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock, sessionMapperMock)

            assertThrows<Exception> { sessionService.setLastUser(dummySessionId, dummyUser) }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getById(dummySessionId)
            }
            verify(exactly = 0) {
                sessionRepositoryMock.setLastUser(dummySession, dummyUser)
            }
        }

        @Test
        fun `set last user null when session not found`() {
            every { sessionRepositoryMock.getById(dummySessionId) } returns null
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock, sessionMapperMock)

            assertThrows<Exception> { sessionService.setLastUser(dummySessionId, null) }
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
    inner class GetLastUser {

        @Test
        fun `get existing last user when session found`() {
            every { sessionRepositoryMock.getById(dummySessionId) } returns dummySession
            every { sessionRepositoryMock.getLastUser(dummySession) } returns dummyUser
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock, sessionMapperMock)

            val userResult = sessionService.getLastUser(dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getById(dummySessionId)
                sessionRepositoryMock.getLastUser(dummySession)
            }
            assertEquals(dummyUser, userResult)
        }

        @Test
        fun `get non-existing last user when session found`() {
            every { sessionRepositoryMock.getById(dummySessionId) } returns dummySession
            every { sessionRepositoryMock.getLastUser(dummySession) } returns null
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock, sessionMapperMock)

            val userResult = sessionService.getLastUser(dummySessionId)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getById(dummySessionId)
                sessionRepositoryMock.getLastUser(dummySession)
            }
            assertEquals(null, userResult)
        }

        @Test
        fun `get last user when session not found`() {
            every { sessionRepositoryMock.getById(dummySessionId) } returns null
            mockTransaction()

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock, sessionMapperMock)

            assertThrows<Exception> { sessionService.getLastUser(dummySessionId) }
            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                sessionRepositoryMock.getById(dummySessionId)
            }
            verify(exactly = 0) {
                sessionRepositoryMock.getLastUser(dummySession)
            }
        }

    }

    @Nested
    inner class Move {

        @Test
        fun `move all users`() {
            mockTransaction()
            every { userRepositoryMock.moveAll(dummySessionFromId, dummySessionToId) } just Runs

            val sessionService = SessionServiceImpl(sessionRepositoryMock, userRepositoryMock, sessionMapperMock)

            sessionService.moveAllUsers(dummySessionFrom, dummySessionTo)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userRepositoryMock.moveAll(dummySessionFromId, dummySessionToId)
            }

        }

    }

}
