/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.test.plugins.routing

import com.mypasswordgen.common.dto.server.SiteServerDto
import com.mypasswordgen.common.routes.SiteRoute
import com.mypasswordgen.server.controller.impl.SessionControllerImpl
import com.mypasswordgen.server.controller.impl.SiteControllerImpl
import com.mypasswordgen.server.dto.SessionDto
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
