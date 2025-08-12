package task.f1_grand_prix;

/**
 * Championship class manages the whole Grand Prix competition,
 * including handling the drivers, venues, race simulation, mechanical issues, and point calculations.
 * 
 * @author catic
 */
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;

public class Championship {

    private ArrayList<Driver> drivers;
    private ArrayList<Venue> venues;
    private Venue currentVenue;
    private int currentLap;

    // Constants for mechanical fault probabilities and times
    private final int MINOR_MECHANICAL_FAULT = 5;           // Probability in %
    private final int MINOR_MECHANICAL_FAULT_TIME = 20;     // Time penalty in seconds
    private final int MAJOR_MECHANICAL_FAULT = 3;           // Probability in %
    private final int MAJOR_MECHANICAL_FAULT_TIME = 120;    // Time penalty in seconds
    private final int UNRECOVERABLE_MECHANICAL_FAULT = 1;   // Probability in %

    // Points awarded to the first four positions
    private final int POINTS_AFTER_RACE_PLACE_1 = 8;
    private final int POINTS_AFTER_RACE_PLACE_2 = 5;
    private final int POINTS_AFTER_RACE_PLACE_3 = 3;
    private final int POINTS_AFTER_RACE_PLACE_4 = 1;
    
    // Time penalties based on rankings
    private final int TIME_RANK_1 = 0;
    private final int TIME_RANK_2 = 3;
    private final int TIME_RANK_3 = 5;
    private final int TIME_RANK_4 = 7;
    private final int TIME_RANK_5_AND_MORE = 10;

