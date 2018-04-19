package org.mj.retry
import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext
import scala.util.Random

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.typesafe.scalalogging.LazyLogging

import junit.framework.TestCase

object RetryHandlerTest extends LazyLogging {
  org.apache.log4j.Logger.getRootLogger.setLevel(org.apache.log4j.Level.ALL)

  @Rule val wireMockRule = new WireMockRule(9090)

  @BeforeClass
  def beforeClass(): Unit = {
    logger.debug("in beforeClass")
  }

  @AfterClass
  def afterClass(): Unit = {
    logger.debug("in afterClass")
  }

}
class RetryHandlerJunitTest extends TestCase with LazyLogging {
  private implicit val ec = ExecutionContext.fromExecutorService(Executors.newWorkStealingPool(100))
  val func = {
    logger.debug("in func")
    Thread.sleep(2000)
    val res = Random.nextInt(10) ///Random.nextInt(10)
    logger.debug("in func end")
    res
  }

  @Before
  def beforeTests = {
    logger.info("in before")
  }

  @After
  def afterTests = {
    logger.debug("in after")
  }

  // TODO: Fix tests
  @Test
  def testSuccess = {
    val result = RetryHandler.retry[Int](func, 0.01, 3, 2)
    assert(result.get > 0, "retry handler returns results")
  }
  @Test
  def testTimeOut = {
    val resultTimeout = RetryHandler.retry[Int](func, 0.0001, 3, 2)
    assert(resultTimeout.get != 0, "Retry should timeout on exeeding timeout")
  }
}