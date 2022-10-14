package com.lucasmdjl.passwordgenerator.server.test.plugins.routing

import com.lucasmdjl.passwordgenerator.common.dto.server.SiteServerDto
import com.lucasmdjl.passwordgenerator.common.routes.SiteRoute
import com.lucasmdjl.passwordgenerator.server.controller.impl.SessionControllerImpl
import com.lucasmdjl.passwordgenerator.server.controller.impl.SiteControllerImpl
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

class SiteRoutesTest : RoutesTestParent() {

    private lateinit var dummySessionDto: SessionDto
    private lateinit var dummySiteServerDto: SiteServerDto

    @BeforeAll
    override fun initMocks() {
        mockkConstructor(SiteControllerImpl::class)
        mockkConstructor(SessionControllerImpl::class)
    }

    @BeforeEach
    override fun initDummies() {
        dummySessionDto = SessionDto(UUID.fromString("7639a835-b7a4-4d57-ad28-d23e6bbd1d97"))
        dummySiteServerDto = SiteServerDto("awesomeSite")
    }

    @Nested
    inner class Get {

        @Test
        fun `get old site route`() = testApplication {
            every { anyConstructed<SessionControllerImpl>().validate(any(), any()) } returns dummySessionDto
            coEvery { anyConstructed<SiteControllerImpl>().get(any(), any<SiteRoute.Find>()) } just Runs
            createAndConfigureClient().get(SiteRoute.Find("awesomeSite"))
            coVerifyOrder {
                anyConstructed<SessionControllerImpl>().validate(any(), any())
                anyConstructed<SiteControllerImpl>().get(any(), any<SiteRoute.Find>())
            }
        }

    }

    @Nested
    inner class Post {

        @Test
        fun `post new site route`() = testApplication {
            every { anyConstructed<SessionControllerImpl>().validate(any(), any()) } returns dummySessionDto
            coEvery { anyConstructed<SiteControllerImpl>().post(any(), any<SiteRoute.New>()) } just Runs
            createAndConfigureClient().post(SiteRoute.New()) {
                contentType(ContentType.Application.Json)
                setBody(dummySiteServerDto)
            }
            coVerifyOrder {
                anyConstructed<SessionControllerImpl>().validate(any(), any())
                anyConstructed<SiteControllerImpl>().post(any(), any<SiteRoute.New>())
            }
        }

    }

    @Nested
    inner class Delete {

        @Test
        fun `delete old site route`() = testApplication {
            every { anyConstructed<SessionControllerImpl>().validate(any(), any()) } returns dummySessionDto
            coEvery { anyConstructed<SiteControllerImpl>().delete(any(), any<SiteRoute.Delete>()) } just Runs
            createAndConfigureClient().delete(SiteRoute.Delete("awfulSite"))
            coVerifyOrder {
                anyConstructed<SessionControllerImpl>().validate(any(), any())
                anyConstructed<SiteControllerImpl>().delete(any(), any<SiteRoute.Delete>())
            }
        }

    }

}
