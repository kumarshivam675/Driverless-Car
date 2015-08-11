import java.util.Random;

import org.iiitb.es103_15.traffic.*;
import org.iiitb.es103_15.traffic.TrafficSignal.SignalListener;
//import org.iiitb.es103_15.traffic.Intersection;

public class Car56 extends Car implements SignalListener {

	Intersection nextInter;
	Random rand = new Random();
	int signal = 0;
	int flag = 0;
	int turned = 0;
	int flagcarAhead=0;
	TrafficSignal trafficC = null;
	
	public Car56()
	{
		super();
		nextInter = null;
	}
	
	public String toString() {
		return "Car56 ";
				
	}
	
	
	public void onChanged(int currSignal) {
		signal = currSignal;
	}


	@Override
    public void updatePos() {
        super.updatePos(); //To change body of generated methods, choose Tools | Templates.
        
        int dir = Math.abs(rand.nextInt())%4;
        //turn(dir);
        //Road curr_road = this.getRoad();
        
        nextInter = this.findNextIntersection();
        //System.out.println(this.getSpeed());
                
        if(this.getRoad().getSpeedLimit() == 10 && (this.getSpeed() < this.getRoad().getSpeedLimit()))
        	accelerate(2,1);
        
        else if(this.getSpeed() < this.getRoad().getSpeedLimit())
    		accelerate(5f, 200);
        
        else if(((this.getSpeed() - this.getRoad().getSpeedLimit()) > 1) && this.getSpeed() != 0)
        {
        	accelerate(-(this.getSpeed() - this.getRoad().getSpeedLimit()), 2000);
        }
        
        if(distToNextInter() <= 60 && this.getRoad().getSpeedLimit() != 10)
    	{	
    		float a = (float) ((Math.pow(this.getSpeed(),2)) / (2*distToNextInter()));
    		accelerate(-a,10);
    	}
        
        
        if(this.getDir() == this.getRoad().getDir())
        {
        	trafficC = (TrafficSignal)this.getRoad().getEndIntersection().getTrafficControl();
        	
        	if(trafficC == null)
        	{
        		dir = Math.abs(rand.nextInt())%4;
                if(distToNextInter() < 25)
	        		turn(dir);
    		}
        	
        	else if(trafficC != null )
	     	{
        		//System.out.println("traffic signal approaching");
        		if(flag == 0)
        		{
	        		synchronized (trafficC)
	        		{
						trafficC.addListener(this, RoadGrid.getOppDir(this.getDir()));
						signal = ((TrafficSignal)trafficC).getSignalState(RoadGrid.getOppDir(this.getDir()));
						flag = 1;
						//System.out.println("1 if");
					}
        		}
        	
        		/*if(signal == 0 && distToNextInter() <= 20)
        		{
	        		accelerate(-1000,10);
	        		System.out.println("3 if");
	        	}*/
        	
	        	if(signal == 1 && distToNextInter() < 20)
	        	{
	        		synchronized (trafficC)
	        		{
	        			
						trafficC.removeListener(this, RoadGrid.getOppDir(this.getDir()));
						//signal = 0;
						flag = 0;
						//trafficC = null;
						//System.out.println("Removing listener same ");
						
						//accelerate(10,1);
						dir = Math.abs(rand.nextInt())%4;
						turn(dir);
	        		}
	        	}
        	}
        }
        
        else if(this.getDir() != this.getRoad().getDir() && distToNextInter() <= 50)
        {
        	trafficC = (TrafficSignal) this.getRoad().getStartIntersection().getTrafficControl();
        	
        	if(trafficC == null){
                dir = Math.abs(rand.nextInt())%4;
        		turn(dir);
        		//System.out.println("Null tc oppo");
    		}
        	
        	else if(trafficC != null)
        	{
        		if(flag == 0)
        		{
	        		synchronized (trafficC) 
	        		{
						trafficC.addListener(this, RoadGrid.getOppDir(this.getDir()));
						signal = ((TrafficSignal)trafficC).getSignalState(RoadGrid.getOppDir(this.getDir()));
						flag = 1;
					}
        		}
        	
        		else if(signal == 1 && distToNextInter() < 20)
	        	{
	        		synchronized (trafficC)
	        		{
	        			trafficC.removeListener(this, RoadGrid.getOppDir(this.getDir()));
	        			flag = 0;
						accelerate(10, 1);
						dir = Math.abs(rand.nextInt())%4;
						turn(dir);
	        		}
	        	}
	        }
        }
	}    
    
    protected void startDrive() {
	} 
    
    protected void turn(int dir){
    	super.updatePos();
    	Road curr_road = this.getRoad();
        //turned = 0;
        if(this.getDir() == this.getRoad().getDir()){
        	if(Math.abs(this.getPos().y - this.getRoad().getEndIntersection().getCoords().y) <= 15  && Math.abs(this.getPos().x - this.getRoad().getEndIntersection().getCoords().x) <= 15)
            {
        		dir = Math.abs(rand.nextInt())%4;
            	if(this.getRoad().getEndIntersection().getRoads()[dir]!=null && this.getRoad().getEndIntersection().getRoads()[dir]!= curr_road){
            		accelerate(-1000, 200);
            		this.crossIntersection(this.getRoad().getEndIntersection(), dir);
            		//turned = 1;
            		//System.out.println("turned == "+turned);
            	}
            }
        }
        
        
        else {
        	if(Math.abs(this.getPos().y - this.getRoad().getStartIntersection().getCoords().y) <= 15  && Math.abs(this.getPos().x - this.getRoad().getStartIntersection().getCoords().x) <= 15)
            {
        		dir = Math.abs(rand.nextInt())%4;
            	if(this.getRoad().getStartIntersection().getRoads()[dir] != null && this.getRoad().getStartIntersection().getRoads()[dir]!= curr_road){
            		accelerate(-1000, 200);
            		this.crossIntersection(this.getRoad().getStartIntersection(), dir);
            		//turned = 1;
            		//System.out.println("turned == "+turned);
            	}
            }
        }
        
    }
    
    protected void moveRight() {
    }
    
    protected void moveLeft() {
    }

    public Intersection findNextIntersection(){
    	Road r = getRoad();
    	if(getDir() == r.getDir())
    	{
    		nextInter = r.getEndIntersection();
    	}
    	
    	else
    		nextInter = r.getStartIntersection();
    	return nextInter;
    }
    
    public int distToNextInter() {
        Coords pos = getPos();
        Coords pos_inter = findNextIntersection().getCoords();
        int dist = 0;
        if(getDir() == 0){
            dist = pos.y - pos_inter.y;
        }
        else if(getDir() == 1){
            dist = pos_inter.x - pos.x;
        }
        else if(getDir() == 2){
            dist = pos_inter.y - pos.y;
        }
        else if(getDir() == 3){
            dist = pos.x - pos_inter.x;
        }
        return dist;
    }
    
    
    public void carInFront(Car c)
	{
		if(c != null)
		{
			flagcarAhead = 1;
			double cardist = Math.sqrt( Coords.distSqrd( this.getPos(),c.getPos()) );
			float diffspeed = (this.getSpeed()-c.getSpeed());
			
			if(cardist <= 40 && diffspeed >= 0)
				this.accelerate(-2000);
			
		}

		else{
			flagcarAhead = 0;
			accelerate(10,1);
		}
			
		
	}

}

