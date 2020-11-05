package me.amuxix

import io.getquill._

object Main {
  lazy val ctx = new PostgresJdbcContext(SnakeCase, "ctx")

  def main(args: Array[String]): Unit = {
    import ctx._
    val a = ctx.run(query[Bases])
    a.map(println)
  }
}
