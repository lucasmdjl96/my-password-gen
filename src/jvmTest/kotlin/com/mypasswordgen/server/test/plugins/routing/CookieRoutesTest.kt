package com.mypasswordgen.server.test.plugins.routing

import com.mypasswordgen.common.routes.CookieRoute
import com.mypasswordgen.server.controller.impl.CookieControllerImpl
import io.ktor.client.plugins.resources.*
import io.ktor.server.testing.*
import io.mockk.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CookieRoutesTest : RoutesTestParent() {

    @BeforeAll
    override fun initMocks() {
        mockkConstructor(CookieControllerImpl::class)
    }

    override fun initDummies() {
    }

    @Nested
    inner class OptOut {

        @Test
        fun `get opt out route`() = testApplication {
            coEvery { anyConstructed<CookieControllerImpl>().get(any(), any<CookieRoute.OptOut>()) } just Runs
            createAndConfigureClient().get(CookieRoute.OptOut())
            coVerify { anyConstructed<CookieControllerImpl>().get(any(), any<CookieRoute.OptOut>()) }
        }

    }

    @Nested
    inner class Policy {

        @Test
        fun `get policy route`() = testApplication {
            coEvery { anyConstructed<CookieControllerImpl>().get(any(), any<CookieRoute.Policy>()) } just Runs
            createAndConfigureClient().get(CookieRoute.Policy())
            coVerify { anyConstructed<CookieControllerImpl>().get(any(), any<CookieRoute.Policy>()) }
        }

    }

}
