import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;

import org.iiitb.es103_15.traffic.*;
import org.iiitb.es103_15.traffic.TrafficSignal.SignalListener;

public class Car49 extends org.iiitb.es103_15.traffic.Car implements SignalListener{
	
	boolean glight = true;
	private static int id = 0;
    private Color colors[];
    private int carId;
    private Color carColor;
    Car frontCar;
    //private ArrayList<Car> Cars; 
    
    public Car49()
    {
        /*colors = (new Color[] {
            Color.ORANGE, Color.RED, Color.MAGENTA, Color.BLUE, Color.CYAN
        });
       
        carId = id++;
        carColor = colors[carId % 5];*/
    	
    	carColor = Color.ORANGE;
    }
    
    public String toString()
    {
        String s = "S49 ";
        /*if(frontCar != null)
        	s = s + "-->" + frontCar ;*/
        return s;
    	
    }

    
    public void paint(Graphics g)
    {
    	g.setColor(carColor);
    	super.paint(g);
    	g.setColor(RoadGrid.DEFAULT_COLOR);
    }
	
	double initialDist;
	Intersection dest;

	public void setInitialPos(Road r, Coords loc, int dir) {
		super.setInitialPos(r, loc, dir);
		newRoad(r , loc );
	}
	
	
	public void getDestination(Road r)
	{
		if(r.getDir() == getDir())
		{	
			
			dest = r.getEndIntersection();
		}
		else
		{	
			dest = r.getStartIntersection();
		}

	}
	
	public void carInFront(Car car)
	{
		frontCar = car;
		//System.out.println("Car slowing down : " + carId + "Car in front: " + frontCar.getId());

	}
	
	private double getSeparation(Car car)
	{
		Coords carpos = car.getPos();
		Coords presentpos = getPos();
		double separation = Math.abs(Math.sqrt(Coords.distSqrd(carpos, presentpos)));
		return separation;
	}
	
	public void newRoad(Road r, Coords loc) {

		getDestination(r);
		Coords dpos = dest.getCoords();
		initialDist = Math.sqrt(Coords.distSqrd(loc,dpos));
		
		TrafficControl tc = dest.getTrafficControl();
        if(tc != null && tc.getType() == 0)
        {
            int oppDir = RoadGrid.getOppDir(getDir());
            synchronized(tc)
            {
                ((TrafficSignal)tc).addListener(this, oppDir);
            }
            glight  = ((TrafficSignal)tc).getSignalState(oppDir) == 1;
        }
	}
	
	protected void updatePos() {
		super.updatePos();
		Road road = getRoad();
		double carSeparation = initialDist;
				
		Coords dpos = dest.getCoords();
		Coords cpos = getPos();
		double dist = Math.sqrt(Coords.distSqrd(dpos, cpos));

		if (frontCar != null)
		{
			carSeparation = getSeparation(frontCar);
			//System.out.println(this + "Car slowing down with a speed of " +getSpeed()+ " behind Car " + frontCar + " at a distance of " + carSeparation);
		}
		
		
		float v = road.getSpeedLimit();
		double u = getSpeed();

		double acc;
		
		if(dist >= (initialDist*1/10))
		{	if(v < 15)
				 acc = (v + 1.05 - u)/0.1;
			else
				 acc = (v*v)/(2*(initialDist*9/10));
			
			accelerate((float)acc, 20);
		}
		
		/*else if(dist > (initialDist/3) && dist < (initialDist*2/3))
		{	
				accelerate(0, 20);
		}*/
		/*if (getSpeed() > v) 
		{	if(v<15)
			{
			//double a = (u*u)/(2*(dist));
			accelerate((float)(-10), 20);
			}
			else
			{
				double a = (u*u)/(2*(dist));
				accelerate((float)(-a), 20);
			}
				
			
		}
		*/
		if(v <=40)
		{
			if(carSeparation < 5*getLength())
			{	
				double a = (u*u)/(2*(dist));
				accelerate((float)(-5*a) , 0);
			}
		}
		else
		{
			if(carSeparation < 8*getLength())
			{	
				double a = (u*u)/(2*(dist));
				accelerate((float)(-5*a) , 0);
			}
			
		}
		if ( dist < (initialDist*1/10))
		{	
			double a = (u*u)/(2*(dist));
			accelerate((float)(-a), 20);
			
			float cross_speed = 0;
			if(road.getSpeedLimit() < 15)
				cross_speed = 5;
			else
				cross_speed = v/10;
			
			if(getSpeed() < cross_speed && frontCar == null)
				{	
					Road[] roads = dest.getRoads();
					Random rg = new Random();
					Road takeroad = null;
					int roadnum = 0;
					do{
						roadnum = Math.abs(rg.nextInt()%4);
						takeroad = roads[roadnum];
					}while(takeroad==null || road.equals(takeroad));
					
					TrafficControl tc = dest.getTrafficControl();
			        int oppDir = RoadGrid.getOppDir(getDir());
			        boolean hasListener = false;
			        if(tc != null && tc.getType() == 0)
			            hasListener = true;
			        if(glight && !dest.isOccupied())
			        {
			            if(hasListener)
			                synchronized(tc)
			                {
			                    ((TrafficSignal)tc).removeListener(this, oppDir);
			                }
			            synchronized(dest)
			            {
			                crossIntersection(dest, roadnum);
			            }
			            newRoad(takeroad, getPos());
			        }
			}
			
		}
		
	}
	public void onChanged(int arg0) {
			if(arg0 == 0)
				glight = false;
			else 
				glight = true;
		
	}

	
	

}


