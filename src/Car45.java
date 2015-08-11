import java.util.ArrayList;
import java.util.Random;

import org.iiitb.es103_15.traffic.*;
import org.iiitb.es103_15.traffic.TrafficSignal.SignalListener;

public class Car45 extends Car
{
	public Car45()
	{
		super();
	}
		
	int signal = 0, flag = 1 , flagg = 1;
	
	Random rn =  new Random();
	
	private class listenerclass implements SignalListener
	{
		public void onChanged(int currState) 
		{
			signal  = currState;
		}
	}
	TrafficSignal sign;
		
	double startdist = 0 , enddist = 0 , distInI = 0;
	Intersection newStartI, newEndI , aheadI;
	Road currRoad , nextRoad;
	listenerclass lc = new listenerclass();
	
	int flagcarAhead = 0 , carGone = 0;;
	
	
	
	public void updatePos()
	{
		super.updatePos();
		currRoad = this.getRoad();
		nextRoad = currRoad;
		//R = (RoadSafety)currRoad;
		int a = 0;

		if(this.getDir() == currRoad.getDir())
		{	
			newStartI = currRoad.getStartIntersection();
			newEndI = currRoad.getEndIntersection();
		}
		else
		{
			newEndI = currRoad.getStartIntersection();
			newStartI = currRoad.getEndIntersection();
			
		}
		
		aheadI = newEndI;
		
		startdist = Math.sqrt(Coords.distSqrd( this.getPos(),newStartI.getCoords() ));
		enddist = Math.sqrt(Coords.distSqrd( this.getPos(),newEndI.getCoords()) );
		distInI =  Math.sqrt(Coords.distSqrd(newStartI.getCoords(), newEndI.getCoords()));
		
		
		a = (int)(Math.pow(currRoad.getSpeedLimit(),2) / (2*(distInI/4)));
		
		if(flagcarAhead == 1)
		{
			if(enddist >= 5 && this.getSpeed() <= currRoad.getSpeedLimit())
			{
				accelerate(currRoad.getSpeedLimit()-this.getSpeed() + 1,0);
				//System.out.println(this + "  " + this.getSpeed() + " " + currRoad.getSpeedLimit());
				//a = (int)(Math.pow(10,2) / (2*enddist));
				/*if(this.getSpeed() <= currRoad.getSpeedLimit())
				{
					accelerate(10,0);
					
				}
				else
					accelerate(-14,1);
				*/
				//System.out.println(this + "if ");
			
			//else
				//carGone = 0;
			}
			else
				accelerate(-1000);
		}
		else
		{
			if( currRoad.getSpeedLimit() == 10 )
				{	
					if((this.getSpeed() + 2 + a) <  currRoad.getSpeedLimit())
						accelerate(42,0);
					else
						accelerate(-14,1);
					//System.out.println(this + "else if ");
				}	
				
			else
			{
				if(startdist < distInI/4 && ((this.getSpeed() + a) <  currRoad.getSpeedLimit())) // the limit can be reached even before as i am accelerating everytime . So accelerate based on currspeed
					accelerate(a,1);
			
				else if(startdist >= (distInI/2))
				{
					accelerate((float)(-getSpeed()*getSpeed()/(2*enddist))+1,1);
					//System.out.println("decelerating");
				}
				else
					accelerate(0,1);
				//System.out.println(this + "else ");
			}
		}	
		sign  = (TrafficSignal)newEndI.getTrafficControl();
		
		if(sign == null)
		{
			if( enddist < 25 )
				TurnAtI();
		}
	
		else if(sign != null) 
		{
			if(flag == 1)
			{
				synchronized(sign)
				{
					sign.addListener(lc, RoadGrid.getOppDir(this.getDir()));
					signal = sign.getSignalState(RoadGrid.getOppDir(this.getDir()));
					flag = 0;
				}
				
			}
			if( enddist < 25 && signal == 1)
			{
				synchronized(sign)
				{
					sign.removeListener(lc, RoadGrid.getOppDir(this.getDir()));
					TurnAtI();
				}
				
			}
		}
		
	}
	
	public void carInFront(Car c)
	{
		
		
		if(c != null )
		{
			
			double cardist = Math.sqrt( Coords.distSqrd( this.getPos(),c.getPos()) );
			
			float diffspeed = (this.getSpeed()-c.getSpeed());
			
			if(cardist <= 40 && diffspeed >= 0)
			{
				this.accelerate(-2000);
				flagcarAhead = 1;
				carGone = 0;
				//this.accelerate(10);
				
			}
			
		}
		else
		{
			flagcarAhead = 0;
			carGone = 1;
		}
		
	}
	
	public void newacc()
	{
		if(enddist >= 5)
		{
			accelerate(currRoad.getSpeedLimit()-this.getSpeed() + 1,0);
			System.out.println(this + "  " + this.getSpeed() + " " + currRoad.getSpeedLimit());
		}
	}
	

	public void TurnAtI()
	{
		currRoad = getRoad();
		Road[] roadsonNewI = aheadI.getRoads();
		int randomNum = 0;
		while(true)
		{
			randomNum = rn.nextInt((3 - 0) + 1) + 0;
			if((roadsonNewI[randomNum] != currRoad) && (roadsonNewI[randomNum] != null))
			{
				nextRoad = roadsonNewI[randomNum];
				break;
			}
		}
		/*for(int i = 0; i < roadsonNewI.length ; i++)
		{
			
		    if((roadsonNewI[i] != currRoad) && (roadsonNewI[i] != null))
			{
				nextRoad = roadsonNewI[i];
				break;
			}
		}
		*/
		if(aheadI == nextRoad.getStartIntersection())
			crossIntersection ( aheadI, nextRoad.getDir() );
		else
			crossIntersection ( aheadI, RoadGrid.getOppDir(nextRoad.getDir()) );
		flag = 1;

		
		if(nextRoad.getSpeedLimit() == 10 || this.getSpeed() == 0)
			accelerate(-12,1);
	}
}