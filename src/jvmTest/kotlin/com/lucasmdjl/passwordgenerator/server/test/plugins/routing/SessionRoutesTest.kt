package com.lucasmdjl.passwordgenerator.server.test.plugins.routing

import com.lucasmdjl.passwordgenerator.common.routes.SessionRoute
import com.lucasmdjl.passwordgenerator.server.controller.impl.SessionControllerImpl
import io.ktor.client.plugins.resources.*
import io.ktor.server.testing.*
import io.mockk.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SessionRoutesTest : RoutesTestParent() {

    @BeforeAll
    override fun initMocks() {
        mockkConstructor(SessionControllerImpl::class)
    }

    override fun initDummies() {
    }

    @Nested
    inner class Put {

        @Test
        fun `put session use route`() = testApplication {
            coEvery { anyConstructed<SessionControllerImpl>().put(any(), any<SessionRoute>()) } just Runs
            createAndConfigureClient().put(SessionRoute())
            coVerify {
                anyConstructed<SessionControllerImpl>().put(any(), any<SessionRoute>())
            }
        }

    }

}
