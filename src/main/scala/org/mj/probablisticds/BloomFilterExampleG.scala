package org.mj.probablisticds

import com.google.common.hash.BloomFilter
import com.google.common.hash.Funnels
import com.typesafe.scalalogging.LazyLogging
import java.io.FileOutputStream
import java.io.FileInputStream
import com.google.common.base.Charsets

object BloomFilterExample extends App with LazyLogging {
  logger.info(s"Start ${this.getClass}")

  val readFilterFromFile = false
  val funnel = Funnels.stringFunnel(Charsets.UTF_8)
  val testTerms = Set ("is", "from")
  val sentences = Set(
    "Here are 65 examples of long an sentences ranging from the relatively brief 96 words to one of the longest sentences at 2,156 words. Almost all of the really long sentences are under 1,000 words. The six longest sentences (1,000+ words) are mostly a curiosity, just to see what is possible.",
    "Bloom filter is a data structure",
    "a b c",
    "",
    "")

  sentences.foreach(line => {
    val bfilter = if (readFilterFromFile) {
      // Read bloom from file
      val ios = new FileInputStream(s"./target/bloom-${line.hashCode}.blm")
      val bfilter = BloomFilter.readFrom(ios, funnel)
      ios.close()
      bfilter
    } else {
      // Create bloom
      val tokensArray = tokenize(line)
      val bfilter = BloomFilter.create[String](funnel, tokensArray.length, 0.01)
      tokensArray.foreach(token => bfilter.put(token))
      bfilter
    }

    println()
    println(s"${line.hashCode}: ${line}")
    testTerms.foreach(tt => println(s"${tt}: ${bfilter.mightContain(tt)}"))

    if (!readFilterFromFile) {
      // Write bloom to file
      val fos = new FileOutputStream(s"./target/bloom-${line.hashCode}.blm")
      bfilter.writeTo(fos)
      fos.close
    }
  })

  private def tokenize(line: String) = line.split(" ")
}