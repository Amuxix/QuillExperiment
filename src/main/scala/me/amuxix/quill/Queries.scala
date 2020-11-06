package me.amuxix.quill

import java.sql.Timestamp

import io.getquill.{EntityQuery, MappedEncoding}
import me.amuxix.models.Locale.Locale
import me.amuxix.models.Resource
import me.amuxix.quill.Main.context._

object Queries {
  implicit val timestampEncoder = MappedEncoding[Timestamp, Long](_.getTime)
  implicit val timestampDecoder = MappedEncoding[Long, Timestamp](new Timestamp(_))
  private val q: EntityQuery[Resource] = query[Resource]

  def insert(resource: Resource) = quote {
    implicit val resourceInsertMeta = insertMeta[Resource]()
    q.insert(lift(resource))
  }

  val selectAll = q

  def selectByKeyAndLocale(key: String, locale: Locale) =
    q
      .filter(_.locale == locale)
      .filter(_.key == key)

  def update(resource: Resource) = {
    implicit val resourceInsertMeta = updateMeta[Resource]()
    q.update(resource)
  }

  private def byID(id: (String, Locale)) = (selectByKeyAndLocale _).tupled(id)

  def update(id: (String, Locale), body: String) = byID(id).update(_.body -> body)

  def delete(id: (String, Locale)) = byID(id).delete
}
