package me.amuxix

import java.time.ZonedDateTime

import me.amuxix.model.{Resource, ResourceLocale}
import me.amuxix.repository.doobie.ResourceRepository

import scala.concurrent.ExecutionContext

object DoobieMain extends App {
  val repo = ResourceRepository()(ExecutionContext.global)

  print("Getting with an empty DB: ")
  println(repo.get("greeting", ResourceLocale.English).unsafeRunSync())

  print("     Inserting some data: ")
  println(repo.insert(
    Resource("greeting", ResourceLocale.English, "Welcome!", ZonedDateTime.now()),
    Resource("greeting", ResourceLocale.German, "Willkommen!", ZonedDateTime.now()),
    Resource("greeting", ResourceLocale.Default, "👋", ZonedDateTime.now())
  ).unsafeRunSync())

  print("        English greeting: ")
  println(repo.get("greeting", ResourceLocale.English).unsafeRunSync())

  print("         German greeting: ")
  println(repo.get("greeting", ResourceLocale.German).unsafeRunSync())

  print(" Deleting german variant: ")
  println(repo.delete("greeting", ResourceLocale.German).unsafeRunSync())

  print("         German greeting: ")
  println(repo.get("greeting", ResourceLocale.German).unsafeRunSync())

  print("  Deleting all greetings: ")
  println(repo.delete("greeting").unsafeRunSync())

  print("        English greeting: ")
  println(repo.get("greeting", ResourceLocale.English).unsafeRunSync())
}
