package org.mj.file

import java.util.concurrent.TimeUnit

import scala.util.Failure
import scala.util.Success
import scala.util.Try

import org.mj.http.HttpHandler

import com.typesafe.scalalogging.LazyLogging
import scala.annotation.tailrec
import java.util.concurrent.TimeoutException
import java.util.concurrent.CountDownLatch

object FunctionsHandler extends LazyLogging {
  /**
   * <p>Function for resolving a deep nested path in a hierarchical class structure.<br>Returns the Item or defaultVal else None Option</p>
   * <p><b>USAGE:</b><br> FunctionUtil.resolve(mm.getCanonicalEnvelope.getMessage.getKey.toBase64String, Some("default value"))</p>
   */
  def resolve[T](func: => T, default: Option[T] = None): Option[T] = {
    val tryResult = Try {
      val result = func
      if (result == null) default else Some(func)
    }
    tryResult match {
      case Success(result) => result
      case Failure(ex) => default
    }
  }

  /**
   * Reruns func given number of times
   */
  def repeat[R](func: => R, times: Int = 1, timed: Boolean = true): Seq[R] = {
    for (i <- 1 to times) yield { if (timed) time(func) else func }
  }

  /**
   * Sleeps for waitTime recursively till the latch finishes or maxWaitTime is exhausted
   * Returns total slept time
   */
  def waitSleep(latch: CountDownLatch, waitTimeMS: Int = 10, maxWaitTimeMS: Int = 1000): Int = {
    @tailrec
    def recursiveSleep(waitTimeMS: Int = 10, totalWaitTimeMS: Int = 0, maxWaitTimeMS: Int = 1000): Int = {
      logger.debug(s"Sleeping for $waitTimeMS with $maxWaitTimeMS sleep quota left!")
      if (latch.getCount == 0)
        return totalWaitTimeMS
      if (maxWaitTimeMS <= 0)
        throw new TimeoutException(s"Run exceeded maximum allowed time quota $maxWaitTimeMS (${latch.getCount})")
      Thread.sleep(waitTimeMS)
      recursiveSleep(waitTimeMS, totalWaitTimeMS + waitTimeMS, maxWaitTimeMS - waitTimeMS)
    }
    recursiveSleep(waitTimeMS, 0, maxWaitTimeMS)
  }

  def time[R](funcBlock: => R, funcName: String = "function"): R = {
    val startTime = System.nanoTime()
    val result = funcBlock // call-by-name
    val stopTime = System.nanoTime()
    val timeTaken = getTimeString(stopTime - startTime)

    logger.info(s"Took ${timeTaken}(s) to run ${funcName}")
    result
  }

  def getTimeString(nanoTime: Long): String = {
    nanoTime match {
      case pt if (pt >= TimeUnit.DAYS.toNanos(1)) => s"${pt / TimeUnit.DAYS.toNanos(1)} day"
      case pt if (pt >= TimeUnit.HOURS.toNanos(1)) => s"${pt / TimeUnit.HOURS.toNanos(1)} hour"
      case pt if (pt >= TimeUnit.MINUTES.toNanos(1)) => s"${pt / TimeUnit.MINUTES.toNanos(1)} minute"
      case pt if (pt >= TimeUnit.SECONDS.toNanos(1)) => s"${pt / TimeUnit.SECONDS.toNanos(1)} sec"
      case pt if (pt >= TimeUnit.MILLISECONDS.toNanos(1)) => s"${pt / TimeUnit.MILLISECONDS.toNanos(1)} millisec"
      case pt if (pt >= TimeUnit.MICROSECONDS.toNanos(1)) => s"${pt / TimeUnit.MICROSECONDS.toNanos(1)} μsec"
      case pt if (pt < TimeUnit.MICROSECONDS.toNanos(1)) => s"${pt / TimeUnit.NANOSECONDS.toNanos(1)} nanosec"
    }
  }
}