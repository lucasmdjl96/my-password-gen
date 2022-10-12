package com.lmdjl.passwordgenerator.server.controller

import com.lucasmdjl.passwordgenerator.common.dto.client.SiteClientDto
import com.lucasmdjl.passwordgenerator.common.dto.server.SiteServerDto
import com.lucasmdjl.passwordgenerator.common.routes.SiteRoute
import com.lucasmdjl.passwordgenerator.server.controller.impl.SiteControllerImpl
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.mapper.SiteMapper
import com.lucasmdjl.passwordgenerator.server.model.Site
import com.lucasmdjl.passwordgenerator.server.service.SiteService
import com.lucasmdjl.passwordgenerator.server.tables.Sites
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

class SiteControllerTest : ControllerTestParent() {

    private lateinit var siteServiceMock: SiteService
    private lateinit var siteMapperMock: SiteMapper
    private lateinit var callMock: ApplicationCall

    private lateinit var dummySessionDto: SessionDto
    private lateinit var dummySite: Site
    private lateinit var dummySiteServerDto: SiteServerDto
    private lateinit var dummySiteClientDto: SiteClientDto

    @BeforeAll
    override fun initMocks() {
        siteServiceMock = mockk()
        siteMapperMock = mockk()
        callMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummySessionDto = SessionDto(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        dummySite = Site(EntityID(1, Sites))
        dummySiteServerDto = SiteServerDto("siteAbc")
        dummySiteClientDto = SiteClientDto("Site123")
    }

    @Nested
    inner class New {

        @Test
        fun `create new site when it doesn't exist`() = runBlocking {
            every { siteServiceMock.create(dummySiteServerDto, dummySessionDto.sessionId) } returns dummySite
            with(siteMapperMock) {
                every { dummySite.toSiteClientDto() } returns dummySiteClientDto
            }
            mockCall(callMock, dummySessionDto, dummySiteServerDto)

            val siteController = SiteControllerImpl(siteServiceMock, siteMapperMock)

            siteController.post(callMock, SiteRoute.New())

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                siteServiceMock.create(dummySiteServerDto, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                callMock.receive<SiteServerDto>()
                siteServiceMock.create(dummySiteServerDto, dummySessionDto.sessionId)
                with(siteMapperMock) {
                    dummySite.toSiteClientDto()
                }
                callMock.respondNullable(dummySiteClientDto as SiteClientDto?)
            }
        }

        @Test
        fun `create new site when it already exists`() = runBlocking {
            every { siteServiceMock.create(dummySiteServerDto, dummySessionDto.sessionId) } returns null
            mockCall(callMock, dummySessionDto, dummySiteServerDto)

            val siteController = SiteControllerImpl(siteServiceMock, siteMapperMock)

            siteController.post(callMock, SiteRoute.New())

            verify(exactly = 0) {
                with(siteMapperMock) {
                    any<Site>().toSiteClientDto()
                }
            }
            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                siteServiceMock.create(dummySiteServerDto, dummySessionDto.sessionId)
            }
            coVerifyOrder {
                callMock.receive<SiteServerDto>()
                siteServiceMock.create(dummySiteServerDto, dummySessionDto.sessionId)
                callMock.respondNullable(null as SiteClientDto?)
            }
        }

    }

    @Nested
    inner class Find {

        @Test
        fun `find site when it already exists`() = runBlocking {
            every { siteServiceMock.find(dummySiteServerDto, dummySessionDto.sessionId) } returns dummySite
            with(siteMapperMock) {
                every { dummySite.toSiteClientDto() } returns dummySiteClientDto
            }
            mockCall(callMock, dummySessionDto)

            val siteController = SiteControllerImpl(siteServiceMock, siteMapperMock)

            siteController.get(callMock, SiteRoute.Find(dummySiteServerDto.siteName))

            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                siteServiceMock.find(dummySiteServerDto, dummySessionDto.sessionId)
                with(siteMapperMock) {
                    dummySite.toSiteClientDto()
                }
                callMock.respondNullable(dummySiteClientDto as SiteClientDto?)
            }
        }

        @Test
        fun `find site when it doesn't exist`() = runBlocking {
            every { siteServiceMock.find(dummySiteServerDto, dummySessionDto.sessionId) } returns null
            mockCall(callMock, dummySessionDto)

            val siteController = SiteControllerImpl(siteServiceMock, siteMapperMock)

            siteController.get(callMock, SiteRoute.Find(dummySiteServerDto.siteName))

            verify(exactly = 0) {
                with(siteMapperMock) {
                    any<Site>().toSiteClientDto()
                }
            }
            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                siteServiceMock.find(dummySiteServerDto, dummySessionDto.sessionId)
                callMock.respondNullable(null as SiteClientDto?)
            }
        }

    }

    @Nested
    inner class Delete {
        @Test
        fun `delete site when it already exists`() = runBlocking {
            every { siteServiceMock.delete(dummySiteServerDto, dummySessionDto.sessionId) } returns Unit
            mockCall(callMock, dummySessionDto)

            val siteController = SiteControllerImpl(siteServiceMock, siteMapperMock)

            siteController.delete(callMock, SiteRoute.Delete(dummySiteServerDto.siteName))

            verify(exactly = 0) {
                with(siteMapperMock) {
                    any<Site>().toSiteClientDto()
                }
            }
            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                siteServiceMock.delete(dummySiteServerDto, dummySessionDto.sessionId)
                callMock.respondNullable(Unit as Unit?)
            }
        }

        @Test
        fun `delete site when it doesn't exist`() = runBlocking {
            every { siteServiceMock.delete(dummySiteServerDto, dummySessionDto.sessionId) } returns null
            mockCall(callMock, dummySessionDto)

            val siteController = SiteControllerImpl(siteServiceMock, siteMapperMock)

            siteController.delete(callMock, SiteRoute.Delete(dummySiteServerDto.siteName))

            verify(exactly = 0) {
                with(siteMapperMock) {
                    any<Site>().toSiteClientDto()
                }
            }
            coVerifyOrder {
                callMock.sessions.get<SessionDto>()
                siteServiceMock.delete(dummySiteServerDto, dummySessionDto.sessionId)
                callMock.respondNullable(null as Unit?)
            }
        }

    }

}
