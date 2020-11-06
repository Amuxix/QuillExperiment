package me.amuxix.model

sealed trait ResourceLocale {
  def identifier:String

  override def toString:String = identifier
}
object ResourceLocale {
  lazy val all:Set[ResourceLocale] = Set(Default, English, German)

  case object Default extends ResourceLocale {override def identifier:String = "default"}
  case object English extends ResourceLocale {override def identifier:String = "en"}
  case object German extends ResourceLocale {override def identifier:String = "de"}

  def apply(identifier:String):Option[ResourceLocale] = all.find(_.identifier == identifier)
}
