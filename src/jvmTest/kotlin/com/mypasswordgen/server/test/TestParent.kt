package com.mypasswordgen.server.test

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.mockk.*
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class TestParent {

    @BeforeAll
    abstract fun initMocks()

    @BeforeEach
    abstract fun initDummies()

    @AfterEach
    fun clearMocks() {
        clearAllMocks()
    }

    @AfterAll
    fun unmock() {
        unmockkAll()
    }

}
