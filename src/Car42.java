import java.util.ArrayList;

import org.iiitb.es103_15.traffic.*;
import org.iiitb.es103_15.traffic.TrafficSignal.SignalListener;


public class Car42 extends Car implements SignalListener {
	int signal = 1;
	int status = 1;
	int redlight = 1;
	int flag = 0;
	TrafficControl tc = null;
	int listnercontrol = 0;
	Intersection nextIntersection;
	private Car carAhead;
	
	public Car42() {
		carAhead = null;
		nextIntersection = null;	

	}
	
	public String toString() {
		return "Car42 ";
				
	}
	
	public void onChanged(int currState){
		if(currState == 0){
			redlight = 0;
		}
		else{
			redlight = 1;
		}
	}

	public void setInitialPos(Road r, Coords loc, int dir) {
		super.setInitialPos(r,loc,dir);
		accelerate((float) 100.0,1);
	}
	
	public void accelerate(){
		super.accelerate((float) 100.0,1000);
	}
	
	public void carInFront(Car obstacle){
		carAhead = obstacle;
	}
	
	private Intersection findNextIntersection(){
		Road r = getRoad();
		Intersection nextInter;
		if(getDir() == r.getDir()){
			nextInter = r.getEndIntersection();
		}
		else{
			nextInter = r.getStartIntersection();
		}
		
		tc = nextInter.getTrafficControl();
        if(tc != null && tc.getType() == 0) {
            int oppDir = RoadGrid.getOppDir(getDir());
            synchronized(nextInter){
            	((TrafficSignal)tc).addListener(this, oppDir);
                redlight = ((TrafficSignal)tc).getSignalState(oppDir);
                listnercontrol = 1;
            }
            
        }
        
		nextIntersection = nextInter;
		return nextInter;
	}
	
	private int distToNextInter() {
        Coords pos = getPos();
        Coords pos_inter = nextIntersection.getCoords();
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
	
	
	private int distToCarAhead(Car obstacle){
		Coords pos = getPos();
		Coords pos_carAhead = obstacle.getPos();
		int dist = 0;
        if(getDir() == 0){
            dist = pos.y - pos_carAhead.y;
        }
        else if(getDir() == 1){
            dist = pos_carAhead.x - pos.x;
        }
        else if(getDir() == 2){
            dist = pos_carAhead.y - pos.y;
        }
        else if(getDir() == 3){
            dist = pos.x - pos_carAhead.x;
        }
        return dist;
	}
	
	private int turnDir(){
		int rand;
		while(1 == 1){
			rand = (int) (Math.random()*4);
			if(nextIntersection.getRoads()[rand] != null && nextIntersection.getRoads()[rand] != getRoad()){
				return rand;
			}
		}
	}
	
	// called at regular intervals. Do any processing here
	protected void updatePos() {
		super.updatePos();
		if(flag == 0){
			findNextIntersection();
			flag = 1;
		}
		
		if(carAhead == null){
			status = 1;
		}
		
		if(carAhead != null){
			int distance = distToCarAhead(carAhead);
			if(distance < 20 && distance > 0){
				accelerate(-100,100);
			}
			else if(distance < 30 && distance > 0){
				float diff = carAhead.getSpeed() - getSpeed();
				if(diff > 0){
					diff = diff * (-1);
				}
				accelerate(diff*10,100);
				status = 0;
			}
			else
				status = 1;
		}
        		
        if(getSpeed() == 0 && signal == 0 && redlight == 1){
        	accelerate(10, 100);
        	signal = 1;
        }
		
        if( tc!= null && distToNextInter() < 15 && redlight == 0){
        	signal = 0;
        	accelerate(-1000,100);
        }
		
		if(distToNextInter() < 30 && signal == 1){
			if(getSpeed() < 11 ){
				super.accelerate(10, 10);
			}
			else{
				super.accelerate((float) ((10.6 - getSpeed())*10), 1000);
			}
			status = 0;
		}
		
		if(Math.abs(getRoad().getSpeedLimit() - getSpeed()) < 3 && status == 1 && signal == 1){
			if(getRoad().getSpeedLimit() - getSpeed() == 0){
				accelerate(1, 10);
			}
			else{
				accelerate((getRoad().getSpeedLimit() - getSpeed())*10, 10);
			}
		}
		else if(getRoad().getSpeedLimit() > getSpeed() && status == 1 && signal == 1){
			accelerate((getRoad().getSpeedLimit() - getSpeed())*10, 100);
		}
		else if(getRoad().getSpeedLimit() < getSpeed() && status == 1 && signal == 1){
			accelerate((getRoad().getSpeedLimit() - getSpeed())*10, 100);
		}
		
		if( distToNextInter() < 10 && signal == 1){
            int oppDir = RoadGrid.getOppDir(getDir());
            if(!nextIntersection.isOccupied()){
            	synchronized(nextIntersection){
        			super.crossIntersection(nextIntersection, turnDir());
            	}
            }
        	 if(tc != null && tc.getType() == 0) {
        		 synchronized(nextIntersection){
        			 ((TrafficSignal)tc).removeListener(this, oppDir);
                     listnercontrol = 1;
        		 }
                 
             }
        	 if(getSpeed() > getRoad().getSpeedLimit()){
        		 accelerate((getRoad().getSpeedLimit() - getSpeed())*10,100);
        	 }
        		
			findNextIntersection();
			status = 1;
		}
		
		if(getSpeed() > getRoad().getSpeedLimit()){
   		 accelerate((getRoad().getSpeedLimit() - getSpeed())*10);
   	 }
	}
	
	
	public void setPos(Road r, Coords loc, int dir){
		super.setInitialPos(r,loc,dir);
	}

}
