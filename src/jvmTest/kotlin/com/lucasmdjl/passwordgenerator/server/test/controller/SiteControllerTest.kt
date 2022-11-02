package com.lucasmdjl.passwordgenerator.server.test.controller

import com.lucasmdjl.passwordgenerator.common.dto.client.SiteClientDto
import com.lucasmdjl.passwordgenerator.common.dto.server.SiteServerDto
import com.lucasmdjl.passwordgenerator.common.routes.SiteRoute
import com.lucasmdjl.passwordgenerator.server.controller.impl.SiteControllerImpl
import com.lucasmdjl.passwordgenerator.server.crypto.encode
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.plugins.DataConflictException
import com.lucasmdjl.passwordgenerator.server.plugins.DataNotFoundException
import com.lucasmdjl.passwordgenerator.server.service.SiteService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import java.util.*

class SiteControllerTest : ControllerTestParent() {

    private lateinit var siteServiceMock: SiteService
    private lateinit var callMock: ApplicationCall

    private lateinit var dummySessionDto: SessionDto
    private lateinit var dummySiteClientDto: SiteClientDto
    private lateinit var dummySiteServerDto: SiteServerDto
    private lateinit var dummySiteServerDtoEncoded: SiteServerDto

    @BeforeAll
    override fun initMocks() {
        siteServiceMock = mockk()
        callMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummySessionDto = SessionDto(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        dummySiteClientDto = SiteClientDto("3db7d073-c96b-4a2f-b959-1cdd4eef7e99")
        dummySiteServerDto = SiteServerDto("siteAbc")
        dummySiteServerDtoEncoded = SiteServerDto("siteCba")
    }

    @Nested
    inner class New {

        @Test
        fun `create new site when it doesn't exist`() = runBlocking {
            mockkStatic("com.lucasmdjl.passwordgenerator.server.crypto.Sha256Kt")
            every { dummySiteServerDto.encode() } returns dummySiteServerDtoEncoded
            every {
                siteServiceMock.create(
                    dummySiteServerDtoEncoded,
                    dummySessionDto.sessionId
                )
            } returns dummySiteClientDto
            mockCall(callMock, dummySessionDto, dummySiteServerDto)

            val siteController = SiteControllerImpl(siteServiceMock)

            siteController.post(callMock, SiteRoute.New())

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                siteServiceMock.create(dummySiteServerDtoEncoded, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                callMock.receive<SiteServerDto>()
                dummySiteServerDto.encode()
                siteServiceMock.create(dummySiteServerDtoEncoded, dummySessionDto.sessionId)
                callMock.respond(dummySiteClientDto)
            }
        }

        @Test
        fun `create new site when it already exists`() = runBlocking {
            mockkStatic("com.lucasmdjl.passwordgenerator.server.crypto.Sha256Kt")
            every { dummySiteServerDto.encode() } returns dummySiteServerDtoEncoded
            every {
                siteServiceMock.create(
                    dummySiteServerDtoEncoded,
                    dummySessionDto.sessionId
                )
            } throws DataConflictException()
            mockCall(callMock, dummySessionDto, dummySiteServerDto)

            val siteController = SiteControllerImpl(siteServiceMock)

            assertThrows<DataConflictException> {
                siteController.post(callMock, SiteRoute.New())
            }

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                siteServiceMock.create(dummySiteServerDtoEncoded, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                callMock.receive<SiteServerDto>()
                dummySiteServerDto.encode()
                siteServiceMock.create(dummySiteServerDtoEncoded, dummySessionDto.sessionId)
            }
        }

    }

    @Nested
    inner class Find {

        @Test
        fun `find site when it already exists`() = runBlocking {
            mockkStatic("com.lucasmdjl.passwordgenerator.server.crypto.Sha256Kt")
            every { dummySiteServerDto.encode() } returns dummySiteServerDtoEncoded
            every {
                siteServiceMock.find(
                    dummySiteServerDtoEncoded,
                    dummySessionDto.sessionId
                )
            } returns dummySiteClientDto
            mockCall(callMock, dummySessionDto)

            val siteController = SiteControllerImpl(siteServiceMock)

            siteController.get(callMock, SiteRoute.Find(dummySiteServerDto.siteName))

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                dummySiteServerDto.encode()
                siteServiceMock.find(dummySiteServerDtoEncoded, dummySessionDto.sessionId)
                callMock.respond(dummySiteClientDto)
            }
        }

        @Test
        fun `find site when it doesn't exist`() = runBlocking {
            mockkStatic("com.lucasmdjl.passwordgenerator.server.crypto.Sha256Kt")
            every { dummySiteServerDto.encode() } returns dummySiteServerDtoEncoded
            every {
                siteServiceMock.find(
                    dummySiteServerDtoEncoded,
                    dummySessionDto.sessionId
                )
            } throws DataNotFoundException()
            mockCall(callMock, dummySessionDto)

            val siteController = SiteControllerImpl(siteServiceMock)

            assertThrows<DataNotFoundException> {
                siteController.get(callMock, SiteRoute.Find(dummySiteServerDto.siteName))
            }

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                dummySiteServerDto.encode()
                siteServiceMock.find(dummySiteServerDtoEncoded, dummySessionDto.sessionId)
            }
        }

    }

    @Nested
    inner class Delete {
        @Test
        fun `delete site when it already exists`() = runBlocking {
            mockkStatic("com.lucasmdjl.passwordgenerator.server.crypto.Sha256Kt")
            every { dummySiteServerDto.encode() } returns dummySiteServerDtoEncoded
            every {
                siteServiceMock.delete(
                    dummySiteServerDtoEncoded,
                    dummySessionDto.sessionId
                )
            } returns dummySiteClientDto
            mockCall(callMock, dummySessionDto)

            val siteController = SiteControllerImpl(siteServiceMock)

            siteController.delete(callMock, SiteRoute.Delete(dummySiteServerDto.siteName))

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                dummySiteServerDto.encode()
                siteServiceMock.delete(dummySiteServerDtoEncoded, dummySessionDto.sessionId)
                callMock.respond(dummySiteClientDto)
            }
        }

        @Test
        fun `delete site when it doesn't exist`() = runBlocking {
            mockkStatic("com.lucasmdjl.passwordgenerator.server.crypto.Sha256Kt")
            every { dummySiteServerDto.encode() } returns dummySiteServerDtoEncoded
            every {
                siteServiceMock.delete(
                    dummySiteServerDtoEncoded,
                    dummySessionDto.sessionId
                )
            } throws DataNotFoundException()
            mockCall(callMock, dummySessionDto)

            val siteController = SiteControllerImpl(siteServiceMock)

            assertThrows<DataNotFoundException> {
                siteController.delete(callMock, SiteRoute.Delete(dummySiteServerDto.siteName))
            }

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                dummySiteServerDto.encode()
                siteServiceMock.delete(dummySiteServerDtoEncoded, dummySessionDto.sessionId)
            }
        }

    }

}
