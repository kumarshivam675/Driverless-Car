

import java.util.ArrayList;

import org.iiitb.es103_15.traffic.*;
import org.iiitb.es103_15.traffic.TrafficSignal.*;

public class Verna extends Car implements SignalListener {
	private int redLight = 0;
	private int moving  = 1;
	private Car car_front = null;
    private static int tcId = 1;
    public int tcarId = tcId++;
	private TrafficControl tc;
	public void onChanged(int currState){
		if(currState == 0)
			redLight=0;
		else
			redLight =1;
	}
	public void setInitialPos(Road r, Coords loc, int dir) {
		super.setInitialPos(r, loc, dir);
		super.accelerate(5);
	}
	public double square(double x){
		return x*x;
	}
	private Intersection nextIntersection(){
		int x = this.getDir();
		Road r = getRoad();
		Intersection inter;
		if(x == r.getDir()){
			inter = r.getEndIntersection();
		}
		else
			inter = r.getStartIntersection();
		tc = inter.getTrafficControl();
		if(tc!=null && tc.getType()==0){
			synchronized(tc){
			((TrafficSignal)tc).addListener(this,RoadGrid.getOppDir(this.getDir()));
			redLight = ((TrafficSignal)tc).getSignalState(RoadGrid.getOppDir(this.getDir()));
			}
		}
		if(tc==null){
			redLight=-1;
		}
		return inter;
		
	}
	private double distance(){
		Intersection c1 = nextIntersection();
		Coords i1 = c1.getCoords();
		return Math.sqrt(((i1.x-getPos().x)*(i1.x-getPos().x)) + ((i1.y-getPos().y)*(i1.y-getPos().y))); 
	}
	private int distanceFromCar(Coords c1, Coords c2, int dir){
		if(dir==0)
			return c1.y -c2.y;
		else if(dir==2)
			return c2.y - c1.y;
		else if(dir==1)
			return c2.x - c1.x;
		else
			return c1.x - c2.x;
	}
	public void carInFront(Car c){
		car_front = c;
	}
	protected void updatePos() {
		super.updatePos();
		Intersection i1 = this.nextIntersection();
		float limit = this.getRoad().getSpeedLimit();
		int flag = 0;
		float car_speed = 0;
		if (limit ==10){
			limit = (float) 11;
		}
		if(nextIntersection().isOccupied()){
			System.out.println("Occupied");
		}
		ArrayList<Car> cars = this.getRoad().findCars(getDir());
		for(int i=0;i<cars.size();i++){
			int dist = distanceFromCar(this.getPos(),cars.get(i).getPos(),this.getDir());
			if(this!=cars.get(i) && dist <= 20 && dist >=0){
				flag=1;
				car_speed = cars.get(i).getSpeed();
		//		System.out.println(this.tcarId);
			}
		}
		/*if(car_front!=null){
			flag=1;
			car_speed = car_front.getSpeed();
			System.out.println(this.tcarId);
		}*/
		
		System.out.println(this.getSpeed()  + " "+ limit);
		if(distance()<15 && redLight == 0 ){
			this.accelerate(-400);
			moving = 0;
		}
		//System.out.println(i1.getCoords().x + " "+i1.getCoords().y);
		//System.out.println(this.getPos().x + " "+ this.getPos().y);
		
		if(moving ==0 && redLight==1){
			this.accelerate(10);
		}
		if(flag==1){
			this.accelerate(car_speed-this.getSpeed()*10);
			car_speed = 0;
			flag=0;
		}
		
		

		else if(moving ==1 || (moving ==0 && redLight==1)){
			if(distance()<50){
				this.accelerate((float) ((10.6-this.getSpeed())*5));
			}
			/*else if(distance()<30 && this.getSpeed()>10 && this.getSpeed()<30){
				this.accelerate(0);
			}*/
			else if(limit>this.getSpeed() && limit <=30 ){
				//System.out.println("HI1");
				this.accelerate((limit) - this.getSpeed());
			}
			else if(limit - this.getSpeed()<0.1 && limit - this.getSpeed()>0){
				//System.out.println("HI2");
				this.accelerate(0);
			}
			else if(limit<this.getSpeed()){
				//System.out.println("HI3");
				this.accelerate(limit - this.getSpeed()*10);
			}
			else if(limit - this.getSpeed()>0.1){
				//System.out.println("HI4 "+limit+" "+" "+this.getSpeed());
				this.accelerate((float) ((limit-this.getSpeed())*(3)));
			}
			if(moving ==0 && redLight == 1){
				synchronized(tc){
				((TrafficSignal)tc).removeListener(this,RoadGrid.getOppDir(getDir()));
				}
			}
			if(getSpeed()<10 && limit >10){
				this.accelerate(10);
			}
			moving =1;
		}
		//System.out.println(this.getSpeed());
		if((this.getDir()==0 && (this.getPos().y - i1.getCoords().y)<10) || moving ==0 && redLight == 1){
			Road[] roads = i1.getRoads();
			while(1==1){
				int i = (int) (Math.random()*4);
				if(roads[i]!=this.getRoad() && roads[i]!=null){
					this.crossIntersection(i1,i);
					break;
				}
			}
		}
		else if((this.getDir()==1 && (i1.getCoords().x-this.getPos().x)<10) || moving ==0 && redLight == 1){
			Road[] roads = i1.getRoads();
			while(1==1){
				int i = (int) (Math.random()*4);
				if(roads[i]!=this.getRoad()&& roads[i]!=null){
					this.crossIntersection(i1,i);
					break;
				}
			}
		}
		else if((this.getDir()==2 && (i1.getCoords().y-this.getPos().y )<10)  || moving ==0 && redLight == 1){
			
			Road[] roads = i1.getRoads();
			while(1==1){
				int i = (int) (Math.random()*4);
				if(roads[i]!=this.getRoad()&& roads[i]!=null){
					this.crossIntersection(i1,i);
					break;
				}
			}
		}
		else if((this.getDir()==3 && (this.getPos().x-i1.getCoords().x)<10 || moving ==0 && redLight == 1)){
			Road[] roads = i1.getRoads();
			while(1==1){
				int i = (int) (Math.random()*4);
				if(roads[i]!=this.getRoad()&& roads[i]!=null){
					this.crossIntersection(i1,i);
					break;
				}
			}
		}
		
	}
}