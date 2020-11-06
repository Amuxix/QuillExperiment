package me.amuxix.quill

import io.getquill._
import me.amuxix.models.{Locale, Resource}

object Main {
  lazy val context = new PostgresJdbcContext(SnakeCase, "ctx")

  def main(args: Array[String]): Unit = {
    val resource = Resource("test", Locale.English, "test body")

    println(s"insert = ${context.run(Queries.insert(resource))}")
  }
}
