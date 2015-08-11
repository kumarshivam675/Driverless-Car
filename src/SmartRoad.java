
import java.util.ArrayList;
import java.util.Collections;

import org.iiitb.es103_15.traffic.Car;
import org.iiitb.es103_15.traffic.Coords;
import org.iiitb.es103_15.traffic.Intersection;
import org.iiitb.es103_15.traffic.Road;
import org.iiitb.es103_15.traffic.RoadGrid;


public class SmartRoad extends Road {

	public SmartRoad(int dir, Intersection start, Intersection end) {
		super(dir, start, end);
	}

	public void checkCollisions() {
		int i = 0;
		while (i < 2) {
			int dir;
			if(i==0)
				dir =getDir();
			else
				dir = RoadGrid.getOppDir(getDir());
			ArrayList<Car> cars = this.getCarsL(dir);
			if (cars.size()!=0){
				cars.get(0).carInFront(null);
				for (int j = 1; j < cars.size(); j++) {
					Car c1 = cars.get(j-1);
					Car c2 = cars.get(j);
					c2.carInFront(c1);
				}
			}
			i++;
		}
	}
}