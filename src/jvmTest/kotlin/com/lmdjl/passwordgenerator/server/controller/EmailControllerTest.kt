package com.lmdjl.passwordgenerator.server.controller

import com.lucasmdjl.passwordgenerator.common.dto.client.EmailClientDto
import com.lucasmdjl.passwordgenerator.common.dto.server.EmailServerDto
import com.lucasmdjl.passwordgenerator.common.routes.EmailRoute
import com.lucasmdjl.passwordgenerator.server.controller.impl.EmailControllerImpl
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.mapper.EmailMapper
import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.service.EmailService
import com.lucasmdjl.passwordgenerator.server.tables.Emails
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.id.EntityID
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

class EmailControllerTest : ControllerTestParent() {

    private lateinit var emailServiceMock: EmailService
    private lateinit var emailMapperMock: EmailMapper
    private lateinit var callMock: ApplicationCall

    private lateinit var dummySessionDto: SessionDto
    private lateinit var dummyEmail: Email
    private lateinit var dummyEmailServerDto: EmailServerDto
    private lateinit var dummyEmailClientDto: EmailClientDto

    @BeforeAll
    override fun initMocks() {
        emailServiceMock = mockk()
        emailMapperMock = mockk()
        callMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummySessionDto = SessionDto(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        dummyEmail = Email(EntityID(1, Emails))
        dummyEmailServerDto = EmailServerDto("email@email.com")
        dummyEmailClientDto = EmailClientDto("gmail@email.com", mutableListOf("site1", "site2"))
    }

    @Nested
    inner class New {

        @Test
        fun `create new email when it doesn't exist`() = runBlocking {
            every { emailServiceMock.create(dummyEmailServerDto, dummySessionDto.sessionId) } returns dummyEmail
            with(emailMapperMock) {
                every { dummyEmail.toEmailClientDto() } returns dummyEmailClientDto
            }
            mockCall(callMock, dummySessionDto, dummyEmailServerDto)

            val emailController = EmailControllerImpl(emailServiceMock, emailMapperMock)

            emailController.post(callMock, EmailRoute.New())

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                emailServiceMock.create(dummyEmailServerDto, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                callMock.receive<EmailServerDto>()
                emailServiceMock.create(dummyEmailServerDto, dummySessionDto.sessionId)
                with(emailMapperMock) {
                    dummyEmail.toEmailClientDto()
                }
                callMock.respondNullable(dummyEmailClientDto as EmailClientDto?)
            }
        }

        @Test
        fun `create new email when it already exists`() = runBlocking {
            every { emailServiceMock.create(dummyEmailServerDto, dummySessionDto.sessionId) } returns null
            mockCall(callMock, dummySessionDto, dummyEmailServerDto)

            val emailController = EmailControllerImpl(emailServiceMock, emailMapperMock)

            emailController.post(callMock, EmailRoute.New())

            verify(exactly = 0) {
                with(emailMapperMock) {
                    any<Email>().toEmailClientDto()
                }
            }
            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                emailServiceMock.create(dummyEmailServerDto, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                callMock.receive<EmailServerDto>()
                emailServiceMock.create(dummyEmailServerDto, dummySessionDto.sessionId)
                callMock.respondNullable(null as EmailClientDto?)
            }
        }

    }

    @Nested
    inner class Find {

        @Test
        fun `find email when it already exists`() = runBlocking {
            every { emailServiceMock.find(dummyEmailServerDto, dummySessionDto.sessionId) } returns dummyEmail
            with(emailMapperMock) {
                every { dummyEmail.toEmailClientDto() } returns dummyEmailClientDto
            }
            mockCall(callMock, dummySessionDto)

            val emailController = EmailControllerImpl(emailServiceMock, emailMapperMock)

            emailController.get(
                callMock,
                EmailRoute.Find(dummyEmailServerDto.emailAddress)
            )

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                emailServiceMock.find(dummyEmailServerDto, dummySessionDto.sessionId)
                with(emailMapperMock) {
                    dummyEmail.toEmailClientDto()
                }
                callMock.respondNullable(dummyEmailClientDto as EmailClientDto?)
            }
        }

        @Test
        fun `find email when it doesn't exist`() = runBlocking {
            every { emailServiceMock.find(dummyEmailServerDto, dummySessionDto.sessionId) } returns null
            mockCall(callMock, dummySessionDto)

            val emailController = EmailControllerImpl(emailServiceMock, emailMapperMock)

            emailController.get(
                callMock,
                EmailRoute.Find(dummyEmailServerDto.emailAddress)
            )

            verify(exactly = 0) {
                with(emailMapperMock) {
                    any<Email>().toEmailClientDto()
                }
            }
            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                emailServiceMock.find(dummyEmailServerDto, dummySessionDto.sessionId)
                callMock.respondNullable(null as EmailClientDto?)
            }
        }

    }

    @Nested
    inner class Delete {
        @Test
        fun `delete email when it already exists`() = runBlocking {
            every { emailServiceMock.delete(dummyEmailServerDto, dummySessionDto.sessionId) } returns Unit
            mockCall(callMock, dummySessionDto)

            val emailController = EmailControllerImpl(emailServiceMock, emailMapperMock)

            emailController.delete(
                callMock,
                EmailRoute.Delete(dummyEmailServerDto.emailAddress)
            )

            verify(exactly = 0) {
                with(emailMapperMock) {
                    any<Email>().toEmailClientDto()
                }
            }
            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                emailServiceMock.delete(dummyEmailServerDto, dummySessionDto.sessionId)
                callMock.respondNullable(Unit as Unit?)
            }
        }

        @Test
        fun `delete email when it doesn't exist`() = runBlocking {
            every { emailServiceMock.delete(dummyEmailServerDto, dummySessionDto.sessionId) } returns null
            mockCall(callMock, dummySessionDto)

            val emailController = EmailControllerImpl(emailServiceMock, emailMapperMock)

            emailController.delete(
                callMock,
                EmailRoute.Delete(dummyEmailServerDto.emailAddress)
            )

            verify(exactly = 0) {
                with(emailMapperMock) {
                    any<Email>().toEmailClientDto()
                }
            }
            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                emailServiceMock.delete(dummyEmailServerDto, dummySessionDto.sessionId)
                callMock.respondNullable(null as Unit?)
            }
        }
    }

}
