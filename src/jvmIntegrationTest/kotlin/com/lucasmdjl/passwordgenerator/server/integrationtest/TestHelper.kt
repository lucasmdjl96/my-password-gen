package com.lucasmdjl.passwordgenerator.server.integrationtest

import kotlin.test.assertFalse
import kotlin.test.assertTrue


fun assertEmpty(iterable: Iterable<*>) {
    assertTrue(iterable.toList().isEmpty(), "Expected value to be empty.")
}

fun assertNotEmpty(iterable: Iterable<*>) {
    assertFalse(iterable.toList().isEmpty(), "Expected value to not be empty.")
}
