package me.amuxix.repository.doobie

import java.sql.Timestamp
import java.time.ZoneId
import java.util.TimeZone

import cats.effect.{ContextShift, IO}
import doobie.Meta
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update
import me.amuxix.model.{Resource, ResourceLocale}
import doobie.postgres._
import doobie.postgres.implicits._

import scala.concurrent.ExecutionContext

trait ResourceRepository {
  def delete(key:String):IO[Int]
  def delete(key:String, locale:ResourceLocale):IO[Int]
  def get(key:String, locale:ResourceLocale):IO[Option[Resource]]
  def insert(resources:Resource*):IO[Int]
}
object ResourceRepository {
  // IntelliJ IDEA removes this one:
  import doobie.implicits.javasql._

  def apply()(implicit ec:ExecutionContext):ResourceRepository = new Impl(IO.contextShift(ec))

  private implicit val resourceLocaleMeta:Meta[ResourceLocale] =
    pgEnumStringOpt("resource_locale", ResourceLocale.apply, _.identifier)

  // Required because using `ResourceLocale.Default` directly in an SQL interpolation does not work. (Why?)
  private val defaultLocale:ResourceLocale = ResourceLocale.Default

  // TODO try using Instant instead of Timestamp.
  private final class Impl(contextShift:ContextShift[IO]) extends ResourceRepository {
    private implicit val cs:ContextShift[IO] = contextShift
    private val xa:Transactor.Aux[IO,Unit] =
      Transactor.fromDriverManager[IO]("org.postgresql.Driver", "jdbc:postgresql:test", "test", "test")

    override def delete(key:String):IO[Int] =
      sql"""delete from resources where "key" = $key""".update.run.transact(xa)

    override def delete(key:String, locale:ResourceLocale):IO[Int] =
    sql"""delete from resources where "key" = $key and locale = $locale""".update.run.transact(xa)

    override def get(key:String, locale:ResourceLocale):IO[Option[Resource]] =
      sql"""
        select "key", locale, body, created_at from resources
        where "key" = $key and (
          locale = $locale or locale = $defaultLocale
        )
      """
        .query[(String, ResourceLocale, String, Timestamp)] // TODO try query[Resource]
        .to[List]
        .transact(xa)
        .map(results => results.find(_._2 == locale).orElse(results.headOption))
        .map(_.map(untupled))

    override def insert(resources:Resource*):IO[Int] = {
      val sql = """insert into resources ("key", locale, body, created_at, modified_at) values (?, ?, ?, ?, ?)"""
      Update[(String, ResourceLocale, String, Timestamp, Timestamp)](sql)
        .updateMany(resources.map(tupledWithModifiedAt).toList)
        .transact(xa)
    }
  }

  private val defaultTimezone:ZoneId = TimeZone.getDefault.toZoneId

  // Required because of the use of Timestamp instead of ZonedDateTime, and String instead of ResourceLocale.
  private def untupled(t:(String, ResourceLocale, String, Timestamp)):Resource =
    Resource(t._1, t._2, t._3, t._4.toLocalDateTime.atZone(defaultTimezone))

  private def tupledWithModifiedAt(r:Resource):(String, ResourceLocale, String, Timestamp, Timestamp) = {
    val timestamp = Timestamp.from(r.createdAt.toInstant)
    (r.key, r.locale, r.body, timestamp, timestamp)
  }
}
