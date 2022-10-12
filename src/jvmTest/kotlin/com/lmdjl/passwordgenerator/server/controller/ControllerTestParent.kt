package com.lmdjl.passwordgenerator.server.controller

import com.lmdjl.passwordgenerator.server.TestParent
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
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
