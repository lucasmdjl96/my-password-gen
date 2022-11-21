package com.mypasswordgen.server.test.controller

import com.mypasswordgen.common.dto.client.SiteClientDto
import com.mypasswordgen.common.dto.server.SiteServerDto
import com.mypasswordgen.common.routes.SiteRoute
import com.mypasswordgen.server.controller.impl.SiteControllerImpl
import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.plugins.DataConflictException
import com.mypasswordgen.server.plugins.DataNotFoundException
import com.mypasswordgen.server.service.SiteService
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

class SiteControllerTest : ControllerTestParent() {

    private lateinit var siteServiceMock: SiteService
    private lateinit var callMock: ApplicationCall

    private lateinit var dummySessionDto: SessionDto
    private lateinit var dummySiteClientDto: SiteClientDto
    private lateinit var dummySiteServerDto: SiteServerDto

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
    }

    @Nested
    inner class New {

        @Test
        fun `create new site when it doesn't exist`() = runBlocking {
            every {
                siteServiceMock.create(
                    dummySiteServerDto,
                    dummySessionDto.sessionId
                )
            } returns dummySiteClientDto
            mockCall(callMock, dummySessionDto, dummySiteServerDto)

            val siteController = SiteControllerImpl(siteServiceMock)

            siteController.post(callMock, SiteRoute.New())

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                siteServiceMock.create(dummySiteServerDto, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                callMock.receive<SiteServerDto>()
                siteServiceMock.create(dummySiteServerDto, dummySessionDto.sessionId)
                callMock.respond(dummySiteClientDto)
            }
        }

        @Test
        fun `create new site when it already exists`() = runBlocking {
            every {
                siteServiceMock.create(
                    dummySiteServerDto,
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
                siteServiceMock.create(dummySiteServerDto, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                callMock.receive<SiteServerDto>()
                siteServiceMock.create(dummySiteServerDto, dummySessionDto.sessionId)
            }
        }

    }

    @Nested
    inner class Find {

        @Test
        fun `find site when it already exists`() = runBlocking {
            every {
                siteServiceMock.find(
                    dummySiteServerDto,
                    dummySessionDto.sessionId
                )
            } returns dummySiteClientDto
            mockCall(callMock, dummySessionDto)

            val siteController = SiteControllerImpl(siteServiceMock)

            siteController.get(callMock, SiteRoute.Find(dummySiteServerDto.siteName))

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                siteServiceMock.find(dummySiteServerDto, dummySessionDto.sessionId)
                callMock.respond(dummySiteClientDto)
            }
        }

        @Test
        fun `find site when it doesn't exist`() = runBlocking {
            every {
                siteServiceMock.find(
                    dummySiteServerDto,
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
                siteServiceMock.find(dummySiteServerDto, dummySessionDto.sessionId)
            }
        }

    }

    @Nested
    inner class Delete {
        @Test
        fun `delete site when it already exists`() = runBlocking {
            every {
                siteServiceMock.delete(
                    dummySiteServerDto,
                    dummySessionDto.sessionId
                )
            } returns dummySiteClientDto
            mockCall(callMock, dummySessionDto)

            val siteController = SiteControllerImpl(siteServiceMock)

            siteController.delete(callMock, SiteRoute.Delete(dummySiteServerDto.siteName))

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                siteServiceMock.delete(dummySiteServerDto, dummySessionDto.sessionId)
                callMock.respond(dummySiteClientDto)
            }
        }

        @Test
        fun `delete site when it doesn't exist`() = runBlocking {
            every {
                siteServiceMock.delete(
                    dummySiteServerDto,
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
                siteServiceMock.delete(dummySiteServerDto, dummySessionDto.sessionId)
            }
        }

    }

}
