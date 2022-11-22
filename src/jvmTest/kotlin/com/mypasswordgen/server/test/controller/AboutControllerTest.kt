/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.test.controller

import com.mypasswordgen.common.routes.AboutRoute
import com.mypasswordgen.common.routes.ContributeRoute
import com.mypasswordgen.server.controller.impl.AboutControllerImpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AboutControllerTest : ControllerTestParent() {

    private lateinit var callMock: ApplicationCall

    @BeforeAll
    override fun initMocks() {
        callMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
    }

    @Nested
    inner class About {
        @Test
        fun `get about page`() = runBlocking {
            mockkStatic("io.ktor.server.response.ApplicationResponseFunctionsKt")
            coEvery { callMock.respondText(any(), any()) } just Runs

            val aboutController = AboutControllerImpl()

            aboutController.get(callMock, AboutRoute())
            coVerify { callMock.respondText(any(), ContentType.Text.Html) }
        }
    }

    @Nested
    inner class Contribute {
        @Test
        fun `get contribute page`() = runBlocking {
            mockkStatic("io.ktor.server.response.ApplicationResponseFunctionsKt")
            coEvery { callMock.respondText(any(), any()) } just Runs

            val aboutController = AboutControllerImpl()

            aboutController.get(callMock, ContributeRoute())
            coVerify { callMock.respondText(any(), ContentType.Text.Plain) }
        }
    }

}
