package org.mj.probablisticds



import com.github.mgunlogson.cuckoofilter4j.CuckooFilter
import com.github.mgunlogson.cuckoofilter4j.Utils.Algorithm
import com.google.common.base.Charsets
import com.google.common.hash.Funnels
import com.typesafe.scalalogging.LazyLogging

object CuckooFilterExample extends App with LazyLogging {
  logger.info(s"Start ${this.getClass}")
  val readFilterFromFile = false
  val funnel = Funnels.stringFunnel(Charsets.UTF_8)
  val testTerms = Set ("is", "from")
  val sentences = Array(
    "Here are 65 examples of long sentences ranging from the relatively brief 96 words to one of the longest sentences at 2,156 words. Almost all of the really long sentences are under 1,000 words. The six longest sentences (1,000+ words) are mostly a curiosity, just to see what is possible.",
    "Cuckoo filter data structure",
    "a b c",
    "")

  sentences.foreach(line => {
    val cfilter: CuckooFilter[String] = if (readFilterFromFile) {
      // TODO: Read bloom from file
      new CuckooFilter.Builder[String](funnel, 3).build()
    } else {
      // Create bloom
      val tokensArray = tokenize(line)
      val cfilter = new CuckooFilter.Builder[String](funnel, 3 + tokensArray.length)
        .withFalsePositiveRate(0.01)
        .withHashAlgorithm(Algorithm.Murmur3_32)
        .withExpectedConcurrency(2)
        .build()
      tokensArray.foreach(token => cfilter.put(token))
      cfilter
    }

    println()
    println(s"${line.hashCode}: ${line}")
    testTerms.foreach(tt => println(s"${tt}: ${cfilter.mightContain(tt)}"))
    println(s"c_${cfilter.getCount}-ac_${cfilter.getActualCapacity}-lf_${cfilter.getLoadFactor}-ss_${cfilter.getStorageSize}")

    if (!readFilterFromFile) {
      // Write bloom to file
    }
  })

  private def tokenize(line: String) = line.split(" ")
}

class CuckooFilterExample {}
