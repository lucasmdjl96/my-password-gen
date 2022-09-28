package com.lucasmdjl.application.repository

import com.lucasmdjl.application.model.Email
import com.lucasmdjl.application.model.Site

interface SiteRepository {

    fun create(siteName: String, email: Email): Int?

    fun getById(id: Int): Site?

    fun getAllFromEmail(email: Email): Iterable<Site>

    fun getByNameAndEmail(siteName: String, email: Email): Site?

    fun delete(siteName: String, email: Email): Unit?

}