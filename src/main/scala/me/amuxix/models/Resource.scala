package me.amuxix.models

import java.sql.Timestamp

import me.amuxix.models.Locale.Locale

case class Resource(
  key: String,
  locale: Locale,
  body: String,
  createdAt: Timestamp = new Timestamp(System.currentTimeMillis()),
  modifiedAt: Timestamp = new Timestamp(System.currentTimeMillis()),
)
