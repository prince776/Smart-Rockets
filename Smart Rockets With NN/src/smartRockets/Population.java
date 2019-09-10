package smartRockets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;

import maths.Vector;

public class Population {
	/**
	 * refers to current generation
	 */
	public ArrayList<Rocket> rockets;
	public ArrayList<Rocket> oldGen;
	public static int popSize = 100;
	public int generation=1;
	//Target
	public static Vector target;
	public static int tr=24;
	public int genAge=0;
	
	public static float maxMR = 0.8f,minMR=0.25f,maxMD=0.7f,minMD=0.25f;
	public int loops=1;
	public int maxReached=0;
	public Population(){
		rockets = new ArrayList<Rocket>();
		oldGen = new ArrayList<Rocket>();
		for(int i=0;i<popSize;i++){
			rockets.add(new Rocket());
		}
		target = new Vector(Game.width/2-Rocket.rx/2,20);
	}
	
	public void render(Graphics g,ArrayList<Obstacle> obstacles){
			if(Game.progress){
				for(int j=0;j<loops;j++){
					genAge++;
					if(genAge>Rocket.lifeSpan){
						regenerate(obstacles);
					}
					
					for(int i=rockets.size()-1;i>=0;i--){
						rockets.get(i).tick(obstacles);
					}
				}
			
		}
		if(Game.normalSpeed){
			loops=1;
		}else if(!Game.normalSpeed){
			loops=20;
		}
		if(Game.superSpeed){
			loops=100;
		}
		for(int i=rockets.size()-1;i>=0;i--){
			rockets.get(i).render(g);
		}
		//render target
		g.setColor(Color.GREEN);
		g.fillOval((int)target.x,(int)target.y,tr+1,tr+1);
		g.setColor(Color.WHITE);
		g.drawOval((int)target.x,(int)target.y,tr,tr);
		renderGUI(g);
	}
	
	public void renderGUI(Graphics g){
		int ra = rockets.size();
		int rr = 0;
		for(Rocket r:rockets){
			if(r.deadByBoundaries || r.deadByCollision)
				ra--;
			if(r.reached)
				rr++;
		}
		if(rr>maxReached)
			maxReached=rr;
		g.setColor(Color.orange);
		g.setFont(new Font("sans-serif" , Font.BOLD,12));
		g.drawString("Generation: " + generation , 20,20);
		g.drawString("Rockets Alive: " + ra, 20, 40);
		g.drawString("Rockets Reached: " + rr, 20, 60);
		g.drawString("Maximum Rockets Reached: " + maxReached +" => " +  (maxReached*100)/rockets.size()+"%", 20, 80);
		g.drawString("Current Lifespan: " + Rocket.lifeSpan,20,100);
		
		g.drawString("Rockets Max Speed: " + Math.round(Rocket.maxSpeed*100.0)/100.0 + " m/s", 20, 120);
		g.drawString("Rockets Max Force: " + Math.round(Rocket.maxForce*100.0)/100.0+" N", 20, 140);
		
		g.drawString("Playback Speed: " + loops + "x, and " + ((Game.progress)? "RUNNING!":"PAUSED!"), 20, 160);
		
	}
	
	public void regenerate(ArrayList<Obstacle> obstacles){
		genMatingPool();
		rockets.clear();
		calcFitness(obstacles);
		normalizeFitnesses();
		generate();
		genAge=0;
		oldGen.clear();
		generation++;
	}
	

	
	public void genMatingPool(){
		for(int i=0;i<rockets.size();i++){
			oldGen.add(rockets.get(i));
		}
	}
	
	public void calcFitness(ArrayList<Obstacle> obstacles){
		for(Rocket r:oldGen){
			r.calcFitness(obstacles);
		}
	}
	
	public void normalizeFitnesses(){
		float sum=0;
		for(Rocket r:oldGen){
			sum+=r.fitness;
		}
		for(Rocket r:oldGen){
			r.normalizedFitness=r.fitness / sum;

		}
	}
	
	public void generate(){
		
		float maxNormalizedFitness = 0;
		for(Rocket r:oldGen){
			if(r.normalizedFitness>maxNormalizedFitness)
				maxNormalizedFitness = r.normalizedFitness;
		}
		
		for(int i=0;i<popSize;i++){
			Rocket parent1 = naturalSelection();
			Rocket parent2 = naturalSelection();
			//Rocket child = new Rocket(parent1.brain);
			Rocket child = new Rocket(parent1.brain,parent2.brain);
			
			float avgNFitness = (parent1.normalizedFitness + parent2.normalizedFitness)/2;
			
			float mr = (minMR/2)*(maxNormalizedFitness/avgNFitness) + minMR;
			if(mr>maxMR)
				mr=maxMR;
			float md = (minMD/2)*(maxNormalizedFitness/avgNFitness) + minMD;
			if(md>maxMD)
				md=maxMD;
			//System.out.println(mr +":"+md);
			child.brain.mutate(mr,md);
			rockets.add(child);

		}
		int lifeSpanDelta = (int)Math.floor(Math.random()*3)-1;
		Rocket.lifeSpan+=lifeSpanDelta;
	}
	
	public Rocket naturalSelection(){
		float r = (float)Math.random();
		int index=0;
		while(r>0){
			r-=oldGen.get(index).normalizedFitness;
			index++;
		}
		index--;
		if(index>=oldGen.size())
			index = oldGen.size()-1;
		return oldGen.get(index);
	}
	
}
