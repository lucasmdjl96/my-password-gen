package com.lucasmdjl.passwordgenerator.server.mapper

import com.lucasmdjl.passwordgenerator.common.dto.client.SiteClientDto
import com.lucasmdjl.passwordgenerator.server.model.Site

interface SiteMapper {

    fun siteToSiteClientDto(site: Site): SiteClientDto

    fun Site.toSiteClientDto() = siteToSiteClientDto(this)

    fun siteIterableToSiteDtoClientIterable(siteIterable: Iterable<Site>): Iterable<SiteClientDto>

    fun Iterable<Site>.toSiteClientDtoIterable() = siteIterableToSiteDtoClientIterable(this)

}
