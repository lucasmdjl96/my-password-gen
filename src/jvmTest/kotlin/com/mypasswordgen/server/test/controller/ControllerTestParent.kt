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

import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.test.TestParent
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.sessions.*
import io.mockk.*

abstract class ControllerTestParent : TestParent() {

    inline fun <reified T> mockCall(callMock: ApplicationCall, dummySessionDto: SessionDto, dummyReceive: T) {
        mockkStatic("io.ktor.server.sessions.SessionDataKt")
        every { callMock.sessions.get(any()) } returns dummySessionDto
        every { callMock.sessions.findName(any()) } returns ""
        mockkStatic("io.ktor.server.request.ApplicationReceiveFunctionsKt")
        coEvery { callMock.receiveNullable<T>(any()) } returns dummyReceive
        every { callMock.response.call.attributes.put(any(), any()) } just Runs
        coEvery { callMock.response.pipeline.execute(any(), any()) } returns Unit
    }

    fun mockCall(callMock: ApplicationCall, dummySessionDto: SessionDto?) {
        mockkStatic("io.ktor.server.sessions.SessionDataKt")
        every { callMock.sessions.get(any()) } returns dummySessionDto
        every { callMock.sessions.findName(any()) } returns ""
        every { callMock.response.call.attributes.put(any(), any()) } just Runs
        coEvery { callMock.response.pipeline.execute(any(), any()) } returns Unit
    }

}
