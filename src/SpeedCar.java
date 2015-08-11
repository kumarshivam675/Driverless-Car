import org.iiitb.es103_15.traffic.*;
import java.util.Random;

public class SpeedCar extends Car implements TrafficSignal.SignalListener{

	/**
	 * @param args
	 */
	
	private TrafficSignal.SignalListener watch;
	private TrafficSignal light;
	private boolean cross;
	private Car obstacle;
	private Random random;
	
	public SpeedCar() {
		super();
		cross = true;
		obstacle = null;
	}
	
	public void setInitialPos(Road r, Coords loc, int dir) {
		super.setInitialPos(r, loc, dir);
		watchSignal();
	}
	
	private Intersection end(Road r) {
		if(getDir() == r.getDir())
			return r.getEndIntersection();
		else
			return r.getStartIntersection();
	}
	
	private int direction(int  curr) {
		if(curr == RoadGrid.EAST)
			return RoadGrid.WEST;
		else if(curr == RoadGrid.WEST)
			return RoadGrid.EAST;
		else if(curr == RoadGrid.NORTH)
			return RoadGrid.SOUTH;
		else
			return RoadGrid.NORTH;
	}
	
	private int chooseRoad(Intersection i) {
		Road[] temp = i.getRoads();
		random = new Random();
		int randDir = random.nextInt(4);
		while(temp[randDir] == null || getDir() == direction(randDir))
			randDir = random.nextInt(4);
		return randDir;
	}
	
	private void avoidCollision(float distance){
		float gap = Coords.distSqrd(obstacle.getPos(), getPos());
		if(getSpeed() < 10)
			accelerate(0, 500);
		if(distance > gap && gap < 70000 && obstacle.getSpeed() < getSpeed()) {
			float acc = (float) (Math.pow(obstacle.getSpeed(), 2) - Math.pow(getSpeed(), 2));
			acc = acc/(2*(float)Math.pow(gap, 0.5));
			accelerate(acc, 500);
		}
	}
	
	private void handleTurn(Intersection i) {
		if(light != null)
			synchronized(light){light.removeListener(watch,  direction(getDir()));}

		crossIntersection(i, chooseRoad(i));
	
		watchSignal();
		
	}
	
	private void watchSignal() {
		if(end(getRoad()).getTrafficControl() != null) {
			if(end(getRoad()).getTrafficControl().getType() == TrafficControl.SIGNAL_LIGHT) {
				light = (TrafficSignal) end(getRoad()).getTrafficControl();
				watch = create();
				synchronized(light){light.addListener(watch, direction(getDir()));}
				if(light.getSignalState(direction(getDir())) == TrafficSignal.RED_LIGHT)
					cross = false;
				else
					cross = true;
			}
		}
		else {
			light = null;
			cross = true;
		}
	}
	
	private void excess(Road r, float distance) {
		float acc = (float) (Math.pow(getSpeed(), 2) - Math.pow(r.getSpeedLimit(), 2));
		accelerate(-acc/(float)Math.pow(distance, 0.5), 500);
	}
	
	private void limitReached() {
		accelerate(0,500);
	}
	
	private void nearJunction(float distance) {
			if(getSpeed() > 11) {
				float acc = (float) Math.pow(getSpeed(), 2) - 4;
				acc = acc/(float)(2*(Math.pow(distance, 0.5)));
				accelerate(-acc, 500);
			}
			else if(getSpeed()<=11) {
				float acc = (float)(Math.pow(getSpeed(), 2) - 4);
				accelerate(-acc, 500);
			}
			else
				accelerate(0, 500);
	}
	
	private void slowRoad(Road r, float distance) {
		if(distance < 120)
			accelerate(-getSpeed(), 500);
		else if(r.getSpeedLimit() - getSpeed() > 0.004 && getSpeed() < r.getSpeedLimit())
			accelerate(5, 500);
		else if(getSpeed() > r.getSpeedLimit() + 2)
			accelerate(-3,500);
		else
			accelerate(0, 500);
	}
	
	private void freeToGo(Road r, float distance) {
		if(getSpeed() < 30) 
			accelerate(r.getSpeedLimit()/2, 500);
		else {
			float acc = (float) (Math.pow(r.getSpeedLimit(), 2) - Math.pow(getSpeed(),2));
			acc =  acc/(float)((Math.pow(distance, 0.5)));
			accelerate(acc, 500);
		}
	}
	
	public void updatePos() {
		super.updatePos();
		
		Road r = getRoad();
		Intersection i = end(r);
		float distance = Coords.distSqrd(getPos(),i.getCoords()) - 16;
		
		if(obstacle != null)
			avoidCollision(distance);
		else if(distance < 107 && cross == true && i.isOccupied()!=true)
			handleTurn(i);
		else if(getSpeed() > r.getSpeedLimit() && r.getSpeedLimit() >= 20)
			excess(r, distance);
		else if(r.getSpeedLimit() < 20 )
			slowRoad(r, distance);
		else if(getSpeed() == r.getSpeedLimit())
			limitReached();
		else if((distance < 50*50 && r.getSpeedLimit() >= 20 && r.getSpeedLimit() < 60) || (distance < 60*60 && r.getSpeedLimit() >= 60))
			nearJunction(distance);
		else if(getSpeed() < r.getSpeedLimit())
			freeToGo(r, distance);
	}

	public void onChanged(int currState){
		if(currState == TrafficSignal.GREEN_LIGHT)
			cross = true;
		else
			cross = false;
	}
	
	private TrafficSignal.SignalListener create() {
		return new TrafficSignal.SignalListener() {
			
			public void onChanged(int currState) {
				// TODO Auto-generated method stub
				if(currState == TrafficSignal.GREEN_LIGHT)
					cross = true;
				else
					cross = false;
			}
		};
	}
	
	public void carInFront(Car c) {
		obstacle = c;
	}
	
	public String toString() {
		return "ZTK21" + " " + getSpeed();
	}
}

