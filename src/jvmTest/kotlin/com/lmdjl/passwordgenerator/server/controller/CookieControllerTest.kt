package com.lmdjl.passwordgenerator.server.controller

import com.lucasmdjl.passwordgenerator.common.routes.CookieRoute
import com.lucasmdjl.passwordgenerator.server.controller.impl.CookieControllerImpl
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.service.SessionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

class CookieControllerTest : ControllerTestParent() {

    private lateinit var sessionServiceMock: SessionService
    private lateinit var callMock: ApplicationCall

    private lateinit var dummySessionDto: SessionDto

    @BeforeAll
    override fun initMocks() {
        sessionServiceMock = mockk()
        callMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummySessionDto = SessionDto(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
    }

    @Nested
    inner class OptOut {

        @Test
        fun `opt out when session stored`() = runBlocking {
            every { sessionServiceMock.delete(dummySessionDto) } returns Unit
            mockCall(callMock, dummySessionDto)
            mockkStatic("io.ktor.server.response.ApplicationResponseFunctionsKt")
            every { callMock.sessions.clear(any()) } just Runs
            coEvery { callMock.respondText(any(), any()) } just Runs

            val cookieController = CookieControllerImpl(sessionServiceMock)

            cookieController.get(callMock, CookieRoute.OptOut())

            coVerify {
                callMock.sessions.get<SessionDto>()
                sessionServiceMock.delete(dummySessionDto)
                callMock.sessions.clear<SessionDto>()
                callMock.respondText(any(), ContentType.Text.Html)
            }
        }

        @Test
        fun `opt out when session not stored`() = runBlocking {
            mockCall(callMock, dummySessionDto)
            every { callMock.sessions.get<SessionDto>() } returns null
            mockkStatic("io.ktor.server.response.ApplicationResponseFunctionsKt")
            coEvery { callMock.respondText(any(), any()) } just Runs

            val cookieController = CookieControllerImpl(sessionServiceMock)

            cookieController.get(callMock, CookieRoute.OptOut())

            verify(exactly = 0) {
                sessionServiceMock.delete(any())
            }
            with(callMock.sessions) {
                verify(exactly = 0) {
                    clear<SessionDto>()
                }
            }
            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                callMock.respondText(any(), ContentType.Text.Html)
            }
        }

    }

    @Nested
    inner class Policy {

        @Test
        fun `get policy page`() = runBlocking {
            mockkStatic("io.ktor.server.response.ApplicationResponseFunctionsKt")
            coEvery { callMock.respondText(any(), any()) } just Runs

            val cookieController = CookieControllerImpl(sessionServiceMock)

            cookieController.get(callMock, CookieRoute.Policy())
            coVerify { callMock.respondText(any(), ContentType.Text.Html) }
        }

    }

}
