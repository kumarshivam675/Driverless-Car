import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;
import org.iiitb.es103_15.traffic.*;
import org.iiitb.es103_15.traffic.TrafficSignal.SignalListener;

public class Car14 extends Car implements SignalListener{
		private Color diff_col[];
	    private Color carColor;
	    private boolean watch_red;
	    Intersection nextInter;
	    Car front_car;
	    Random random;
    public Car14()
    {
    	diff_col = (new Color[] { Color.BLACK, Color.RED,Color.DARK_GRAY,Color.MAGENTA}); 
        watch_red = false;
        nextInter = null;
        front_car = null;
        random = new Random();
        carColor = diff_col[getId()%4];
    }
    
    public String toString() {
   	//String s = (new StringBuilder("A14:")).append(getId()).toString();
   	return "A14:"+getId();
    }

    public void paint(Graphics g)
    {
        g.setColor(carColor);
        super.paint(g);
       
    }

    public void drive()
    {
        super.drive();       
        accelerate(getRoad().getSpeedLimit(),0);
    }
    
    public void onChanged(int State_of_light)
    {
        if(State_of_light == 0)
            watch_red = true;
        else
            watch_red = false;
    }

    private int getDist(Car car)
    {
        Coords x2 = car.getPos();
        Coords x1 = getPos();
        int distance = 0;
        distance = (x2.x - x1.x) + (x2.y - x1.y);
        if(distance < 0)
            distance = distance*-1;
        return distance - car.getLength();
    }
 
    public void carInFront(Car c2)
    {
        front_car = c2;
    }

    private Intersection findNextInter()
    {
        Road r = getRoad();
        if(getDir() == r.getDir())
            nextInter = r.getEndIntersection();
        else
            nextInter = r.getStartIntersection();
        TrafficControl tc = nextInter.getTrafficControl();
        if(tc != null && tc.getType() == 0)
        {
            int oppDir = RoadGrid.getOppDir(getDir());
            synchronized(tc)
            {
                ((TrafficSignal)tc).addListener(this, oppDir);
            }
            watch_red = ((TrafficSignal)tc).getSignalState(oppDir) == 0;
        }
        return nextInter;
    }

    public void setInitialPos(Road r, Coords loc, int dir)
    {
        super.setInitialPos(r, loc, dir);
        findNextInter();
    }

    private int next_ints_dist()
    {
        Coords pos = getPos();
        Coords new_pos = nextInter.getCoords();
        int distance = 0;
        switch(getDir())
        {
        case 0: 
            distance = pos.y - new_pos.y;
            break;

        case 1: 
            distance = new_pos.x - pos.x;
            break;

        case 2: 
            distance = new_pos.y - pos.y;
            break;

        case 3: 
            distance = pos.x - new_pos.x;
            break;
        }
        return distance - 12;
    }

    private void make_Turn(int dir)
    {
        TrafficControl traf_cont = nextInter.getTrafficControl();
        int oppDir = RoadGrid.getOppDir(getDir());
        boolean listen = false;
        if(traf_cont != null && traf_cont.getType() == 0)
            listen = true;
        if(!watch_red && !nextInter.isOccupied())
        {
            if(listen)
                synchronized(traf_cont)
                {
                    ((TrafficSignal)traf_cont).removeListener(this, oppDir);
                }
            synchronized(nextInter)
            {
                crossIntersection(nextInter, dir);
                this.accelerate((getRoad().getSpeedLimit() - getSpeed())*10,0);
                
            }
            findNextInter();
        }
    }

    protected void updatePos()
    {
        super.updatePos();
        float speed = getSpeed();
        float speed_lim = getRoad().getSpeedLimit();
        int dist_ints = next_ints_dist();
        int dist_obst = dist_ints;
        int dist_new_car = dist_ints;
        if(front_car != null)
        {
            dist_new_car = getDist(front_car);
            if(dist_new_car < dist_obst)
                dist_obst = dist_new_car;
        }
       if((float)dist_obst > 4F * speed)
        {
            float a = 0.0F;
            if((double)speed < 0.8D * (double)speed_lim)
                a = 50F;
            else
            if(speed < speed_lim)
                a = 10F;
            else
            if(speed > speed_lim)
                a = 10F * (speed_lim - speed);
            if((double)(speed + a) < 10D)
                a = 10F - speed;
            accelerate(a);
        } 
       else if(dist_ints < 6)
        {
            boolean stop_inter = false;
            
            if(!stop_inter)
            {
                int dir = random.nextInt(3);
                Road roads[] = nextInter.getRoads();
                int d[] = new int[3];
                int direction = getDir();
                d[0] = RoadGrid.getLeftDir(direction);
                d[1] = direction;
                for(d[2] = RoadGrid.getRightDir(direction); roads[d[dir]] == null; dir = random.nextInt(3));
                accelerate(-50F);
                make_Turn(d[dir]);
            }
            if(watch_red || front_car != null)
            {
                accelerate(-50F);
                stop_inter = true;
            }
        }
        else if(speed > 12F)
            accelerate(-(speed * speed) / (float)(4 * dist_ints));
        else
            accelerate(10F - speed);
       
       if(front_car!=null){
			double diff;
			if(getRoad().getDir()==getDir()&& (getDist((Car)this,front_car,getDir()) < 2*getSpeed())){ 
				if(getSpeed()>front_car.getSpeed())
					 diff = getSpeed() - front_car.getSpeed();
				else
					 diff = -1*(getSpeed() - front_car.getSpeed());
				accelerate(-1*(float)(diff/2.0f),2);
				
			if((int) Math.sqrt(Coords.distSqrd(front_car.getPos(), getPos())) < 20)
				accelerate((float) (-1*getSpeed()*getSpeed()/(0.01)) , 20);
			}
		}
    }
    
    private static int getDist(Car c1, Car c2, int dir) { 
		int dist = 0; 
		dist = (c2.getPos().x - c1.getPos().x) + (c2.getPos().y - c1.getPos().y); 
		if(dir == 0 || dir == 3) dist *= -1; 
		return dist - 12; 
	}
}