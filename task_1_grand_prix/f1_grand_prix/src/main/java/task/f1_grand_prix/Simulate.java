package task.f1_grand_prix;

import java.util.*;

/**
 * Simulate class runs the Grand Prix simulation, allowing user interaction to
 * set up and run a series of races. The simulation will initialize race tracks,
 * drivers, and handle the race events such as weather, mechanical issues, and
 * special skills. After all races are completed, it will declare the winner of
 * the championship.
 *
 * @author catic
 */
public class Simulate {

    public static void main(String[] args) {
        // Initialize Scanner for user input
        Scanner scanner = new Scanner(System.in);

        // Create a Championship object, which will handle the race tracks and drivers
        System.out.println("Loading the Drivers and Tracks...");
        Championship championship = new Championship();
        System.out.println("\nLoaded! Game is ready.\n\n");

        System.out.println("-----------------------------------");
        System.out.println("Welcome brajko moj to the Grand Prix Championship, where legends are made!");
        System.out.println("You, the creator of this championship, now hold the power to shape the race season.");
        System.out.println("The moment of truth has come!");
        System.out.println("As the creator of this championship, you have the honor of choosing:");
        System.out.println("- The number of races in the Grand Prix.");
        System.out.println("- The specific tracks for each race.");

        int numOfRaces = 0;
        System.out.println("-----------------------------------");
        System.out.print("Enter the number of races in the championship (3-5): ");
        while (numOfRaces < 3 || numOfRaces > 5) {
            numOfRaces = scanner.nextInt();
            if (numOfRaces < 3 || numOfRaces > 5) {
                System.out.println("Valid number for selection is 3-5. Please enter a valid number.");
            }
        }
        scanner.nextLine();
        System.out.println("-----------------------------------\n");


        // Create list of available venues and of used venues
        List<Venue> availableVenues = new ArrayList<>(championship.getVenues());
        List<Venue> usedVenues = new ArrayList<>();

        System.out.println("-----------------------------------");
        System.out.println("Please select the tracks your Champions will drive on.");
        System.out.println("-----------------------------------");
        /*
        NAPOMENA: kasno sam primjetio da je zapravo trebalo ici pitanje za jendu stazu pa odmah trka,a ne
         */
        for (int j = 0; j < numOfRaces; ++j) {
            Venue selectedVenue = null;

            // Ensure the selected venue is not null and not already used
            while (selectedVenue == null) {
                System.out.println("\nAvailable " + availableVenues.size() + " race tracks:");
                int i = 0;

                // List available race tracks
                for (Venue venue : availableVenues) {
                    if (!usedVenues.contains(venue)) {
                        System.out.println(++i + ". Track: " + venue.getVenueName());
                    }
                }

                // Get user input for the race track number
                System.out.print("Enter the track number for race " + (j + 1) + ": ");
                int trackNumber = Integer.parseInt(scanner.nextLine().trim());  // Get the number input

                // Ensure the track number is valid
                if (trackNumber < 1 || trackNumber > availableVenues.size()) {
                    System.out.println("Invalid track number. Please try again.");
                    continue;
                }

                // Find the selected venue by index and mark it as used
                selectedVenue = availableVenues.get(trackNumber - 1); // Get the venue based on the number
                availableVenues.remove(trackNumber - 1);
                if (usedVenues.contains(selectedVenue)) {
                    System.out.println("The selected track has already been used. Please try again.");
                    selectedVenue = null;
                } else {
                    usedVenues.add(selectedVenue);  // Add the venue object to usedVenues list
                    System.out.println("Track " + selectedVenue.getVenueName() + " selected.");
                }

                // If no valid track is selected, ask again
                if (selectedVenue == null) {
                    System.out.println("Invalid selection. Please try again.");
                }
            }
        }

        System.out.println("-----------------------------------");
        System.out.println("And the Championship may start!!!");
        System.out.println("-----------------------------------\n");
        // Loop through each race
        for (int raceIndex = 0; raceIndex < numOfRaces; ++raceIndex) {
            Venue selectedVenue = usedVenues.get(raceIndex);

            System.out.println("\n-----------------------------------");
            System.out.println("Preparing the race " + (raceIndex + 1) + " at track: " + selectedVenue.getVenueName());
            System.out.println("-----------------------------------");
            // Initialize driver attributes for the new race
            championship.prepareForTheRace();
            championship.setCurrentVenue(selectedVenue);
            championship.setCurrentLap(0);
            
            System.out.println("\n-----------------------------------");
            System.out.println("Starting the race " + (raceIndex + 1) + " at track: " + selectedVenue.getVenueName());
            System.out.println("-----------------------------------");

            int numLaps = selectedVenue.getNumberOfLaps();
            boolean isRaining = false;
            // Loop through each lap
            for (int lap = 1; lap <= numLaps; lap++) {
                // Determine if it rains and update driver statuses
                RNG rng = new RNG();
                rng.setMinimumValue(0);
                rng.setMaximumValue(99);
                
                // If it is the second lap, drivers may switch to wet weather tires
                if (lap == 2) {
                    for (Driver driver : championship.getDrivers()) {
                        if (!driver.isEligibleToRace()) {
                            continue; // Skip driver if they are not eligible to race
                        }
                        if (rng.getRandomValue() < 50) {
                            driver.setWetWeatherPneumatics(true);
                            driver.setAccumulatedTime(driver.getAccumulatedTime() + 10);
                            System.out.println(driver.getName() + " changed to wet weather tires.");
                        }
                    }
                    System.out.println("\n");
                }

                // Check the chance of rain based on the tracks weather conditions
                isRaining = rng.getRandomValue() < (selectedVenue.getChanceOfRain()) * 100;
                if (isRaining) {
                    System.out.println("Lap: " + lap + " It started raining!\n" );
                } else {
                    System.out.println("Lap: " + lap + " Weather is dry.\n");
                }
                
                // If it rains the drivers without wet weather tires suffer time penalties
                if (isRaining) {
                    for (Driver driver : championship.getDrivers()) {
                        if (!driver.isEligibleToRace()) {
                            continue; // Skip driver if theyre not eligible
                        }
                        if (!driver.isWetWeatherPneumatics()) {
                            driver.setAccumulatedTime(driver.getAccumulatedTime() + 5);
                            System.out.println(driver.getName() + " suffers a 5-second penalty due to rain.");
                        }
                    }
                }
                
                // Simulate average lap time
                championship.driveAverageLapTime();
                
                championship.setCurrentLap(lap-1);
                championship.applySpecialSkills();

                championship.checkMechanicalProblem();
                
                championship.printLeader(lap);
                
                System.out.println("\n-----------------------------------");
                System.out.println("Lap " + lap + " completed with average lap time: " + selectedVenue.getAverageLapTime());
                System.out.println("-----------------------------------");
            }

            System.out.println("-----------------------------------");
            System.out.println("The race on the track " + selectedVenue.getVenueName() + " has finished!");
            System.out.println("-----------------------------------\n");

            championship.printWinnersAfterRace(selectedVenue.getVenueName());
            championship.updateDriversPoints();
        }

        System.out.println("-----------------------------------");
        System.out.println("The Championship has ended!");
        System.out.println("-----------------------------------");

        championship.printChampion(numOfRaces);

        scanner.close();
    }
}
