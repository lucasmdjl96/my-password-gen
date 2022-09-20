package com.lucasmdjl.application.repository

import com.lucasmdjl.application.model.Email
import com.lucasmdjl.application.model.Site

interface SiteRepository {

    fun create(name: String, email: Email): Site

    fun getByName(name: String): Site?

    fun getAllForEmail(email: Email): Iterable<Site>

    fun getByNameAndEmail(name: String, email: Email): Site?

}