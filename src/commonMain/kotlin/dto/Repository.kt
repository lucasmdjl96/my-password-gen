package dto

val emailDtos = mutableListOf<EmailDto>()

val siteDtos = mutableListOf<SiteDto>()

val siteDto1 = SiteDto("GitHub")
val siteDto2 = SiteDto("GitLab")
val siteDto3 = SiteDto("JetBrains")
val siteDto4 = SiteDto("AWS")

val emailDto1 = EmailDto("lmdjong1@gmail.com", mutableListOf(siteDto1, siteDto2))
val emailDto2 = EmailDto("lucasmdjl96@gmail.com", mutableListOf(siteDto3, siteDto4))

val lucas = UserDto("Lucas", mutableListOf(emailDto1, emailDto2))

val users = mutableListOf(lucas)