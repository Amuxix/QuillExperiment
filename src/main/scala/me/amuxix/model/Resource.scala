package me.amuxix.model

import java.time.ZonedDateTime

// modifiedAt left out intentionally, as it should be updated implicitly
final case class Resource(key:String, locale:ResourceLocale, body:String, createdAt:ZonedDateTime)
