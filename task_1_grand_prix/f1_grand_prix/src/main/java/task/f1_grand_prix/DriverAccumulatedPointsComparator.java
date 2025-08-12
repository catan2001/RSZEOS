package task.f1_grand_prix;

/**
 *
 * @author catic
 */

import java.util.Comparator;

public class DriverAccumulatedPointsComparator implements Comparator<Driver>{
    	int direction = 1;
	
	public DriverAccumulatedPointsComparator(int direction) {
		if(direction!=1 && direction!=-1){
			direction = 1;
		}
		this.direction = direction;
	}
        
        @Override
	public int compare(Driver driver1, Driver driver2) {
		int retVal = 0;
		if(driver1!= null && driver2!=null){
			retVal = Integer.compare(driver1.getAccumulatedPoints(), driver2.getAccumulatedPoints());
		}
		return retVal * direction;
	}
}
