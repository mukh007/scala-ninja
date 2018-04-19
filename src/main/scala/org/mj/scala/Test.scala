package org.mj.scala

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.mapAsScalaMap

import scala.collection.JavaConverters._
import scala.collection.JavaConversions.asScalaBuffer

import org.slf4j.LoggerFactory

import java.util.function.Supplier
import org.joda.time.DateTime
import scala.util.Random
import java.util.HashMap
import java.util.Date
import scala.concurrent.Future
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import java.util.Locale


object Test {
  val LOG = LoggerFactory.getLogger(this.getClass.getSimpleName)

  def main(args: Array[String]) {
    LOG.info("MJ-New-S @" + DateTime.now())

    var futureList = new java.util.ArrayList[Future[String]]()
    Locale.getISOLanguages.foreach { lang => {
    	val future = Future {
    		val sleepTime = Random.nextInt(500)
    				Thread.sleep(sleepTime)
    				lang
    	}
    	futureList.add(future)
    }}
    println("futuresSize=" + futureList.size())
    futureList.foreach { future => {
    	future.onComplete { 
    	case Success(value) => println(s"Got the callback, meaning = $value")
    	case Failure(e) => e.printStackTrace
    	}
    }}
    LOG.info("MJ-New-D @" + DateTime.now())
    Thread.sleep(5000)
    LOG.info("MJ-New-Finished @" + DateTime.now())
  }
}