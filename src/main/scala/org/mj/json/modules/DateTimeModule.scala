package org.mj.json.modules

import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import java.util.Date

class DateTimeModule extends SimpleModule {
  addSerializer(classOf[Date], new CustomDateSerializer)
}

class CustomDateSerializer extends JsonSerializer[Date] {
  override def serialize(date: Date, jsonGenerator: JsonGenerator, serializers: SerializerProvider) = {
    val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    df.setTimeZone(TimeZone.getTimeZone("UTC"))
    jsonGenerator.writeString(df.format(date))
  }
}