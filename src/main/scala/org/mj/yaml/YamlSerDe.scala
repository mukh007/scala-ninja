package org.mj.yaml

import java.io.FileReader
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import scala.reflect.runtime.universe._
import scala.reflect.ClassTag
import scala.reflect._
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.mj.json.modules.DateTimeModule
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.DeserializationFeature

object YamlSerDe {
  val LOG = LoggerFactory.getLogger(this.getClass.getSimpleName)
  val mapper = new ObjectMapper(new YAMLFactory)

  def getYmlMapper = mapper

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

  def configure = {
    mapper.registerModule(new DateTimeModule)
    mapper.registerModule(new DefaultScalaModule)
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false)
    this
  }
}