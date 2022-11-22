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
            coEvery { anyConstructed<SessionControllerImpl>().put(any(), any<SessionRoute.Update>()) } just Runs
            createAndConfigureClient().put(SessionRoute.Update())
            coVerify {
                anyConstructed<SessionControllerImpl>().put(any(), any<SessionRoute.Update>())
            }
        }

    }

}
