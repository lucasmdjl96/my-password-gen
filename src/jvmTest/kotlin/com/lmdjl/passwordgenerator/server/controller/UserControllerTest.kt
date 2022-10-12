package com.lmdjl.passwordgenerator.server.controller

import com.lucasmdjl.passwordgenerator.common.dto.client.UserClientDto
import com.lucasmdjl.passwordgenerator.common.dto.server.UserServerDto
import com.lucasmdjl.passwordgenerator.common.routes.UserRoute
import com.lucasmdjl.passwordgenerator.server.controller.impl.UserControllerImpl
import com.lucasmdjl.passwordgenerator.server.crypto.encode
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.mapper.UserMapper
import com.lucasmdjl.passwordgenerator.server.model.User
import com.lucasmdjl.passwordgenerator.server.service.UserService
import com.lucasmdjl.passwordgenerator.server.tables.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.id.EntityID
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

class UserControllerTest : ControllerTestParent() {

    private lateinit var dummyUser: User
    private lateinit var dummyUserClientDto: UserClientDto
    private lateinit var dummySessionDto: SessionDto
    private lateinit var dummyUserServerDto: UserServerDto
    private lateinit var dummyEncodedUserServerDto: UserServerDto

    private lateinit var userServiceMock: UserService
    private lateinit var userMapperMock: UserMapper
    private lateinit var callMock: ApplicationCall

    @BeforeAll
    override fun initMocks() {
        userServiceMock = mockk()
        userMapperMock = mockk()
        callMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummyUser = User(EntityID(1, Users))
        dummyUserClientDto = UserClientDto("userClient123", mutableListOf("email1", "email2"))
        dummySessionDto = SessionDto(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        dummyUserServerDto = UserServerDto("userServer123")
        dummyEncodedUserServerDto = UserServerDto("userServer321")
    }

    @Nested
    inner class Login {

        @Test
        fun `user login when already registered`() = runBlocking {
            mockkStatic("com.lucasmdjl.passwordgenerator.server.crypto.Sha256Kt")
            every { dummyUserServerDto.encode() } returns dummyEncodedUserServerDto
            every { userServiceMock.find(dummyEncodedUserServerDto, dummySessionDto.sessionId) } returns dummyUser
            with(userMapperMock) {
                every { dummyUser.toUserClientDto() } returns dummyUserClientDto
            }
            mockCall(callMock, dummySessionDto, dummyUserServerDto)

            val userController = UserControllerImpl(userServiceMock, userMapperMock)

            userController.post(callMock, UserRoute.Login())

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                userServiceMock.find(dummyEncodedUserServerDto, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                callMock.receive<UserServerDto>()
                dummyUserServerDto.encode()
                userServiceMock.find(dummyEncodedUserServerDto, dummySessionDto.sessionId)
                with(userMapperMock) {
                    dummyUser.toUserClientDto()
                }
                callMock.respondNullable(dummyUserClientDto as UserClientDto?)
            }
        }

        @Test
        fun `user login when not already registered`() = runBlocking {
            mockkStatic("com.lucasmdjl.passwordgenerator.server.crypto.Sha256Kt")
            every { dummyUserServerDto.encode() } returns dummyEncodedUserServerDto
            every { userServiceMock.find(dummyEncodedUserServerDto, dummySessionDto.sessionId) } returns null
            mockCall(callMock, dummySessionDto, dummyUserServerDto)

            val userController = UserControllerImpl(userServiceMock, userMapperMock)

            userController.post(callMock, UserRoute.Login())

            verify(exactly = 0) {
                with(userMapperMock) {
                    any<User>().toUserClientDto()
                }
            }
            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                userServiceMock.find(dummyEncodedUserServerDto, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                callMock.receive<UserServerDto>()
                dummyUserServerDto.encode()
                userServiceMock.find(dummyEncodedUserServerDto, dummySessionDto.sessionId)
                callMock.respondNullable(null as UserClientDto?)
            }
        }

    }

    @Nested
    inner class Register {

        @Test
        fun `user register when not already registered`() = runBlocking {
            mockkStatic("com.lucasmdjl.passwordgenerator.server.crypto.Sha256Kt")
            every { dummyUserServerDto.encode() } returns dummyEncodedUserServerDto
            every { userServiceMock.create(dummyEncodedUserServerDto, dummySessionDto.sessionId) } returns dummyUser
            with(userMapperMock) {
                every { dummyUser.toUserClientDto() } returns dummyUserClientDto
            }
            mockCall(callMock, dummySessionDto, dummyUserServerDto)

            val userController = UserControllerImpl(userServiceMock, userMapperMock)

            userController.post(callMock, UserRoute.Register())

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                userServiceMock.create(dummyEncodedUserServerDto, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                callMock.receive<UserServerDto>()
                dummyUserServerDto.encode()
                userServiceMock.create(dummyEncodedUserServerDto, dummySessionDto.sessionId)
                with(userMapperMock) {
                    dummyUser.toUserClientDto()
                }
                callMock.respondNullable(dummyUserClientDto as UserClientDto?)
            }
        }

        @Test
        fun `user register when already registered`() = runBlocking {
            mockkStatic("com.lucasmdjl.passwordgenerator.server.crypto.Sha256Kt")
            every { dummyUserServerDto.encode() } returns dummyEncodedUserServerDto
            every { userServiceMock.create(dummyEncodedUserServerDto, dummySessionDto.sessionId) } returns null
            mockCall(callMock, dummySessionDto, dummyUserServerDto)

            val userController = UserControllerImpl(userServiceMock, userMapperMock)

            userController.post(callMock, UserRoute.Register())

            verify(exactly = 0) {
                with(userMapperMock) {
                    any<User>().toUserClientDto()
                }
            }
            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                userServiceMock.create(dummyEncodedUserServerDto, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                callMock.receive<UserServerDto>()
                dummyUserServerDto.encode()
                userServiceMock.create(dummyEncodedUserServerDto, dummySessionDto.sessionId)
                callMock.respondNullable(null as UserClientDto?)
            }
        }

    }

    @Nested
    inner class Logout {

        @Test
        fun `logout user`() = runBlocking {
            mockCall(callMock, dummySessionDto, dummyUserServerDto)
            every { userServiceMock.logout(dummyUserServerDto, dummySessionDto.sessionId) } just Runs

            val userController = UserControllerImpl(userServiceMock, userMapperMock)

            userController.patch(callMock, UserRoute.Logout())

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                userServiceMock.logout(dummyUserServerDto, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                callMock.receive<UserServerDto>()
                dummyUserServerDto
                userServiceMock.logout(dummyUserServerDto, dummySessionDto.sessionId)
                callMock.respond(HttpStatusCode.OK)
            }

        }

    }

}
