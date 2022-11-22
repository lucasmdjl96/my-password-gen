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

import com.mypasswordgen.common.routes.AboutRoute
import com.mypasswordgen.common.routes.ContributeRoute
import com.mypasswordgen.server.controller.impl.AboutControllerImpl
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
        fun `get about route`() = testApplication {
            coEvery { anyConstructed<AboutControllerImpl>().get(any(), any<AboutRoute>()) } just Runs
            createAndConfigureClient().get(AboutRoute())
            coVerify { anyConstructed<AboutControllerImpl>().get(any(), any<AboutRoute>()) }
        }

        @Test
        fun `get contribute route`() = testApplication {
            coEvery { anyConstructed<AboutControllerImpl>().get(any(), any<ContributeRoute>()) } just Runs
            createAndConfigureClient().get(ContributeRoute())
            coVerify { anyConstructed<AboutControllerImpl>().get(any(), any<ContributeRoute>()) }
        }

    }

}
