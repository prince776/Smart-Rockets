package smartRockets;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;

import maths.Vector;
import ai.NeuralNetwork;

public class Rocket {
	
	public static int lifeSpan = 450;
	
	public Vector pos,vel,acc;
	public static int rx=12,ry=30;
	public static float maxSpeed=4f,maxForce=0.3f;
	public NeuralNetwork brain;
	public boolean deadByBoundaries = false,deadByCollision=false,reached=false;
	public float fitness=0,normalizedFitness=0;
	public float rot=0;
	
		
	public Rocket(){
		pos = new Vector(Game.width/2-rx/2 , Game.height - ry);
		vel = new Vector();
		vel.randomize();
		acc = new Vector();
		brain = new NeuralNetwork(10,20, 2);
	}
	
	public Rocket(NeuralNetwork brain){
		pos = new Vector(Game.width/2-rx/2 , Game.height - ry);
		vel = new Vector();
		vel.randomize();
		acc = new Vector();
		this.brain= brain.copy();
	}
	
	public Rocket(NeuralNetwork brain1,NeuralNetwork brain2){
		pos = new Vector(Game.width/2-rx/2 , Game.height - ry);
		vel = new Vector();
		vel.randomize();
		acc = new Vector();
		this.brain= NeuralNetwork.crossover(brain1,brain2);
		//this.brain= NeuralNetwork.fixedCrossover(brain1,brain2);
	}
	
	public void tick(ArrayList<Obstacle> obstacles){
		deadByBoundaries = isOutside();
		deadByCollision = hasCollided(obstacles);
		reached = hasReached();
		if(!deadByBoundaries &&!deadByCollision && !hasReached()){
			move(obstacles);
			vel.add(acc);
			vel.limit(maxSpeed);
			pos.add(vel);
		}
		acc.multiply(0);
	}
	
	public Rectangle getBounds(){
		Rectangle r =  new Rectangle((int)pos.x,(int)pos.y,rx,ry);
		
		return r;
	}
	
	public boolean isOutside(){
		float x = pos.x +rx/2;
		float y = pos.y +ry/2;
		
		if(x<0||y<0||x>Game.width-rx/2){//||pos.y>Game.height-r){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean hasCollided(ArrayList<Obstacle> obstacles){
		for(Obstacle o:obstacles){
			//if(getBounds().intersects(o.bounds))
			float x = pos.x + rx/2;
			float y = pos.y + ry/2;
			if(o.bounds.contains(x,y))
				return true;
		}
		return false;
	}
	
	
	
	public boolean hasReached(){
		//if(this.getBounds().intersects(new Rectangle((int)Population.target.x,(int)Population.target.y,Population.tr,Population.tr)))
		float x = pos.x + rx/2;
		float y = pos.y + ry/2;
		float tx = Population.target.x + Population.tr/2;
		float ty = Population.target.y + Population.tr/2;
		float dist = (float)Math.sqrt((x-tx)*(x-tx)+(y-ty)*(y-ty));
		if(dist <=Population.tr/2)	{
			//System.out.println(dist);
			return true;
		}
		return false;
	}
	
	public float getLineWithTarget(float x){
		float dx = pos.x - Population.target.x;
		float dy = pos.y - Population.target.y;
		return (dy*(x-pos.x) + pos.y * dx)/dx;
		
	}
	
	public void calcFitness(ArrayList<Obstacle> obstacles){
		float dx = pos.x - Population.target.x;
		float dy = pos.y - Population.target.y;
		float distance = (float)Math.sqrt(dx*dx + dy*dy);
		fitness = 1f/distance;
		fitness= (float)Math.pow(fitness, 4);
		if(deadByCollision){
			fitness/=60;
		}
		else if(deadByBoundaries){
			fitness/=20;
		}
		else if(reached){
			fitness*=180;
		}
		else if(pos.y >Game.height + ry){
			fitness/=10;
		}else{
			fitness*=80;
		}
		
	}
	
	public float getRot(){
		float rotT =   ((float)Math.atan2(vel.y,vel.x));
		 rotT +=(float)Math.PI/2;
		return rotT;
	}
	
	public float dist(Obstacle o){
		float d = (pos.x-(o.x+o.width/2))*(pos.x-(o.x+o.width/2)) + (pos.y-(o.y+o.height/2))*(pos.y-(o.y+o.height/2));
		d = (float)Math.sqrt(d);
		return d;
	}
	
	public void move(ArrayList<Obstacle> obstacles){
		float[] inputs = new float[10];
		inputs[0] = pos.x/Game.width;
		inputs[1] = pos.y/Game.height;
		inputs[2] = vel.x/maxSpeed;
		inputs[3] =  vel.y/maxSpeed;
		inputs[4] = Population.target.x/Game.width;//target X
		inputs[5] = Population.target.y/Game.height;//target Y
		Obstacle o=null ;
		if(obstacles.size()>=1)
			o = obstacles.get(0);
		if(o==null){
			o = new Obstacle(0, 0, 0, 0);
		}
		for(Obstacle oi :obstacles){
			if(dist(o)>dist(oi)){
				o = oi;
			}
		}
		
		inputs[6] = o.x/Game.width;
		inputs[7] = o.y/Game.height;
		inputs[8] = o.width/Game.width;
		inputs[9] = o.height/Game.height;
		
		float[] guess = brain.predict(inputs);
		for(int i=0;i<guess.length;i++){
			guess[i] = guess[i]*2-1f;
		}
		Vector force = new Vector(guess[0], guess[1]);
		force.setMagnitude(maxForce);
		applyForce(force);
	}
	
	public void applyForce(Vector force){
		this.acc.add(force);
	}
	
	public void render(Graphics g1){
		
		Graphics2D g = (Graphics2D)g1;
		Rectangle rect = new Rectangle(0,0,rx,ry);
		
		g.translate((int)pos.x+rx/2,(int)pos.y+ry/2);
		rot = getRot();
		g.rotate(rot);
		g.translate(-rx/2, -ry/2);

		g.setColor(new Color(255,255,255,100));
		g.fill(rect);
		g.setColor(new Color(255,255,255,255));
		g.draw(rect);
		
		g.translate(rx/2, ry/2);
		g.rotate(-rot);//Math.atan(-pos.y/pos.x));
		g.translate(-(int)pos.x-rx/2,-(int)pos.y-ry/2);
		
	}
	
}
