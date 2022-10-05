package com.lucasmdjl.passwordgenerator.server.mapper

import com.lucasmdjl.passwordgenerator.common.dto.client.SiteClientDto
import com.lucasmdjl.passwordgenerator.server.model.Site

interface SiteMapper {

    fun siteToSiteClientDto(site: Site): SiteClientDto

    fun siteIterableToSiteDtoClientIterable(siteList: Iterable<Site>?): Iterable<SiteClientDto>?

}
