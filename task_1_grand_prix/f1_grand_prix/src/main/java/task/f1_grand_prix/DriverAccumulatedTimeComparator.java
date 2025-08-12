package task.f1_grand_prix;

/**
 *
 * @author catic
 */
import java.util.Comparator;

public class DriverAccumulatedTimeComparator implements Comparator<Driver>{
        	int direction = 1;
	
	public DriverAccumulatedTimeComparator(int direction) {
		if(direction!=1 && direction!=-1){
			direction = 1;
		}
		this.direction = direction;
	}
        
        @Override
	public int compare(Driver driver1, Driver driver2) {
		int retVal = 0;
		if(driver1!= null && driver2!=null){
			retVal = Integer.compare(driver1.getAccumulatedTime(), driver2.getAccumulatedTime());
		}
		return retVal * direction;
	}
}
