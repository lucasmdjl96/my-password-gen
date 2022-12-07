/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.test.controller

import com.mypasswordgen.common.dto.client.UserClientDto
import com.mypasswordgen.common.dto.fullClient.FullUserClientDto
import com.mypasswordgen.common.dto.fullServer.FullUserServerDto
import com.mypasswordgen.common.dto.idb.UserIDBDto
import com.mypasswordgen.common.dto.server.UserServerDto
import com.mypasswordgen.common.routes.UserRoute
import com.mypasswordgen.server.controller.impl.UserControllerImpl
import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.plugins.DataConflictException
import com.mypasswordgen.server.plugins.DataNotFoundException
import com.mypasswordgen.server.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import java.util.*

class UserControllerTest : ControllerTestParent() {

    private lateinit var dummyUserClientDto: UserClientDto
    private lateinit var dummySessionDto: SessionDto
    private lateinit var dummyUserServerDto: UserServerDto
    private lateinit var dummyFullUserServerDto: FullUserServerDto
    private lateinit var dummyUserIDBDto: UserIDBDto
    private lateinit var dummyFullUserClientDto: FullUserClientDto

    private lateinit var userServiceMock: UserService
    private lateinit var callMock: ApplicationCall

    @BeforeAll
    override fun initMocks() {
        userServiceMock = mockk()
        callMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummyUserClientDto = UserClientDto(
            "cff48f5f-5b1c-4336-ae25-c80de052f8cf",
            setOf("628a5996-8fa5-417a-99fb-97f12f5acccb", "a5d69d82-5399-43db-b9c1-e380066de0a8")
        )
        dummySessionDto = SessionDto(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        dummyUserServerDto = UserServerDto("userServer123")
        dummyFullUserServerDto = FullUserServerDto("User123")
        dummyUserIDBDto = UserIDBDto("b4d79165-7afa-4bd7-9ce6-444cb9d2fb0c", "UserXYz")
        dummyFullUserClientDto = FullUserClientDto("userAbc")
    }

    @Nested
    inner class Login {

        @Test
        fun `user login when already registered`() = runBlocking {
            every {
                userServiceMock.find(
                    dummyUserServerDto,
                    dummySessionDto.sessionId
                )
            } returns dummyUserClientDto
            mockCall(callMock, dummySessionDto)

            val userController = UserControllerImpl(userServiceMock)

            userController.get(callMock, UserRoute.Login(dummyUserServerDto.username))

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                userServiceMock.find(dummyUserServerDto, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                userServiceMock.find(dummyUserServerDto, dummySessionDto.sessionId)
                callMock.respond(dummyUserClientDto)
            }
        }

        @Test
        fun `user login when not already registered`() = runBlocking {
            every {
                userServiceMock.find(
                    dummyUserServerDto,
                    dummySessionDto.sessionId
                )
            } throws DataNotFoundException()
            mockCall(callMock, dummySessionDto)

            val userController = UserControllerImpl(userServiceMock)

            assertThrows<DataNotFoundException> {
                userController.get(callMock, UserRoute.Login(dummyUserServerDto.username))
            }

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                userServiceMock.find(dummyUserServerDto, dummySessionDto.sessionId)
            }
            coVerify {
                userServiceMock.find(dummyUserServerDto, dummySessionDto.sessionId)
            }
        }

    }

    @Nested
    inner class Register {

        @Test
        fun `user register when not already registered`() = runBlocking {
            every {
                userServiceMock.create(
                    dummyUserServerDto,
                    dummySessionDto.sessionId
                )
            } returns dummyUserClientDto
            mockCall(callMock, dummySessionDto, dummyUserServerDto)

            val userController = UserControllerImpl(userServiceMock)

            userController.post(callMock, UserRoute.Register())

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                userServiceMock.create(dummyUserServerDto, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                callMock.receive<UserServerDto>()
                userServiceMock.create(dummyUserServerDto, dummySessionDto.sessionId)
                callMock.respond(dummyUserClientDto)
            }
        }

        @Test
        fun `user register when already registered`() = runBlocking {
            every {
                userServiceMock.create(
                    dummyUserServerDto,
                    dummySessionDto.sessionId
                )
            } throws DataConflictException()
            mockCall(callMock, dummySessionDto, dummyUserServerDto)

            val userController = UserControllerImpl(userServiceMock)

            assertThrows<DataConflictException> {
                userController.post(callMock, UserRoute.Register())
            }

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                userServiceMock.create(dummyUserServerDto, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                callMock.receive<UserServerDto>()
                userServiceMock.create(dummyUserServerDto, dummySessionDto.sessionId)
            }
        }

    }

