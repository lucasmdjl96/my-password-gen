package com.lucasmdjl.application.repository

import com.lucasmdjl.application.model.Email
import com.lucasmdjl.application.model.Site

interface SiteRepository {

    fun createAndGetId(siteName: String, email: Email): Int?

    fun getById(id: Int): Site?

    fun getByNameAndEmail(siteName: String, email: Email): Site?

    fun delete(siteName: String, email: Email): Unit?

}