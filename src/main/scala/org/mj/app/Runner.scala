package org.mj.app

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext
import scala.util.Random

import org.mj.future.FuturesHandler
import org.mj.http.HttpHandler
import org.mj.retry.RetryHandler

import com.typesafe.scalalogging.LazyLogging
import org.mj.file.FunctionsHandler

object Runner extends App with LazyLogging {
  org.apache.log4j.Logger.getRootLogger.setLevel(org.apache.log4j.Level.INFO)
  private implicit val ec = ExecutionContext.fromExecutorService(Executors.newWorkStealingPool(10))
  val totalRequestCount = 10000
  val failurerate = 5 // 20% failure

  val hh = new HttpHandler

  FunctionsHandler.repeat(run(RetryHandler.retry(sampleFunctionCall(failurerate), maxRetryCount = 5), 100), 100, false)

  private def run[T](func: => T) = {
    val (successfulRequests, failedRequests) = FuturesHandler.runFutures(func, totalRequestCount)
    successfulRequests.map(r => logger.debug(s"${r}"))
    failedRequests.foreach(r => logger.debug(s"${r}"))
    logger.info(s"Results: ${successfulRequests.size} succeeded & ${failedRequests.size} failed of $totalRequestCount totalRequestCount")
  }

  private def sampleFunctionCall(failureRate: Int = 10) = {
    val randRes = (1 / Random.nextInt(failureRate)) // Induce artificial failures
    //val hr = hh.getHttpPostFormResponse("http://api.icndb.com/jokes/random")
    //val result = new String(hr.body)
    val result = s"$randRes :: ${Random.nextString(10)}"
    logger.trace(s"${result}")
    result
  }
}