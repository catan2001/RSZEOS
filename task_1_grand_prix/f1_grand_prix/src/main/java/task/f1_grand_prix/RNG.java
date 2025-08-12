package task.f1_grand_prix;

import java.util.Random;

/**
 * RNG (Random Number Generator) class generates random values within a specified range.
 * It uses the Random class from Java standard library to generate random numbers.
 * This class ensures that the range is valid (maximumValue >= minimumValue).
 *
 * @author catic
 */
public class RNG {
    private int minimumValue;
    private int maximumValue;
    
    // Instance of Random class used to generate random numbers.
    private Random rnd;

    /**
     * Constructor initializes the Random instance for generating random numbers.
     */
    public RNG() {
        this.rnd = new Random(); // Initialize Random instance
    }

    /**
     * Getter for the minimum value.
     * 
     * @return The minimum value for the random number generation.
     */
    public int getMinimumValue() {
        return minimumValue;
    }

    /**
     * Setter for the minimum value.
     *
     * @param minimumValue The minimum value to set for the range.
     */
    public void setMinimumValue(int minimumValue) {
        this.minimumValue = minimumValue;
    }

    /**
     * Getter for the maximum value.
     *
     * @return The maximum value for the random number generation.
     */
    public int getMaximumValue() {
        return maximumValue;
    }

    /**
     * Setter for the maximum value.
     *
     * @param maximumValue The maximum value to set for the range.
     */
    public void setMaximumValue(int maximumValue) {
        this.maximumValue = maximumValue;
    }

    /**
     * Generates a random value within the specified range.
     * Throws an IllegalArgumentException if the maximumValue is less than minimumValue.
     *
     * @return A random integer between minimumValue and maximumValue (inclusive).
     * @throws IllegalArgumentException if maximumValue < minimumValue.
     */
    public int getRandomValue() {
        // Ensure that the maximum value is greater than or equal to the minimum value
        if (maximumValue < minimumValue) {
            throw new IllegalArgumentException("maximumValue must be >= minimumValue");
            // TODO: ovo izbaci jer svakako ne koristim try i catch?
        }
        
        // Return a random number between minimumValue and maximumValue (inclusive)
        return rnd.nextInt((maximumValue - minimumValue) + 1) + minimumValue;
    }
}
