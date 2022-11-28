/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.test.mapper

import com.mypasswordgen.common.dto.fullClient.FullSessionClientDto
import com.mypasswordgen.common.dto.fullClient.FullUserClientDto
import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.mapper.UserMapper
import com.mypasswordgen.server.mapper.impl.SessionMapperImpl
import com.mypasswordgen.server.model.Session
import com.mypasswordgen.server.model.User
import io.mockk.*
import org.jetbrains.exposed.sql.SizedCollection
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class SessionMapperTest : MapperTestParent() {

    private lateinit var sessionMock: Session
    private lateinit var userListMock: List<User>
    private lateinit var dummyFullUserClientList: List<FullUserClientDto>
    private lateinit var dummySessionId: UUID
    private lateinit var dummySessionDto: SessionDto
    private lateinit var userMapperMock: UserMapper
    private lateinit var dummyFullSessionClientDto: FullSessionClientDto

    @BeforeAll
    override fun initMocks() {
        sessionMock = mockk()
        userListMock = listOf(mockk(), mockk())
        userMapperMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummyFullUserClientList = listOf(FullUserClientDto("user132"), FullUserClientDto("user434"))
        dummySessionId = UUID.fromString("f2e0b5b3-cc9f-4c9e-8715-df4f51a342bf")
        dummySessionDto = SessionDto(dummySessionId)
        dummyFullSessionClientDto = FullSessionClientDto()
    }

    @Nested
    inner class SessionToSessionDto {

        @Test
        fun `with argument`() {
            every { sessionMock.id.value } returns dummySessionId
            val sessionMapper = SessionMapperImpl(userMapperMock)
            val sessionDto = sessionMapper.sessionToSessionDto(sessionMock)
            assertEquals(dummySessionId, sessionDto.sessionId)
        }

        @Test
        fun `with receiver`() {
            val sessionMapper = SessionMapperImpl(userMapperMock)
            val sessionMapperSpy = spyk(sessionMapper)
            every { sessionMapperSpy.sessionToSessionDto(sessionMock) } returns dummySessionDto
            val result = with(sessionMapperSpy) {
                sessionMock.toSessionDto()
            }
            assertEquals(dummySessionDto, result)
            verifySequence {
                with(sessionMapperSpy) {
                    sessionMock.toSessionDto()
                }
                sessionMapperSpy.sessionToSessionDto(sessionMock)
            }
        }

    }

    @Nested
    inner class SessionToFullSessionClientDto {
        @Test
        fun `with argument`() {
            mockTransaction()
            every { sessionMock.users } returns SizedCollection(userListMock)
            userListMock.forEachIndexed { index, user ->
                every {
                    with(userMapperMock) {
                        user.toFullUserClientDto()
                    }
                } returns dummyFullUserClientList[index]
            }

            val sessionMapper = SessionMapperImpl(userMapperMock)

            val result = sessionMapper.sessionToFullSessionClientDto(sessionMock)

            assertEquals(2, result.users.size)
            val userList = result.users.toList()
            for (i in 0..1) {
                assertEquals(dummyFullUserClientList[i], userList[i])
            }
            verifyOrder {
                sessionMock.users
                for (user in userListMock) {
                    with(userMapperMock) {
                        user.toFullUserClientDto()
                    }
                }
            }
        }

        @Test
        fun `with receiver`() {
            val sessionMapper = SessionMapperImpl(userMapperMock)
            val sessionMapperSpy = spyk(sessionMapper)
            every { sessionMapperSpy.sessionToFullSessionClientDto(sessionMock) } returns dummyFullSessionClientDto
            val result = with(sessionMapperSpy) {
                sessionMock.toFullSessionClientDto()
            }

            assertEquals(dummyFullSessionClientDto, result)
            verifySequence {
                with(sessionMapperSpy) {
                    sessionMock.toFullSessionClientDto()
                }
                sessionMapperSpy.sessionToFullSessionClientDto(sessionMock)
            }
        }
    }


}