    @Nested
    inner class Logout {

        @Test
        fun `logout user`() = runBlocking {
            mockCall(callMock, dummySessionDto, dummyUserServerDto)
            every { userServiceMock.logout(dummyUserServerDto, dummySessionDto.sessionId) } just Runs

            val userController = UserControllerImpl(userServiceMock)

            userController.patch(callMock, UserRoute.Logout())

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                userServiceMock.logout(dummyUserServerDto, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                callMock.receive<UserServerDto>()
                userServiceMock.logout(dummyUserServerDto, dummySessionDto.sessionId)
                callMock.respond(HttpStatusCode.OK)
            }

        }

        @Test
        fun `logout user when bad data`() = runBlocking {
            mockCall(callMock, dummySessionDto, dummyUserServerDto)
            every {
                userServiceMock.logout(
                    dummyUserServerDto,
                    dummySessionDto.sessionId
                )
            } throws DataNotFoundException()

            val userController = UserControllerImpl(userServiceMock)

            assertThrows<DataNotFoundException> {
                userController.patch(callMock, UserRoute.Logout())
            }

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                userServiceMock.logout(dummyUserServerDto, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                callMock.receive<UserServerDto>()
                userServiceMock.logout(dummyUserServerDto, dummySessionDto.sessionId)
            }

        }

    }

    @Nested
    inner class Import {
        @Test
        fun `import when user already exists`() = runBlocking {
            mockCall(callMock, dummySessionDto, dummyFullUserServerDto)
            every {
                userServiceMock.createFullUser(dummyFullUserServerDto, dummySessionDto.sessionId)
            } throws DataConflictException()

            val userController = UserControllerImpl(userServiceMock)
            assertThrows<DataConflictException> {
                userController.post(callMock, UserRoute.Import())
            }

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                userServiceMock.createFullUser(dummyFullUserServerDto, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                callMock.receive<FullUserServerDto>()
                userServiceMock.createFullUser(dummyFullUserServerDto, dummySessionDto.sessionId)
            }
        }

        @Test
        fun `import when user doesn't exist`() = runBlocking {
            mockCall(callMock, dummySessionDto, dummyFullUserServerDto)
            every {
                userServiceMock.createFullUser(dummyFullUserServerDto, dummySessionDto.sessionId)
            } returns dummyUserIDBDto

            val userController = UserControllerImpl(userServiceMock)
            userController.post(callMock, UserRoute.Import())

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                userServiceMock.createFullUser(dummyFullUserServerDto, dummySessionDto.sessionId)
                callMock.respond(dummyUserIDBDto)
            }
            coVerifyOrder {
                callMock.receive<FullUserServerDto>()
                userServiceMock.createFullUser(dummyFullUserServerDto, dummySessionDto.sessionId)
            }
        }
    }

    @Nested
    inner class Export {
        @Test
        fun `export when user already exists`() = runBlocking {
            every {
                userServiceMock.getFullUser(
                    dummyUserServerDto,
                    dummySessionDto.sessionId
                )
            } returns dummyFullUserClientDto
            mockCall(callMock, dummySessionDto)

            val userController = UserControllerImpl(userServiceMock)

            userController.get(callMock, UserRoute.Export(dummyUserServerDto.username))

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                userServiceMock.getFullUser(dummyUserServerDto, dummySessionDto.sessionId)
                callMock.respond(dummyFullUserClientDto)
            }
        }

        @Test
        fun `export when user doesn't exist`() = runBlocking {
            every {
                userServiceMock.getFullUser(
                    dummyUserServerDto,
                    dummySessionDto.sessionId
                )
            } throws DataNotFoundException()
            mockCall(callMock, dummySessionDto)

            val userController = UserControllerImpl(userServiceMock)

            assertThrows<DataNotFoundException> {
                userController.get(callMock, UserRoute.Export(dummyUserServerDto.username))
            }

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                userServiceMock.getFullUser(dummyUserServerDto, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                userServiceMock.getFullUser(dummyUserServerDto, dummySessionDto.sessionId)
            }
        }
    }

}
