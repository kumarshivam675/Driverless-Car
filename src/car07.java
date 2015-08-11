

//import java.util.ArrayList;

import org.iiitb.es103_15.traffic.Car;
import org.iiitb.es103_15.traffic.Coords;
import org.iiitb.es103_15.traffic.RoadGrid;
import org.iiitb.es103_15.traffic.TrafficControl;
import org.iiitb.es103_15.traffic.TrafficSignal;
import org.iiitb.es103_15.traffic.TrafficSignal.SignalListener;

public class car07 extends Car implements SignalListener{
	private int red = 0;//redlight
	private int flag = 1;//moving car
	private TrafficControl tc;//traffic control
	private Car cf = null;//car front
	private int nc = 0;//if car is there or not
	//private int b_crossed = 0;//before car crossed or not
	public void onChanged(int pstate){
		if(pstate == 0){
			red = 0;
		}
		else{
			red = 1;
		}
	}
	private int calcd(Coords c_1,Coords c_2){
		return Math.abs(c_1.x - c_2.x) + Math.abs(c_1.y-c_2.y);
	}
	public void carInFront(Car car){
		cf = car;
	}
	public void updatePos(){
		super.updatePos();
		if(this.getSpeed() > this.getRoad().getSpeedLimit()){
			this.accelerate(-16);
		}
		else if(this.getRoad().getSpeedLimit() - this.getSpeed() > 1){
			this.accelerate(3);
		}
		//ArrayList<Car> cars = this.getRoad().findCars(getDir());
		if(cf == null){
			
		}
		if (cf != null){
			nc = 1;
			//System.out.println("gotcha11");
			if(calcd(this.getPos(),cf.getPos()) < 50){
				/*System.out.println("diff:" + calcd(this.getPos(),cf.getPos()));
				System.out.println("before speed" + cf.getSpeed());
				System.out.println("now speed" + this.getSpeed());*/	
				//System.out.println((float) ((10.5*10.5) - (this.getSpeed()*this.getSpeed())/40000));
				this.accelerate((cf.getSpeed() - this.getSpeed())*10);
			}
		}
		int dist1 = (Math.abs(this.getPos().x-this.getRoad().getStartIntersection().getCoords().x)+Math.abs(this.getPos().y-this.getRoad().getStartIntersection().getCoords().y));
		int dist2 = (Math.abs(this.getPos().x-this.getRoad().getEndIntersection().getCoords().x)+Math.abs(this.getPos().y-this.getRoad().getEndIntersection().getCoords().y));
		
		if(this.getDir() != this.getRoad().getDir() && dist1 < 60) 
		{
			//System.out.println("distance" + dist1);
			this.accelerate(-15);
			//System.out.println("notsame");
			tc = this.getRoad().getStartIntersection().getTrafficControl();
			if(tc!=null && tc.getType()==0){
				synchronized(tc){
				((TrafficSignal)tc).addListener(this,RoadGrid.getOppDir(this.getDir()));
				//red = ((TrafficSignal)tc).getSignalState(RoadGrid.getOppDir(this.getDir()));
				}
			}
			if(tc==null){
				red = -1;
			}
			if(red == 0 && dist1 < 40){
				//System.out.println(red);
				this.accelerate(-10000);
				//System.out.println("stopped");
				flag = 0;
			}
			if(red == 1 && flag==0){
				this.accelerate(10);
				synchronized(tc){
				((TrafficSignal)tc).removeListener(this,RoadGrid.getOppDir(getDir()));
				}
			}
			if(((this.getRoad().getStartIntersection().getRoads()[0] != null && this.getDir() != 2) && (dist1 < 20))  || (flag == 0 && red == 1)){
				if(cf == null || cf.getPos() != this.getRoad().getStartIntersection().getCoords())
					this.crossIntersection(this.getRoad().getStartIntersection(), 0);
				//System.out.println(dist1);
				//System.out.println("turned1");
			}
			else if((this.getRoad().getStartIntersection().getRoads()[1] != null && this.getDir() != 3 && (dist1 < 20)) || (flag == 0 && red == 1)){
				if(cf == null || cf.getPos() != this.getRoad().getStartIntersection().getCoords())
					this.crossIntersection(this.getRoad().getStartIntersection(), 1);
				//System.out.println(dist1);
				//System.out.println("turned2");
			}
			else if((this.getRoad().getStartIntersection().getRoads()[2] != null && this.getDir() != 0 && (dist1 < 20) ) || (flag == 0 && red == 1)){
				if(cf == null || cf.getPos() != this.getRoad().getStartIntersection().getCoords())
					this.crossIntersection(this.getRoad().getStartIntersection(), 2);
				//System.out.println(dist1);
				//System.out.println("turned3");
			}
			else if((this.getRoad().getStartIntersection().getRoads()[3] != null && this.getDir() != 1 && (dist1 < 20)) || (flag == 0 && red == 1)){
				if(cf == null || cf.getPos() != this.getRoad().getStartIntersection().getCoords())
					this.crossIntersection(this.getRoad().getStartIntersection(), 3);
				//System.out.println(dist1);
				//System.out.println("turned4");
			}
		}
		else if(this.getDir() == this.getRoad().getDir() && dist2 < 60)
		{
			//System.out.println("distance:" + dist2);
			this.accelerate(-15);
			//System.out.println("same");
			tc = this.getRoad().getEndIntersection().getTrafficControl();
			if(tc!=null && tc.getType()==0){
				synchronized(tc){
				((TrafficSignal)tc).addListener(this,RoadGrid.getOppDir(this.getDir()));
				red = ((TrafficSignal)tc).getSignalState(RoadGrid.getOppDir(this.getDir()));
				}
			}
			if(tc==null){
				red = -1;
			}
			if(red == 0 && dist2 < 40){
				//System.out.println(red);
				this.accelerate(-10000);
				//System.out.println("stopped");
				flag = 0;
			}
			if(red == 1 && flag==0){
				this.accelerate(10);
				synchronized(tc){
				((TrafficSignal)tc).removeListener(this,RoadGrid.getOppDir(getDir()));
				}
			}

			if(((this.getRoad().getEndIntersection().getRoads()[0] != null && this.getDir() != 2 ) && (dist2 < 20)) || ((flag == 0 && red == 1))){
				if(cf == null || cf.getPos() != this.getRoad().getEndIntersection().getCoords())
					this.crossIntersection(this.getRoad().getEndIntersection(), 0); 
				//System.out.println(dist2);
			    //System.out.println("turned1");
			}
			else if((this.getRoad().getEndIntersection().getRoads()[1] != null && this.getDir() != 3 && (dist2 < 20)) || (flag == 0 && red == 1)){
				if(cf == null || cf.getPos() != this.getRoad().getEndIntersection().getCoords())
					this.crossIntersection(this.getRoad().getEndIntersection(), 1);
				//System.out.println(dist2);
				//System.out.println("turned2");
			}
			else if((this.getRoad().getEndIntersection().getRoads()[2] != null && this.getDir() != 0 && (dist2 < 20)) || (flag == 0 && red == 1)){
				if(cf == null || cf.getPos() != this.getRoad().getEndIntersection().getCoords())
					this.crossIntersection(this.getRoad().getEndIntersection(), 2);
				//System.out.println(dist2);
				//System.out.println("turned3");
			}
			else if((this.getRoad().getEndIntersection().getRoads()[3] != null && this.getDir() != 1 && (dist2 < 20)) ||(flag == 0 && red == 1)){
				if(cf == null || cf.getPos() != this.getRoad().getEndIntersection().getCoords())
					this.crossIntersection(this.getRoad().getEndIntersection(), 3);
				//System.out.println(dist2);
				//System.out.println("turned4");
			}
		}
		//System.out.println("speed: " + this.getSpeed());
		int limit = this.getRoad().getSpeedLimit();
		if(this.getSpeed() < 10){
			this.accelerate(10);
		}
		if((flag!=0 && nc == 0))
			this.accelerate(10);
		
		//System.out.println(flag+" "+red+" "+cf);
		if(flag==0 && red == 1 && cf==null){
			this.accelerate(11);
			flag = 1;
		}
		if(this.getSpeed() > limit)
			this.accelerate(-1*((this.getSpeed()-limit)*10));
	}

}
