package org.mj.probablisticds

import com.typesafe.scalalogging.LazyLogging
import java.io.FileOutputStream
import java.io.FileInputStream
import com.google.common.base.Charsets
import com.clearspring.analytics.stream.membership.BloomFilter
import org.apache.commons.io.IOUtils

object BloomFilterExampleCS extends App with LazyLogging {
  logger.info(s"Start ${this.getClass}")

  val readFilterFromFile = false
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
      val ba: Array[Byte] = null
      IOUtils.read(ios, ba)
      val bfilter = BloomFilter.deserialize(ba)
      ios.close()
      bfilter
    } else {
      // Create bloom
      val tokensArray = tokenize(line)
      val bfilter = new BloomFilter(tokensArray.length, 0.01)
      tokensArray.foreach(token => bfilter.add(token))
      bfilter
    }

    println()
    println(s"${line.hashCode}: ${line}")
    testTerms.foreach(tt => println(s"${tt}: ${bfilter.isPresent(tt)}"))

    if (!readFilterFromFile) {
      // Write bloom to file
      val fos = new FileOutputStream(s"./target/bloom-${line.hashCode}.blm")
      val ba = BloomFilter.serialize(bfilter)
      fos.write(ba)
      fos.close
    }
  })

  private def tokenize(line: String) = line.split(" ")
}