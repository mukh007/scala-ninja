package org.mj.retry

import java.util.concurrent.TimeUnit

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import com.typesafe.scalalogging.LazyLogging

object RetryHandler extends LazyLogging {
  private val BASE_WAIT_TIME_IN_MIN = 1 // 10 sec
  private val MAX_WAIT_TIME_IN_MIN = 5 // 30 sec

  private def exponentialBackoff(retryItr: Int, baseWaitTime: Duration): Duration = scala.math.pow(2, retryItr).round * baseWaitTime

  def tryOnce[T](func: => T, waitTimeInMin: Double = BASE_WAIT_TIME_IN_MIN)(implicit ec: ExecutionContext): Option[T] = {
    retry(func, None, Duration.apply(waitTimeInMin, TimeUnit.MINUTES), 1, Duration.apply(waitTimeInMin, TimeUnit.MINUTES), Duration.apply(waitTimeInMin, TimeUnit.MINUTES), Duration.apply(waitTimeInMin, TimeUnit.MINUTES))
  }

  def retry[T](func: => T, waitTimeInMin: Double = BASE_WAIT_TIME_IN_MIN, maxRetryCount: Int = 5, maxWaitTimeInMin: Double = MAX_WAIT_TIME_IN_MIN)(implicit ec: ExecutionContext): Option[T] = {
    logger.trace(s"Retry Call Information: Wait Time Minutes: ${waitTimeInMin}, Max Retry Count: ${maxRetryCount}, Max Wait Time Minutes: ${maxWaitTimeInMin}, Wait Time Minutes: ${waitTimeInMin}")
    retry(func, None, Duration.apply(waitTimeInMin, TimeUnit.MINUTES), maxRetryCount, Duration.apply(waitTimeInMin, TimeUnit.MINUTES), Duration.apply(waitTimeInMin, TimeUnit.MINUTES), Duration.apply(waitTimeInMin, TimeUnit.MINUTES))
  }

  private def retry[T](func: => T, retryCount: Option[Int], waitTime: Duration, maxRetryCount: Int, maxWaitTime: Duration, totalTimeWaited: Duration, baseWaitTime: Duration)(implicit ec: ExecutionContext): Option[T] = {

    val resultFuture = Future {
      logger.trace(s"Attempt: ${retryCount}")
      Some(func)
    }
    resultFuture.onComplete({
      case Success(result) => {
        result
      }
      case Failure(ex) => {
        ex
      }
    })

    val futureTry = Try {
      logger.trace(s"Wait: ${waitTime}")
      Await.result(resultFuture, waitTime)
    }
    futureTry match {
      case Success(result) => {
        logger.trace(s"Future Success Response: ${result.get}")
        result
      }
      case Failure(ex: ArithmeticException) => {
        softRetry(func, ex, retryCount, waitTime, maxRetryCount, maxWaitTime, totalTimeWaited, baseWaitTime)
      }
      case Failure(ex) => {
        backoffRetry(func, ex, retryCount, waitTime, maxRetryCount, maxWaitTime, totalTimeWaited, baseWaitTime)
      }
    }

  }

  private def softRetry[T](func: => T, ex: Throwable, retryCount: Option[Int], waitTime: Duration, maxRetryCount: Int, maxWaitTime: Duration, totalTimeWaited: Duration, baseWaitTime: Duration)(implicit ec: ExecutionContext) = {
    logger.debug(s"Failed: ${ex.getLocalizedMessage}, Class: ${ex.getClass}, Retry Count: ${retryCount}")
    val nextRetryCount = retryCount.getOrElse(0) + 1
    val nextWaitTime = baseWaitTime
    val nextTotalTimeWaited = totalTimeWaited

    if (nextRetryCount < maxRetryCount) {
      retry(func, Option(nextRetryCount), nextWaitTime, maxRetryCount, maxWaitTime, nextTotalTimeWaited, baseWaitTime)
    } else {
      logger.trace(s"Failing attempt: ${retryCount}")
      throw ex
    }
  }

  private def backoffRetry[T](func: => T, ex: Throwable, retryCount: Option[Int], waitTime: Duration, maxRetryCount: Int, maxWaitTime: Duration, totalTimeWaited: Duration, baseWaitTime: Duration)(implicit ec: ExecutionContext) = {
    logger.error(s"Failed: ${ex.getLocalizedMessage}, Class: ${ex.getClass}, Retry Count: ${retryCount}")
    val nextRetryCount = retryCount.getOrElse(0) + 1
    logger.trace(s"Retry Count: ${nextRetryCount}, exponentialBackOffTime: ${exponentialBackoff(nextRetryCount, baseWaitTime)}, totalTimeWaited: ${totalTimeWaited}, maxWaitTime: ${maxWaitTime}, baseWaitTime: ${baseWaitTime}")
    if (nextRetryCount < maxRetryCount && totalTimeWaited < maxWaitTime) {
      retry(func, Some(nextRetryCount), exponentialBackoff(nextRetryCount, baseWaitTime), maxRetryCount, maxWaitTime, totalTimeWaited + exponentialBackoff(nextRetryCount, baseWaitTime), baseWaitTime)
    } else {
      logger.trace(s"Failing attempt: ${retryCount}")
      throw ex
    }
  }
}