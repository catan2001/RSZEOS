package task.f1_grand_prix;

/**
 * Venue class represents a race track in the Grand Prix.
 * @author catic
 */
public class Venue {
    private int averageLapTime;    // Average lap time at this venue
    private double chanceOfRain;   // Chance of rain during the race (as a percentage)
    private int numberOfLaps;      // Number of laps in the race
    private String venueName;      // Name of the venue

    
    public Venue() {
        this.averageLapTime = 0;
        this.chanceOfRain = 0;
        this.numberOfLaps = 0;
        this.venueName = "";
    }

    /**
     * Constructor initializes venue with provided values.
     *
     * @param venueName       Name of the venue (track)
     * @param numberOfLaps    Number of laps in the race at this venue
     * @param averageLapTime  Average lap time at the venue
     * @param chanceOfRain    The probability of rain during the race (between 0 and 1)
     */
    public Venue(String venueName, int numberOfLaps, int averageLapTime, double chanceOfRain) {
        this.venueName = venueName;
        this.numberOfLaps = numberOfLaps;
        this.averageLapTime = averageLapTime;
        this.chanceOfRain = chanceOfRain;
    }

    /**
     * Gets the average lap time at the venue.
     *
     * @return Average lap time in seconds.
     */
    public int getAverageLapTime() {
        return averageLapTime;
    }

    /**
     * Sets the average lap time at the venue.
     *
     * @param averageLapTime Average lap time in seconds.
     */
    public void setAverageLapTime(int averageLapTime) {
        this.averageLapTime = averageLapTime;
    }

    /**
     * Gets the chance of rain during the race at this venue.
     *
     * @return Chance of rain as a percentage (0 to 1).
     */
    public double getChanceOfRain() {
        return chanceOfRain;
    }

    /**
     * Sets the chance of rain at this venue.
     *
     * @param chanceOfRain Chance of rain as a percentage (0 to 1).
     */
    public void setChanceOfRain(double chanceOfRain) {
        this.chanceOfRain = chanceOfRain;
    }

    /**
     * Gets the number of laps for a race at this venue.
     *
     * @return Number of laps.
     */
    public int getNumberOfLaps() {
        return numberOfLaps;
    }

    /**
     * Sets the number of laps for the race at this venue.
     *
     * @param numberOfLaps Number of laps.
     */
    public void setNumberOfLaps(int numberOfLaps) {
        this.numberOfLaps = numberOfLaps;
    }

    /**
     * Gets the name of the venue (race track).
     *
     * @return Venue name.
     */
    public String getVenueName() {
        return venueName;
    }

    /**
     * Sets the name of the venue (race track).
     *
     * @param venueName Name of the venue.
     */
    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }
}
