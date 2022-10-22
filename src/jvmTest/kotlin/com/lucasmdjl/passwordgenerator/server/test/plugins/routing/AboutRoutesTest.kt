package com.lucasmdjl.passwordgenerator.server.test.plugins.routing

import com.lucasmdjl.passwordgenerator.common.routes.AboutRoute
import com.lucasmdjl.passwordgenerator.server.controller.impl.AboutControllerImpl
import io.ktor.client.plugins.resources.*
import io.ktor.server.testing.*
import io.mockk.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AboutRoutesTest : RoutesTestParent() {

    @BeforeAll
    override fun initMocks() {
        mockkConstructor(AboutControllerImpl::class)
    }

    override fun initDummies() {
    }

    @Nested
    inner class Get {

        @Test
        fun `get main route`() = testApplication {
            coEvery { anyConstructed<AboutControllerImpl>().get(any(), any<AboutRoute>()) } just Runs
            createAndConfigureClient().get(AboutRoute())
            coVerify { anyConstructed<AboutControllerImpl>().get(any(), any<AboutRoute>()) }
        }

    }

}
