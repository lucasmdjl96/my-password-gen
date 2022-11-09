package com.mypasswordgen.server.test.plugins.routing

import com.mypasswordgen.common.routes.SessionRoute
import com.mypasswordgen.server.controller.impl.SessionControllerImpl
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
