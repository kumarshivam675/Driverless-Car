import org.iiitb.es103_15.traffic.*;
import java.util.*;
import java.lang.*;

public class Perseus extends Car implements TrafficSignal.SignalListener
{
	
	int curr_dir,status=1;
	Intersection next_inter = null;
	Car next_car = null;
	float acc = 8;
	float threshold = 20;
	boolean stop = false;
	TrafficControl nextTc = null;
	Road R = getRoad();

	public String toString() 
	{
		return super.toString() + "-35";
	}

	private Intersection NewIntersection()
	{
		Road R = getRoad();
		if(R.getDir()==getDir())
			next_inter = R.getEndIntersection();
		else
			next_inter = R.getStartIntersection();
		nextTc = next_inter.getTrafficControl();
		if (nextTc != null)
			{
				synchronized(next_inter)
				{
				((TrafficSignal)nextTc).addListener(this, RoadGrid.getOppDir(getDir()));
				}
				stop = ((TrafficSignal)nextTc).getSignalState(RoadGrid.getOppDir((int)getDir())) == 0;
			}	
		if(nextTc==null)
			stop=false;	
		return next_inter; 
	}
		
	private int getNextCarDist(Car c)
	{
		Coords x2 = c.getPos();
	        Coords x1 = getPos();
        	return Math.abs(x2.x - x1.x + x2.y - x1.y) - c.getLength();
	}	

	public void carInFront(Car c) 
	{
        	next_car = c;
	}	

	public void onChanged(int state)
	{
		if (state == 0)
			stop = true;
		else
			stop = false;
	}
	
	public void drive()
	{ 
		super.drive();
		accelerate((float) 100.0,1);		
	}
	
	public void setInitialPos(Road r, Coords loc, int dir)
	{
		super.setInitialPos(r,loc,dir);
		next_inter = NewIntersection();
	}	
	

	private int nextDir()
	{
		Random rand = new Random();
		Road[] road_list = next_inter.getRoads();
		Road[] road_check = new Road[4];
		int[] dir = new int[4];
		int j=0;
		R=getRoad();
		for(Road r:road_list)
		{
			if(r==R || r==null)
				continue;
			if(next_inter==r.getStartIntersection() &&  r.entryAllowed(r.getDir())==true)
				{
					dir[j]=r.getDir();
					road_check[j] = r;
					j++;
				}
			else if(r.entryAllowed(r.getDir())==true)
				{
					dir[j]=(r.getDir() + 2)%4;
					road_check[j] = r;
					j++;
				}
		}

		int lucky_index=rand.nextInt(j);
		int lucky;
		while(true)
		{
			ArrayList<Car>cars = road_check[lucky_index].findCars(dir[lucky_index]);
			if(cars.size()==0)
				return dir[lucky_index];
			Car another_car = (Car)cars.get(cars.size()-1);	
			Coords pos = another_car.getPos();
			Coords curr_pos = next_inter.getCoords();
			if(Math.abs((pos.x + pos.y) - (curr_pos.x + curr_pos.y))>2*another_car.getLength()+5) 
				return dir[lucky_index];
		
			lucky = rand.nextInt(j);
			if(rand.nextInt(j)!=lucky_index)
				lucky_index = lucky;
		}	
	}

	private int distInter() 
	{
	        Coords pos = getPos();
	        Coords pos_inter = NewIntersection().getCoords();
		curr_dir=getDir();
	    	return Math.abs(pos_inter.x - pos.x) + Math.abs(pos.y - pos_inter.y);
    	}

	protected void updatePos() 
	{
		super.updatePos();

		float speed = getSpeed();

		float dist = ((speed*speed))/(2*acc);
	
		float speedLimit = getRoad().getSpeedLimit();
		
		int dist_impact=distInter();

		if(next_car!=null)
			dist_impact=Math.min(distInter(),getNextCarDist(next_car));
		if(dist_impact <= threshold)
		{
			if(stop==true)
				accelerate(-getSpeed()/0.1f);
			else
			{
				if(distInter()!=dist_impact)
				{
					if(next_car.getSpeed() < getSpeed())
					{
						accelerate((next_car.getSpeed() - getSpeed())/0.1f);
					}				
				}
				else if(next_inter.isOccupied()==false)
				{
					accelerate(-100.0f);
					int direction = nextDir();
					if(stop==false)
					{
						if(getSpeed()>10.5)
						{
							//System.out.println(getSpeed());
							accelerate(-(getSpeed()-10.5f)/0.1f);
						}

						if (nextTc != null) 
							((TrafficSignal)nextTc).removeListener(this, RoadGrid.getOppDir(getDir()));
					
						synchronized(next_inter)
						{
						
							crossIntersection(next_inter, direction);
							
							if(getSpeed()==0)
							{
								//float new_speed = getRoad().getSpeedLimit() * 0.7f;  
								accelerate(100.0f);
							}	
						}
					next_inter = NewIntersection();
					}
				}
			}
		}
		
		else if(dist >= dist_impact)
		{
			float u = getSpeed();
			if(getSpeed() > 0 )
			{
				float a = (-(u*u))/(2*dist_impact);
				accelerate(a);
			}
		}
		else if(speedLimit + 0.5f >= getSpeed())
		{
			accelerate((speedLimit + 0.5f -getSpeed())/(0.5f));
		}
		else if(speedLimit + 0.5f < getSpeed())
		{
			accelerate((speedLimit + 0.5f -getSpeed())/(0.1f));
		}
		//if(getRoad().getSpeedLimit()<=15)
		//	System.out.println(this + " " + getSpeed());
	}	
}
