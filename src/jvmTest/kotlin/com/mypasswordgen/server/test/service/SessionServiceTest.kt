/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.test.service

import com.mypasswordgen.common.dto.fullClient.FullSessionClientDto
import com.mypasswordgen.common.dto.fullServer.FullSessionServerDto
import com.mypasswordgen.common.dto.fullServer.FullUserServerDto
import com.mypasswordgen.common.dto.idb.UserIDBDto
import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.mapper.SessionMapper
import com.mypasswordgen.server.model.Session
import com.mypasswordgen.server.model.User
import com.mypasswordgen.server.plugins.DataConflictException
import com.mypasswordgen.server.plugins.DataNotFoundException
import com.mypasswordgen.server.repository.SessionRepository
import com.mypasswordgen.server.repository.UserRepository
import com.mypasswordgen.server.service.UserService
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

    private lateinit var userServiceMock: UserService
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

    private lateinit var dummyFullSessionServerDto: FullSessionServerDto
    private lateinit var dummyFullSessionClientDto: FullSessionClientDto
    private lateinit var dummyUserIDBDtoList: List<UserIDBDto>

    @BeforeAll
    override fun initMocks() {
        userServiceMock = mockk()
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

        dummyFullSessionServerDto = FullSessionServerDto(mutableSetOf(
            FullUserServerDto("user123"),
            FullUserServerDto("userAbc")
        ))
        dummyUserIDBDtoList = listOf(
            UserIDBDto("75e218a6-f854-4a23-812b-dcfc2aad05eb", "user089"),
            UserIDBDto("bec73a0a-9fbf-44ec-bf08-b310b969fa7b", "user645")
        )
        dummyFullSessionClientDto = FullSessionClientDto()
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

            val sessionService = SessionServiceImpl(userServiceMock, sessionRepositoryMock, userRepositoryMock, sessionMapperMock)
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

            val sessionService = SessionServiceImpl(userServiceMock, sessionRepositoryMock, userRepositoryMock, sessionMapperMock)
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

            val sessionService = SessionServiceImpl(userServiceMock, sessionRepositoryMock, userRepositoryMock, sessionMapperMock)

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

            val sessionService = SessionServiceImpl(userServiceMock, sessionRepositoryMock, userRepositoryMock, sessionMapperMock)

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

            val sessionService = SessionServiceImpl(userServiceMock, sessionRepositoryMock, userRepositoryMock, sessionMapperMock)

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

            val sessionService = SessionServiceImpl(userServiceMock, sessionRepositoryMock, userRepositoryMock, sessionMapperMock)

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

            val sessionService = SessionServiceImpl(userServiceMock, sessionRepositoryMock, userRepositoryMock, sessionMapperMock)

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

            val sessionService = SessionServiceImpl(userServiceMock, sessionRepositoryMock, userRepositoryMock, sessionMapperMock)

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
    inner class Move {

        @Test
        fun `move all users`() {
            mockTransaction()
            every { userRepositoryMock.moveAll(dummySessionFromId, dummySessionToId) } just Runs

            val sessionService = SessionServiceImpl(userServiceMock, sessionRepositoryMock, userRepositoryMock, sessionMapperMock)

            sessionService.moveAllUsers(dummySessionFrom, dummySessionTo)

            verifyOrder {
                transaction(statement = any<Transaction.() -> Any>())
                userRepositoryMock.moveAll(dummySessionFromId, dummySessionToId)
            }

        }

    }

    @Nested
    inner class GetFullSession {

        @Test
        fun `with existing session`() {
            mockTransaction()
            val sessionService = SessionServiceImpl(userServiceMock, sessionRepositoryMock, userRepositoryMock, sessionMapperMock)
            val sessionServiceSpy = spyk(sessionService)
            every { sessionServiceSpy.find(dummySessionDto) } returns dummySession
            with(sessionMapperMock) {
                every { dummySession.toFullSessionClientDto() } returns dummyFullSessionClientDto
            }

            val result = sessionServiceSpy.getFullSession(dummySessionDto)

            assertEquals(dummyFullSessionClientDto, result)
            verifyOrder {
                sessionServiceSpy.find(dummySessionDto)
                with(sessionMapperMock) {
                    dummySession.toFullSessionClientDto()
                }
            }
        }

        @Test
        fun `with non-existing session`() {
            mockTransaction()
            val sessionService = SessionServiceImpl(userServiceMock, sessionRepositoryMock, userRepositoryMock, sessionMapperMock)
            val sessionServiceSpy = spyk(sessionService)
            every { sessionServiceSpy.find(dummySessionDto) } returns null

            assertThrows<DataNotFoundException> {
                sessionServiceSpy.getFullSession(dummySessionDto)
            }

            verify {
                sessionServiceSpy.find(dummySessionDto)
            }
        }
    }

    @Nested
    inner class CreateFullUser {

        @Test
        fun `create when a user already exists`() {
            mockTransaction()
            val sessionService = SessionServiceImpl(userServiceMock, sessionRepositoryMock, userRepositoryMock, sessionMapperMock)
            val sessionServiceSpy = spyk(sessionService)
            every { sessionRepositoryMock.create() } returns dummySession
            every { sessionServiceSpy.delete(dummySessionDto) } returns Unit
            every {
                userServiceMock.createFullUser(dummyFullSessionServerDto.users.iterator().next(), dummySession.id.value)
            } throws DataConflictException()

            assertThrows<DataConflictException> {
                sessionServiceSpy.createFullSession(dummySessionDto, dummyFullSessionServerDto)
            }

            verify {
                sessionRepositoryMock.create()
            }
        }

        @Test
        fun `create when nothing already exists`() {
            mockTransaction()
            val sessionService = SessionServiceImpl(userServiceMock, sessionRepositoryMock, userRepositoryMock, sessionMapperMock)
            val sessionServiceSpy = spyk(sessionService)
            every { sessionRepositoryMock.create() } returns dummySession
            every { sessionServiceSpy.delete(dummySessionDto) } returns Unit
            dummyFullSessionServerDto.users.forEachIndexed { index, fullUserServerDto ->
                every {
                    userServiceMock.createFullUser(fullUserServerDto, dummySession.id.value)
                } returns dummyUserIDBDtoList[index]
            }
            with(sessionMapperMock) {
                every {
                    dummySession.toSessionDto()
                } returns dummySessionDtoNew
            }

            val result = sessionServiceSpy.createFullSession(dummySessionDto, dummyFullSessionServerDto)

            assertEquals(dummySessionDtoNew, result.first)
            assertEquals(dummyUserIDBDtoList.toSet(), result.second.users)
            verifyOrder {
                sessionRepositoryMock.create()
                with(sessionMapperMock) {
                    dummySession.toSessionDto()
                }
            }
            verifyOrder {
                sessionRepositoryMock.create()
                for (fullUser in dummyFullSessionServerDto.users) {
                    userServiceMock.createFullUser(fullUser, dummySession.id.value)
                }
            }
            verify { sessionServiceSpy.delete(dummySessionDto) }
        }

    }

}
