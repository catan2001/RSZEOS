package task.f1_grand_prix;

/**
 * Driver class represents a participant in the Grand Prix race.
 * 
 * It contains details like the driver's name, ranking, special skill, 
 * race eligibility, accumulated time, points, and whether the driver has wet weather tires.
 * 
 * @author catic
 */
public class Driver {
    private String name;                    // Name of the driver
    private String specialSkill;            // Special skill of the driver (e.g., overtaking, braking)
    private int ranking;                    // Ranking of the driver in the championship
    private int accumulatedTime;            // The accumulated time in the race
    private int accumulatedPoints;          // The accumulated points based on race results
    private boolean eligibleToRace;         // Whether the driver is eligible to race or not
    private boolean wetWeatherPneumatics;   // Whether the driver is using wet weather tires

    /**
     * Constructor for creating a Driver with specified name, ranking, and special skill.
     * @param name
     * @param ranking
     * @param specialSkill
     */
    public Driver(String name, int ranking, String specialSkill) {
        this.name = name;
        this.ranking = ranking;
        this.specialSkill = specialSkill;
        this.accumulatedPoints = 0;
        this.accumulatedTime = 0;
        this.eligibleToRace = true;
        this.wetWeatherPneumatics = false;
    }

    /**
     * Default constructor for creating a driver with default values.
     */
    public Driver() {
        this.name = "Petar";
        this.ranking = 2;
        this.specialSkill = "Overtaking";
        this.accumulatedPoints = 0;
        this.accumulatedTime = 0;
        this.eligibleToRace = true;
        this.wetWeatherPneumatics = false;
    }

    // Getter and Setter for name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter and Setter for ranking
    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
        System.out.println("Driver ranking updated to: " + ranking);
    }

    // Getter and Setter for specialSkill
    public String getSpecialSkill() {
        return specialSkill;
    }

    public void setSpecialSkill(String specialSkill) {
        this.specialSkill = specialSkill;
    }

    // Getter and Setter for eligibility to race
    public boolean isEligibleToRace() {
        return eligibleToRace;
    }

    public void setEligibleToRace(boolean eligibleToRace) {
        this.eligibleToRace = eligibleToRace;
        System.out.println("Drivers " + name + " eligibility to race updated to: " + eligibleToRace);
    }

    // Getter and Setter for wetWeatherPneumatics
    public void setWetWeatherPneumatics(boolean wetWeatherPneumatics) {
        this.wetWeatherPneumatics = wetWeatherPneumatics;
        System.out.println("Drivers " + name + " wet weather tires status updated to: " + wetWeatherPneumatics);
    }
    
    public boolean isWetWeatherPneumatics() {
        return wetWeatherPneumatics;
    }

    // Getter and Setter for accumulatedTime
    public int getAccumulatedTime() {
        return accumulatedTime;
    }

    public void setAccumulatedTime(int accumulatedTime) {
        this.accumulatedTime = accumulatedTime;
        System.out.println("Driver " + this.name + " accumulated time updated to: " + accumulatedTime);
    }

    // Getter and Setter for accumulatedPoints
    public int getAccumulatedPoints() {
        return accumulatedPoints;
    }

    public void setAccumulatedPoints(int accumulatedPoints) {
        this.accumulatedPoints = accumulatedPoints;
        System.out.println("Driver has " + this.name + " accumulated points updated to: " + accumulatedPoints);
    }

    /**
     * Uses the driver's special skill to modify their accumulated time.
     * The effect of the skill is determined by the RNG.
     *
     * @param rng Random number generator to determine the effect.
     */
    public void useSpecialSkill(RNG rng) {
        int skillEffect = rng.getRandomValue();
        this.setAccumulatedTime(this.getAccumulatedTime() - skillEffect);
        System.out.println("Driver " + this.name + " special skill took effect: " + skillEffect + " seconds subtracted.");
    }
}
