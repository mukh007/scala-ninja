package org.mj.json

import java.util.Date

import scala.reflect.runtime.universe

import org.slf4j.LoggerFactory

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.reflect.runtime.universe._
import scala.reflect.ClassTag
import scala.reflect._
import java.io.FileReader
import org.mj.json.modules.DateTimeModule

object JsonSerDe {
  private val LOG = LoggerFactory.getLogger(this.getClass.getSimpleName)
  private val mapper = new ObjectMapper()

  def loadConfiguration[T](configPath: String)(implicit tag: ClassTag[T]): T = {
    mapper.readValue(new FileReader(configPath), tag.runtimeClass.asInstanceOf[Class[T]])
  }

  def serialize(value: Any): String = {
    LOG.debug(" Ser= {}", mapper.writeValueAsString(value))
    mapper.writeValueAsString(value)
  }

  def deserialize[T](value: String)(implicit tag: ClassTag[T]): T = {
    deserialize[T](value.getBytes)
  }

  def deserialize[T](value: Array[Byte])(implicit tag: ClassTag[T]): T = {
    LOG.debug("Dser= {}", mapper.readValue(value, classTag[T].runtimeClass))
    mapper.readValue(value, tag.runtimeClass.asInstanceOf[Class[T]])
  }

  def getMapper = mapper

  def configure = {
    mapper.registerModule(new DateTimeModule)
    mapper.registerModule(new DefaultScalaModule)
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  }
}
