package com.lucasmdjl.passwordgenerator.server.test.plugins.routing

import com.lucasmdjl.passwordgenerator.common.routes.MainRoute
import com.lucasmdjl.passwordgenerator.server.controller.impl.MainControllerImpl
import io.ktor.client.plugins.resources.*
import io.ktor.server.testing.*
import io.mockk.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MainRoutesTest : RoutesTestParent() {

    @BeforeAll
    override fun initMocks() {
        mockkConstructor(MainControllerImpl::class)
    }

    override fun initDummies() {
    }

    @Nested
    inner class Get {

        @Test
        fun `get main route`() = testApplication {
            coEvery { anyConstructed<MainControllerImpl>().get(any(), any<MainRoute>()) } just Runs
            createAndConfigureClient().get(MainRoute())
            coVerify { anyConstructed<MainControllerImpl>().get(any(), any<MainRoute>()) }
        }

    }

}