    /**
     * Constructor for Championship.
     * Reads driver and venue data from files and initializes the drivers and venues lists.
     */
    public Championship() {
        System.out.println("Creating Championship...");
        String sP = System.getProperty("file.separator");
        File f_drivers = new File("data" + sP + "vozaci.txt");
        File f_venues = new File("data" + sP + "staze.txt");
        this.drivers = new ArrayList<>();
        this.venues = new ArrayList<>();
        this.currentLap = 0;

        try {
            if (f_drivers.exists()) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(new FileInputStream(f_drivers), "UTF8"));
                String row;
                while ((row = in.readLine()) != null) {
                    //System.out.println("Loaded driver: " + row);
                    String[] row_parsed = row.split(",");
                    Driver current = new Driver(row_parsed[0], Integer.parseInt(row_parsed[1]), row_parsed[2]);
                    this.drivers.add(current);
                }
                in.close();
            } else {
                System.out.println("File not found: drivers.txt!");
            }
        } catch (IOException e) {
            System.err.println("Failed to read drivers.txt: " + e.getMessage());
        }

        try {
            if (f_venues.exists()) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(new FileInputStream(f_venues), "UTF8"));
                String row;
                while ((row = in.readLine()) != null) {
                    //System.out.println("Loaded venue: " + row);
                    String[] row_parsed = row.split(",");
                    Venue current = new Venue(row_parsed[0], Integer.parseInt(row_parsed[1]), Integer.parseInt(row_parsed[2]), Double.parseDouble(row_parsed[3]));
                    this.venues.add(current);
                }
                in.close();
            } else {
                System.out.println("File not found: venues.txt");
            }
        } catch (IOException e) {
            System.err.println("Failed to read venues.txt: " + e.getMessage());
        }

        // Print loaded venues information
        /*for (Venue ven : this.venues) {
            System.out.println("Venue: " + ven.getVenueName());
            System.out.println("Number of laps: " + ven.getNumberOfLaps());
            System.out.println("Average lap time: " + ven.getAverageLapTime());
            System.out.println("Chance of rain: " + ven.getChanceOfRain());
        }*/
    }

    // Getters and Setters for drivers, venues and current lap
    public ArrayList<Driver> getDrivers() {
        return drivers;
    }

    public void setDrivers(ArrayList<Driver> drivers) {
        this.drivers = drivers;
    }

    public ArrayList<Venue> getVenues() {
        return venues;
    }

    public void setVenues(ArrayList<Venue> venues) {
        this.venues = venues;
    }
    
    public int getCurrentLap() {
        return currentLap;
    }
    
    public void setCurrentLap(int currentLap) {
        this.currentLap = currentLap;
    }

    // Getter for fault constants
    public int getMinorMechanicalFault() {
        return MINOR_MECHANICAL_FAULT;
    }

    public int getMajorMechanicalFault() {
        return MAJOR_MECHANICAL_FAULT;
    }

    public int getUnrecoverableMechanicalFault() {
        return UNRECOVERABLE_MECHANICAL_FAULT;
    }

    /**
     * Prepares the drivers for the race by resetting their status and setting their initial time based on ranking.
     */
    public void prepareForTheRace() {
        for (Driver driver : drivers) {
            System.out.println("\nPreparing driver: " + driver.getName() + " at Starting position: " + driver.getRanking());
            driver.setEligibleToRace(true);
            driver.setWetWeatherPneumatics(false);
           
            // Set initial time based on driver's ranking
            if(driver.getRanking() == 1) driver.setAccumulatedTime(this.TIME_RANK_1);
            else if(driver.getRanking() == 2) driver.setAccumulatedTime(this.TIME_RANK_2);
            else if(driver.getRanking() == 3) driver.setAccumulatedTime(this.TIME_RANK_3);
            else if(driver.getRanking() == 4) driver.setAccumulatedTime(this.TIME_RANK_4);
            else if(driver.getRanking() >= 5) driver.setAccumulatedTime(this.TIME_RANK_5_AND_MORE);
        }
    }

    /**
     * Adds the average lap time for each driver based on the current venue.
     */
    public void driveAverageLapTime() {
        for (Driver driver : this.drivers) {
            if (!driver.isEligibleToRace()) {
                continue; // Skip driver if they are not eligible to race
            }
            driver.setAccumulatedTime(driver.getAccumulatedTime() + this.currentVenue.getAverageLapTime());
        }
        System.out.println("Added average lap time to eligable drivers.");
    }

    /**
     * Sets the current venue for the race.
     * @param currentVenue
     */
    public void setCurrentVenue(Venue currentVenue) {
        this.currentVenue = currentVenue;
        System.out.println("Current venue set to: " + currentVenue.getVenueName());
    }

    /**
     * Applies special skills to each driver based on their special skill type.
     */
    public void applySpecialSkills() {
        RNG rng = new RNG();  // Create the RNG object
        for (Driver driver : this.drivers) {
            if (!driver.isEligibleToRace()) {
                continue; // Skip if driver is not eligible
            }
            switch (driver.getSpecialSkill()) {
                case "Overtaking":
                    if(currentLap % 3 == 0) {
                        rng.setMinimumValue(10);
                        rng.setMaximumValue(20);
                        break; // break and subtract time
                    } 
                    else {
                        System.out.println("\n" + driver.getName() + "s abbility to overtake is on cooldown.");
                        continue; // Skip if driver is on cooldown
                    }
                case "Braking":
                case "Cornering":
                    rng.setMinimumValue(1);
                    rng.setMaximumValue(8);
                    break;
                default:
                    System.out.println("This ability does not exist!");
                    // Add default case if needed
                    break;
            }
            System.out.println("\nApplying special skill [" + driver.getSpecialSkill() + "] for driver: " + driver.getName());
            driver.useSpecialSkill(rng);
        }
    }

    /**
     * Checks for mechanical problems during the race and applies penalties accordingly.
     */
    public void checkMechanicalProblem() {
        RNG rng = new RNG();
        rng.setMinimumValue(0);
        rng.setMaximumValue(99);  // RNG produces [0, 99]

        for (Driver driver : this.drivers) {
            if (!driver.isEligibleToRace()) {
                continue; // Skip if driver is not eligible
            }
            int chance = rng.getRandomValue();

            if (chance < this.getUnrecoverableMechanicalFault()) {
                driver.setEligibleToRace(false);  // Driver is out of the race
                System.out.println(driver.getName() + " is out of the race due to unrecoverable mechanical fault.");
            } else if (chance < this.getUnrecoverableMechanicalFault() + this.getMajorMechanicalFault()) {
                driver.setAccumulatedTime(driver.getAccumulatedTime() + this.MAJOR_MECHANICAL_FAULT_TIME);
                System.out.println(driver.getName() + " has a major fault! +120s penalty.");
            } else if (chance < this.getUnrecoverableMechanicalFault() + this.getMajorMechanicalFault() + this.getMinorMechanicalFault()) {
                driver.setAccumulatedTime(driver.getAccumulatedTime() + this.MINOR_MECHANICAL_FAULT_TIME);
                System.out.println(driver.getName() + " has a minor fault. +20s penalty.");
            }
        }
    }

    /**
     * Updates the points of the drivers based on their positions in the race.
     */
    public void updateDriversPoints() {
        int[] pointsPerPlace = {
            this.POINTS_AFTER_RACE_PLACE_1,
            this.POINTS_AFTER_RACE_PLACE_2,
            this.POINTS_AFTER_RACE_PLACE_3,
            this.POINTS_AFTER_RACE_PLACE_4
        };

        int awardedCount = 0;

        for (Driver driver : drivers) {
            if (!driver.isEligibleToRace()) {
                continue;
            }

            if (awardedCount < 4) {
                driver.setAccumulatedPoints(driver.getAccumulatedPoints() + pointsPerPlace[awardedCount]);
                awardedCount++;
            } else {
                break; // Points have been awarded to the top 4 drivers
            }
        }
    }

    /**
     * Sorts the drivers by their accumulated points.
     * @param sort_way
     */
    public void sortDriversByPoints(int sort_way) {
        Collections.sort(this.drivers, new DriverAccumulatedPointsComparator(sort_way));
        System.out.println("Drivers sorted by points.");
    }

    /**
     * Sorts the drivers by their accumulated race time.
     * @param sort_way
     */
    public void sortDriversByTime(int sort_way) {
        Collections.sort(this.drivers, new DriverAccumulatedTimeComparator(sort_way));
        System.out.println("Drivers sorted by race time.");
    }

    /**
     * Updates the rankings of the drivers based on their points.
     */
    public void updateRankings() {
        this.sortDriversByPoints(-1);  // Sort by points in descending order
        for (int i = 0; i < this.drivers.size(); ++i) {
            drivers.get(i).setRanking(i + 1);  // Update the ranking
        }
        System.out.println("Updated driver rankings.");
    }

    /**
     * Prints the current leader of the race for a specific lap.
     * @param lap
     */
    public void printLeader(int lap) {
        this.sortDriversByTime(1);
        for (Driver driver : this.drivers) {
            if (driver.isEligibleToRace()) {
                System.out.println("\nLap: " + lap + " Current leader: " + driver.getName() + " with Time: " + driver.getAccumulatedTime());
                break;
            }
        }
    }

    /**
     * Prints the top 4 drivers at the end of a race at a given venue.
     * @param venueName
     */
    public void printWinnersAfterRace(String venueName) {
        this.sortDriversByTime(1);
        System.out.println("Top 4 drivers at " + venueName + " are:");
        int i = 0;
        for (Driver driver : this.drivers) {
            if (!driver.isEligibleToRace()) {
                continue;
            }
            System.out.println(++i + ". " + driver.getName() + " Time: " + driver.getAccumulatedTime());
            if (i >= 4) break;
        }
    }

    /**
     * Prints the champion of the championship after all races.
     * @param numOfRaces
     */
    public void printChampion(int numOfRaces) {
        this.sortDriversByPoints(-1);  // Sort by points in descending order
        System.out.println("After " + numOfRaces + " races, the champion is: " + drivers.get(0).getName()+ "\t Points: " + drivers.get(0).getAccumulatedPoints());
    }
}
