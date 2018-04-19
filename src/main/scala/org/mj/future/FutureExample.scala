package org.mj.future
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Random
import scala.util.Success
import scala.util.Try

import org.joda.time.DateTime

import com.typesafe.scalalogging.LazyLogging

object FutureExample extends App with LazyLogging {
  org.apache.log4j.Logger.getRootLogger.setLevel(org.apache.log4j.Level.DEBUG)

  logger.info(s"Starting ${this.getClass.getSimpleName} @ ${DateTime.now}")

  private val initDelay = 10
  private val randDelay = 90
  private val totalCall = 100
  private val waitForCompletion = true
  private implicit val ec = ExecutionContext.fromExecutorService(Executors.newWorkStealingPool(totalCall / 2))

  runFutureExamples("f")

  // PrivateFunctions

  private def runFutureExamples(mode: String) = {
    val latch = new CountDownLatch(totalCall)
    val result = for (i <- 1 to totalCall) yield {
      val f = mode.toLowerCase match {
        case "f" | "future" => getFuture(s"F-${i}", func)
        case "t" | "futurewithtry" => Future.fromTry(getTry(s"F-${i}", func))
        case _ => getFuture(s"F-${i}", func)
      }
      f.onComplete(_ => { latch.countDown })
      f
    }
    while (waitForCompletion && latch.getCount > 0) {
      logger.info(s"latch is at ${latch.getCount}")
      Thread.sleep(10)
    }
    val rl = result.map(_.value.get).flatMap(_.toOption)
    logger.info(s"latch finished at ${latch.getCount}")

    rl.foreach(r => logger.info(s"${r}"))
  }

  /**
   * Function to be called
   */
  private def func[T](name: T): String = {
    val st = initDelay + Random.nextInt(randDelay)
    Thread.sleep(st)
    val result = s"${name} slept for $st ms div ${Random.nextInt(10) / Random.nextInt(10)}" //.split(" ").toList
    logger.info(s"Finished ${result}")
    result
  }
  /**
   * This is sync
   */
  private def getFutureFromTry[IN, OUT](name: IN, func: (IN) => OUT) = {
    Future.fromTry(getTry(name, func))
  }
  private def getTry[IN, OUT](name: IN, func: (IN) => OUT) = {
    val t = Try { func(name) }
    t match {
      case Success(result) => result
      case Failure(ex) => ex
    }
    t
  }

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
}