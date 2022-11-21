package com.mypasswordgen.server.test.controller

import com.mypasswordgen.common.dto.client.EmailClientDto
import com.mypasswordgen.common.dto.server.EmailServerDto
import com.mypasswordgen.common.routes.EmailRoute
import com.mypasswordgen.server.controller.impl.EmailControllerImpl
import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.plugins.DataConflictException
import com.mypasswordgen.server.plugins.DataNotFoundException
import com.mypasswordgen.server.service.EmailService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import java.util.*

class EmailControllerTest : ControllerTestParent() {

    private lateinit var emailServiceMock: EmailService
    private lateinit var callMock: ApplicationCall

    private lateinit var dummySessionDto: SessionDto
    private lateinit var dummyEmailClientDto: EmailClientDto
    private lateinit var dummyEmailServerDto: EmailServerDto

    @BeforeAll
    override fun initMocks() {
        emailServiceMock = mockk()
        callMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummySessionDto = SessionDto(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        dummyEmailClientDto = EmailClientDto(
            "60a1ec64-374e-4b13-be94-e70d61cec48d",
            listOf("ef48a78f-6ca8-4bab-9b8b-1ee34118b07d", "bfa6f332-8854-4b31-bc40-fdeca43f027f")
        )
        dummyEmailServerDto = EmailServerDto("email@email.com")
    }

    @Nested
    inner class New {

        @Test
        fun `create new email when it doesn't exist`() = runBlocking {
            every {
                emailServiceMock.create(
                    dummyEmailServerDto,
                    dummySessionDto.sessionId
                )
            } returns dummyEmailClientDto
            mockCall(callMock, dummySessionDto, dummyEmailServerDto)

            val emailController = EmailControllerImpl(emailServiceMock)

            emailController.post(callMock, EmailRoute.New())

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                emailServiceMock.create(dummyEmailServerDto, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                callMock.receive<EmailServerDto>()
                emailServiceMock.create(dummyEmailServerDto, dummySessionDto.sessionId)
                callMock.respond(dummyEmailClientDto)
            }
        }

        @Test
        fun `create new email when it already exists`() = runBlocking {
            every {
                emailServiceMock.create(
                    dummyEmailServerDto,
                    dummySessionDto.sessionId
                )
            } throws DataConflictException()
            mockCall(callMock, dummySessionDto, dummyEmailServerDto)

            val emailController = EmailControllerImpl(emailServiceMock)

            assertThrows<DataConflictException> {
                emailController.post(callMock, EmailRoute.New())
            }

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                emailServiceMock.create(dummyEmailServerDto, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                callMock.receive<EmailServerDto>()
                emailServiceMock.create(dummyEmailServerDto, dummySessionDto.sessionId)
            }
        }

    }

    @Nested
    inner class Find {

        @Test
        fun `find email when it already exists`() = runBlocking {
            every {
                emailServiceMock.find(
                    dummyEmailServerDto,
                    dummySessionDto.sessionId
                )
            } returns dummyEmailClientDto
            mockCall(callMock, dummySessionDto)

            val emailController = EmailControllerImpl(emailServiceMock)

            emailController.get(
                callMock,
                EmailRoute.Find(dummyEmailServerDto.emailAddress)
            )

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                emailServiceMock.find(dummyEmailServerDto, dummySessionDto.sessionId)
                callMock.respond(dummyEmailClientDto)
            }
        }

        @Test
        fun `find email when it doesn't exist`() = runBlocking {
            every {
                emailServiceMock.find(
                    dummyEmailServerDto,
                    dummySessionDto.sessionId
                )
            } throws DataNotFoundException()
            mockCall(callMock, dummySessionDto)

            val emailController = EmailControllerImpl(emailServiceMock)

            assertThrows<DataNotFoundException> {
                emailController.get(callMock, EmailRoute.Find(dummyEmailServerDto.emailAddress))
            }

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                emailServiceMock.find(dummyEmailServerDto, dummySessionDto.sessionId)
            }
        }

    }

    @Nested
    inner class Delete {
        @Test
        fun `delete email when it already exists`() = runBlocking {
            every {
                emailServiceMock.delete(
                    dummyEmailServerDto,
                    dummySessionDto.sessionId
                )
            } returns dummyEmailClientDto
            mockCall(callMock, dummySessionDto)

            val emailController = EmailControllerImpl(emailServiceMock)

            emailController.delete(
                callMock,
                EmailRoute.Delete(dummyEmailServerDto.emailAddress)
            )

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                emailServiceMock.delete(dummyEmailServerDto, dummySessionDto.sessionId)
                callMock.respond(dummyEmailClientDto)
            }
        }

        @Test
        fun `delete email when it doesn't exist`() = runBlocking {
            every {
                emailServiceMock.delete(
                    dummyEmailServerDto,
                    dummySessionDto.sessionId
                )
            } throws DataNotFoundException()
            mockCall(callMock, dummySessionDto)

            val emailController = EmailControllerImpl(emailServiceMock)

            assertThrows<DataNotFoundException> {
                emailController.delete(callMock, EmailRoute.Delete(dummyEmailServerDto.emailAddress))
            }

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                emailServiceMock.delete(dummyEmailServerDto, dummySessionDto.sessionId)
            }
        }
    }

}
