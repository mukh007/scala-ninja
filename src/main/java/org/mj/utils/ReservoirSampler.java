package org.mj.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * An in memory implementation of Reservoir Sampling for sampling from a population.
 * 
 * @param <T> Type of the sample
 */
public class ReservoirSampler<T> {
  private final List<T> reservoir = new ArrayList<T>();
  @Min(value=1)
  private final int numSamples;
  @NotNull
  private final Random random;
  private int numItemsSeen = 0;

  /**
   * Create a new sampler with a certain reservoir size using a supplied random number generator.
   *
   * @param numSamples Maximum number of samples to retain in the reservoir. Must be non-negative.
   * @param random Instance of the random number generator to use for sampling
   */
  public ReservoirSampler(int numSamples, Random random) {
    this.numSamples = numSamples;
    this.random = random;
  }

  /**
   * Create a new sampler with a certain reservoir size using the default random number generator.
   *
   * @param numSamples Maximum number of samples to retain in the reservoir. Must be non-negative.
   */
  public ReservoirSampler(int numSamples) {
    this(numSamples, new Random());
  }

  /**
   * Sample an item and store in the reservoir if needed.
   *
   * @param item The item to sample - may not be null.
   */
  public void sample(T item) {
    if (reservoir.size() < numSamples) {
      reservoir.add(item); // reservoir not yet full, just add
    } else {
      int rIndex = random.nextInt(numItemsSeen + 1); // find a sample to replace
      if (rIndex < numSamples) {
        reservoir.set(rIndex, item);
      }
    }
    numItemsSeen++;
  }

  /**
   * Sample a list of items and store in the reservoir if needed.
   *
   * @param item The item to sample - may not be null.
   */
  public void sample(List<T> item) {
    for(T i : item) {
    	sample(i);
    }
  }

  
  /**
   * Get samples collected in the reservoir.
   *
   * @return A sequence of the samples. No guarantee is provided on the order of the samples.
   */
  public List<T> getSamples() {
    return reservoir;
  }

  /**
   *
   * @return The total number of items seen by the reservoir..
   */
  public Integer getNumItemsSeen() {
    return numItemsSeen;
  }
}