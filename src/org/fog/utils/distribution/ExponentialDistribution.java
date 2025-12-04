package org.fog.utils.distribution;

import java.util.Random;

/**
 * Exponential Distribution for sensor tuple emission rates
 * The exponential distribution is commonly used to model the time between events
 * in a Poisson process (e.g., sensor readings in IoT applications)
 */
public class ExponentialDistribution extends Distribution {

	private double mean;
	
	/**
	 * Creates a new exponential distribution
	 * @param mean The mean inter-transmission time (in milliseconds)
	 */
	public ExponentialDistribution(double mean) {
		setMean(mean);
		setRandom(new Random());
	}
	
	/**
	 * Generates the next value from the exponential distribution
	 * Uses inverse transform sampling: -mean * ln(1 - U) where U is uniform(0,1)
	 */
	@Override
	public double getNextValue() {
		// Generate exponentially distributed random value
		// -mean * ln(U) where U is uniform(0,1)
		return -mean * Math.log(1 - random.nextDouble());
	}

	public double getMean() {
		return mean;
	}

	public void setMean(double mean) {
		this.mean = mean;
	}
	
	@Override
	public int getDistributionType() {
		return Distribution.EXPONENTIAL;
	}

	@Override
	public double getMeanInterTransmitTime() {
		return mean;
	}
}

