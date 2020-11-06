package me.amuxix.repository.doobie

import java.sql.Timestamp
import java.time.ZoneId
import java.util.TimeZone

import cats.effect.{ContextShift, IO}
import doobie.{Meta, _}
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import doobie.util.update.Update
import me.amuxix.model.{Resource, ResourceLocale}
import org.jooq.{DSLContext, Query, SQLDialect}
import org.jooq.conf.{Settings, StatementType}
import org.jooq.impl.DSL

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
  import me.amuxix.db.schema.test.tables.Resources.RESOURCES
  import me.amuxix.db.schema.test.enums.{ResourceLocale => DBResourceLocale}

  def apply()(implicit ec:ExecutionContext):ResourceRepository = new Impl(IO.contextShift(ec))

  // This could be made a global constant, because it is immutable.
  private val query:DSLContext = {
    val settings = new Settings
    settings.setStatementType(StatementType.STATIC_STATEMENT)
    DSL.using(SQLDialect.POSTGRES, settings)
  }

  private implicit val resourceLocaleMeta:Meta[ResourceLocale] =
    pgEnumStringOpt("resource_locale", ResourceLocale.apply, _.identifier)

  // Required because using `ResourceLocale.Default` directly in an SQL interpolation does not work. (Why?)
  private val defaultLocale:ResourceLocale = ResourceLocale.Default

  // TODO try using Instant instead of Timestamp.
  private final class Impl(contextShift:ContextShift[IO]) extends ResourceRepository {
    private implicit val cs:ContextShift[IO] = contextShift
    private val xa:Transactor.Aux[IO,Unit] =
      Transactor.fromDriverManager[IO]("org.postgresql.Driver", "jdbc:postgresql:test", "test", "test")

    private def update(query:Query):IO[Int] = Fragment.const(query.getSQL()).update.run.transact(xa)

    override def delete(key:String):IO[Int] = update(
      query.deleteFrom(RESOURCES).where(RESOURCES.KEY.eq(key))
    )

    override def delete(key:String, locale:ResourceLocale):IO[Int] = update(
      query.deleteFrom(RESOURCES).where(RESOURCES.KEY.eq(key).and(RESOURCES.LOCALE.eq(toDB(locale))))
    )

    override def get(key:String, locale:ResourceLocale):IO[Option[Resource]] = {
      query.select(RESOURCES.KEY,RESOURCES.LOCALE, RESOURCES.BODY, RESOURCES.CREATED_AT)
        .where(RESOURCES.KEY.eq(key).and(RESOURCES.LOCALE.in(toDB(locale), toDB(ResourceLocale.Default))))
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
    }

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

  private def toDB(locale:ResourceLocale):DBResourceLocale = locale match {
    case ResourceLocale.Default => DBResourceLocale.default_
    case ResourceLocale.English => DBResourceLocale.en
    case ResourceLocale.German => DBResourceLocale.de
  }
}
