package org.mj.future
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import com.typesafe.scalalogging.LazyLogging

object FuturesHandler extends LazyLogging {
  private val totalAsyncCall = 1000
  private implicit val ec = ExecutionContext.fromExecutorService(Executors.newWorkStealingPool(totalAsyncCall / 2))

  /**
   * Run multiple futures in parallel and aggregate results from them
   * Returns tuple of successful and failed futures
   * Return: (successfulResults: Seq[T], failedResults: Seq[T])
   */
  def runFutures[T](func: => T, times: Int = 1, waitTimeMS: Int = 10, maxWaitTimeMS: Int = 10000): (Seq[Try[T]], Seq[Try[T]]) = {
    val latch = new CountDownLatch(times)
    val result = for (i <- 1 to times) yield {
      val f = getFuture(func)
      f.onComplete(_ => { latch.countDown })
      f
    }
    val isSuccessful = latch.await(maxWaitTimeMS, TimeUnit.SECONDS)
    val (success, failure) = result.map(_.value.get).partition(_.isSuccess)
    logger.debug(s"latch finished ($isSuccessful) at ${latch.getCount}")
    (success, failure)
  }

  /**
   *  Private Functions
   */

  /**
   * This is async
   */
  private def getFuture[IN, OUT](name: IN, func: (IN) => OUT) = {
    val f = Future { func(name) }
    f.onComplete({
      case Success(result) => result
      case Failure(ex) => ex
    })
    f
  }
  private def getFuture[T](func: => T) = {
    val f = Future { func }
    f.onComplete({
      case Success(result) => result
      case Failure(ex) => ex
    })
    f
  }
}