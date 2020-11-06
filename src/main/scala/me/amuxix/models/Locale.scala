package me.amuxix.models

import io.getquill.MappedEncoding

object Locale extends Enumeration {
  type Locale = Value
  val Default = Value("default")
  val German = Value("de")
  val English = Value("en")

  implicit val encoder = MappedEncoding[Locale, String](_.toString)
  implicit val decoder = MappedEncoding[String, Locale](Locale.withName)
}
