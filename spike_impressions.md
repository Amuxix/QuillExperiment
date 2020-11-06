# Spike impressions

## Doobie

* Disappointed that it _really_ is as advertised: SQL must be specified explicitly.
  * Table and column names have to be mentioned explicitly, and escaped if needed.
  * No support for automatically generating simple statements based on case classes.
  * Not only the SQL statement, but also the returned types must be provided manually.
* Postgres enums require implementing conversion functions.
* If any property type is not a basic DB type (String, Int, etc.), it has to be converted manually.
  * This prevents us from using `(MyCaseClass.apply _).tupled` for unmarshalling.
  * Therefore, custom DB marshalling methods need to be written.
* No OOTB support for `ZonedDateTime`. Need to use `java.sql.Timestamp`, and then convert from there.
  * This requires us to use the system default time zone, hoping that the Postgres driver does the same.
  * When using "Optimize imports" with IntelliJ IDEA, it will remove the import required for converting
    `java.sql.Timestamp`.
* Typechecking queries is possible, but has to be run explicitly from the code.
  * Thus, there is no simple SBT command that typechecks all statements in the codebase.
  * All queries need to be checked, probably in unit tests, which makes it possible to forget one, or forget to update
     the check.
  * This means that all queries need to referred to in the code at least twice.
    That's an additional overhead _per query_. 
* Seems to be too generic at times. ("Could not find instance of Foldable for Seq")
* The documentation is strange at times. (Selecting and updating found on the TOC, but not deleting?)
  * After googling, turns out that deleting is just a special case of updating in Doobie.

## Doobie with jOOQ

* Using jOOQ for query generation only works quite easily.
* No use of prepared statements due to static parameter insertion.
