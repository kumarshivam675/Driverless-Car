import org.iiitb.es103_15.traffic.*;
import org.iiitb.es103_15.traffic.TrafficSignal.SignalListener;
import java.util.Random;
import java.awt.Color;
import java.awt.Graphics;

public class Car28 extends Car implements SignalListener
{
	float max_brake_acceleration = 10, max_gas_acceleration = 15, turn_speed = 9, threshold = 12, speed_threshold = (float)0.5, min_distance_between_cars = 10;
	float prv_distance_ahead = 0.0F, prv_distance_behind = 0.0F;
	boolean stop = false;
	Intersection nextInter = null;
	TrafficControl nextTc = null;
	Car car_ahead = null, car_behind = null;
	
	public Car28() 
	{
	}
	
	public void setInitialPos(Road r, Coords loc, int dir) 
	{
		super.setInitialPos(r,loc,dir);
		findNextIntersection();
		if (nextTc != null)
		{		
			synchronized(this)
			{
				((TrafficSignal)nextTc).addListener(this, RoadGrid.getOppDir(getDir()));
			}
			if(((TrafficSignal)nextTc).getSignalState(RoadGrid.getOppDir(getDir())) == 0)
				stop = true;
			else
				stop = false;
		}
	}

	
	public String toString()
    {
		return ("28-"+super.toString()).toString();
    }
	
	
	public void onChanged(int state)
	{
		if (state == 0)
			stop = true;
		else
			stop = false;
	} 
	
	public float getMinDistanceBetweenCars()
	{
		return min_distance_between_cars;
	}
	
	public float DistToCar(Car temp)
	{
        if(temp == null)
        	return 10000000;
        int dist = 0;
        dist = (temp.getPos().x - getPos().x) + (temp.getPos().y - getPos().y);
        if(dist < 0)
            dist *= -1;
        return dist - temp.getLength();
	}
	
	public void carInFront(Car c2)
    {
        car_ahead = c2;
    }
	
	public void accelerate()
	{
		super.accelerate((float) 100.0,1000);
	}
	
	public void turn(int dir)
	{
		if (nextTc != null)
		{
			synchronized(this)
			{
				((TrafficSignal)nextTc).removeListener(this, RoadGrid.getOppDir(getDir()));
			}
		}
		synchronized(nextInter)
		{
			crossIntersection(nextInter, dir);
			findNextIntersection();
		}
		if (nextTc != null)
		{		
			synchronized(this)
			{
				((TrafficSignal)nextTc).addListener(this, RoadGrid.getOppDir(getDir()));
			}
			if(((TrafficSignal)nextTc).getSignalState(RoadGrid.getOppDir(getDir())) == 0)
				stop = true;
			else
				stop = false;
		}
	}
	
	private Intersection findNextIntersection()
	{
		Road r = getRoad();
		if(getDir() == r.getDir())
			nextInter = r.getEndIntersection();
		else
			nextInter = r.getStartIntersection();
		nextTc = nextInter.getTrafficControl();
		return nextInter;
	}
	 
	private int distToNextInter() 
	{
        Coords pos = getPos(), inter_pos = findNextIntersection().getCoords();
        int dist = 0;
        if(getDir() == 0)
            dist = pos.y - inter_pos.y;
        else if(getDir() == 1)
            dist = inter_pos.x - pos.x;
        else if(getDir() == 2)
            dist = inter_pos.y - pos.y;
        else if(getDir() == 3)
            dist = pos.x - inter_pos.x;
        return dist;
	}
	 
	private int directionToTurn()
	{
		 Random randomGenerator = new Random();
		 int random_int = randomGenerator.nextInt(10);
		 int[] priority = new int[]{random_int%3, (random_int+1)%3, (random_int+2)%3};
		 int[][] turns_possible = new int[][]{{1,3,0},{0,2,1},{1,3,2},{0,2,3}};
		 for(int i = 0; i < 3; i++)
			 if(findNextIntersection().getRoads()[turns_possible[getDir()][priority[i]]] != null)
				return turns_possible[getDir()][priority[i]];
		 return -1;
	}
	 
	// called at regular intervals. Do any processing here
	protected void updatePos() 
	{
		super.updatePos();
		float distance_to_decelerate = ((getSpeed()*getSpeed())-(turn_speed*turn_speed))/(2*max_brake_acceleration);
		float maximum_speed_reachable = Math.max(9.5F, getRoad().getSpeedLimit()-speed_threshold);
		float safe_distance = distToNextInter();
		if(car_ahead == null && stop == false && getSpeed() < 9.5F)
			accelerate((9.5F-getSpeed())/((float)1), 1);
		if(car_ahead != null)
			safe_distance = Math.max(1.5F*min_distance_between_cars, (getSpeed()*getSpeed() - (car_ahead.getSpeed()*car_ahead.getSpeed()))/(2*max_brake_acceleration));
		if(distToNextInter() <= threshold)
		{
			accelerate(-max_brake_acceleration*3, 1); //To see the signal properly
			if(stop)
				accelerate(-(float)(1000.0), 1);
			else
			{
				int dir = directionToTurn();
				turn(dir);
			}
		}
		else if(car_ahead != null && safe_distance > DistToCar(car_ahead))
		{
			 if(DistToCar(car_ahead) < min_distance_between_cars)
				 accelerate(-(float)(1000.0), 1);
			else
				accelerate(-max_brake_acceleration, 1);
		}
		else if(distToNextInter() <= distance_to_decelerate+(threshold/1.3F))
		{
			if(getSpeed() > turn_speed)
				accelerate(-max_brake_acceleration, 1);
			else
				accelerate(max_gas_acceleration, 1);
		}
		else
		{
			if(getSpeed() < 10F)
				accelerate(max_gas_acceleration, 1);
			else if(maximum_speed_reachable < getSpeed())
				accelerate((10F-(2*speed_threshold)-getSpeed())/((float)1), 2);
			else if(maximum_speed_reachable >= getSpeed())
				accelerate(Math.min((maximum_speed_reachable-(2*speed_threshold)-getSpeed())/((float)3), max_gas_acceleration), 1);
		}
		if(car_ahead != null)
			prv_distance_ahead = DistToCar(car_ahead);
	}

	public void drive() 
	{
		super.drive();
	}
	 
	public void setPos(Road r, Coords loc, int dir)
	{
		super.setInitialPos(r,loc,dir);
	}
	
	public void paint(Graphics gr)
    {
		Random rand = new Random();
		/*float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();
		Color carColor = new Color(r, g, b);*/
		float r = rand.nextInt()%2;
		Color carColor;
		if(r == 0)
			carColor = new Color(200, 0, 0);
		else
			carColor = new Color(0, 0, 200);
		
		gr.setColor(carColor);//.darker().darker());
        super.paint(gr);
        gr.setColor(RoadGrid.DEFAULT_COLOR);
    }
}
