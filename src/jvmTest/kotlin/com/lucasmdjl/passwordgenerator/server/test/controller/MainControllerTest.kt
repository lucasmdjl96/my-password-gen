package com.lucasmdjl.passwordgenerator.server.test.controller

import com.lucasmdjl.passwordgenerator.common.routes.MainRoute
import com.lucasmdjl.passwordgenerator.server.controller.impl.MainControllerImpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MainControllerTest : ControllerTestParent() {

    private lateinit var callMock: ApplicationCall

    @BeforeAll
    override fun initMocks() {
        callMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
    }

    @Nested
    inner class Main {
        @Test
        fun `get main page`() = runBlocking {
            mockkStatic("io.ktor.server.response.ApplicationResponseFunctionsKt")
            coEvery { callMock.respondText(any(), any()) } just Runs

            val mainController = MainControllerImpl()

            mainController.get(callMock, MainRoute())
            coVerify { callMock.respondText(any(), ContentType.Text.Html) }
        }
    }

}
