package smartRockets;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;

import javax.swing.JFrame;

import maths.Vector;

public class Game implements Runnable,MouseListener,MouseMotionListener,KeyListener{
	
	private JFrame frame;
	private Canvas canvas;
	private Thread thread;
	public static int width = 600,height=600;
	private boolean running = false;
	private BufferStrategy bs;
	private Graphics g;
	
	//vals for making obstacles 
	public int mx1,mx2,my1,my2,mtx,mty,ttx,tty;
	public  boolean drawObst=false;
	//Real Stuff
	private Population population;
	private ArrayList<Obstacle> obstacles;
	public static boolean progress=false,normalSpeed=true,superSpeed=false,moveTarget=false;
	public boolean reset=false;
	public Game(){
		frame = new JFrame("Smart Rockets With NN");
		frame.setSize(width,height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		
		canvas = new Canvas();
		canvas.setPreferredSize(new Dimension(width,height));
		canvas.setMaximumSize(new Dimension(width,height));
		canvas.setMinimumSize(new Dimension(width,height));
		canvas.setFocusable(false);
		frame.addMouseListener(this);
		frame.addMouseMotionListener(this);
		frame.addKeyListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		frame.add(canvas);
		frame.pack();
		init();
	}
	
	
	public void init(){
		population = new Population();
		obstacles = new ArrayList<Obstacle>();
		//obstacles.add(new Obstacle(200,200,100,25));
	}
	
	public synchronized void start(){
		thread = new Thread(this);
		thread.start();
		running = true;
	}
	
	public void render(){
		bs = canvas.getBufferStrategy();
		if(bs==null){
			canvas.createBufferStrategy(3);
			return;
		}
		g=bs.getDrawGraphics();
		g.clearRect(0, 0, width, height);
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, width, height);
		//start
		Graphics2D g2  = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			    RenderingHints.VALUE_ANTIALIAS_ON);	
	
		
		g.setColor(new Color(255,0,0,150));
		g.drawRect(0, 0, width, height);
		g.drawRect(1, 1, width-2, height-2);
		population.render(g,obstacles);
		
		g.setColor(new Color(255,0,0,150));
		for(Obstacle o:obstacles){
			g.fillRect(o.x, o.y, o.width, o.height);
		}
		
		g.setColor(new Color(255,255,255));
		for(Obstacle o:obstacles){
			g.drawRect(o.x, o.y, o.width, o.height);
			
		}
		if(moveTarget){
			g.setColor(new Color(0,255,0,100));
			g.fillOval(ttx-Population.tr/2,tty-Population.tr/2,Population.tr,Population.tr);
			g.setColor(Color.WHITE);
			g.drawOval(ttx-Population.tr/2,tty-Population.tr/2,Population.tr,Population.tr);
		
		}
		if(drawObst){
			g.setColor(new Color(255,0,0,100));
			int x1=mx1,y1=my1;//begining parts
			int x2=mtx,y2=mty;//end parts
			
			if(mtx<mx1){
				x1=mtx;
				x2=mx1;
			}
			if(mty<my1){
				y1=mty;
				y2=my1;
			}
			g.fillRect(x1,y1,x2-x1,y2-y1);
			g.setColor(Color.WHITE);
			g.drawRect(x1,y1,x2-x1,y2-y1);
		}
		
		if(reset){
			g.setColor(Color.orange);
			g.setFont(new Font("sans-serif" , Font.BOLD,100));
			g.drawString("RESET!", Game.width/2-150, Game.height/2);
		}
		
		//end
		bs.show();
		g.dispose();
	}
	
	
	
	public void run(){
		while(running){
			for(int i=0;i<5;i++){
				render();
			}
			if(normalSpeed){
				try {
					Thread.sleep(1000/60);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public synchronized void stop(){
		try{
			thread.join();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		running = false;
	}
	
	//Mouse Events
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if(drawObst){
			mtx=e.getX();
			mty=e.getY();
			
		}
		if(moveTarget){
			ttx=e.getX();
			tty=e.getY();
		}
	}


	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getButton()==1){
			mx1 = e.getX();
			my1 = e.getY();
			mx2=mx1;
			my2=my1;
			mtx=mx1;
			mty=my1;
			drawObst=true;
			
		}
		if(e.getButton()==3){
			if(new Rectangle((int)Population.target.x,(int)Population.target.y,Population.tr,Population.tr).contains(e.getX(),e.getY())){
				moveTarget=true;
			}
		}
		if(e.getButton()==3){
			for(int i=0;i<obstacles.size();i++){
				if(obstacles.get(i).bounds.contains(e.getX(), e.getY())){
					obstacles.remove(i);
					break;
				}
			}
		}
	}


	@Override
	public void mouseReleased(MouseEvent e) {
		if(e.getButton()==1){
			mx2=e.getX();
			my2=e.getY();
			
			if(mx2<mx1){
				int t = mx2;
				mx2=mx1;
				mx1=t;
			}
			if(my2<my1){
				int t = my2;
				my2=my1;
				my1=t;
			}
			obstacles.add(new Obstacle(mx1, my1,(int)Math.abs(mx2-mx1),(int)Math.abs( my2-my1)));
			mx1=0;mx2=0;my1=0;my2=0;
			drawObst=false;
		}
		if(e.getButton()==3 && moveTarget){
			Population.target.x = e.getX() - Population.tr/2;
			Population.target.y = e.getY() - Population.tr/2;
			moveTarget=false;
		}
	}


	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER)
			progress = !progress;
		else if(e.getKeyCode() == KeyEvent.VK_L){
			System.out.println(Rocket.lifeSpan);
		}
		else if(e.getKeyCode() == KeyEvent.VK_N){
			normalSpeed = !normalSpeed;
		}
		else if(e.getKeyCode() == KeyEvent.VK_SPACE){
			superSpeed = !superSpeed;
		}
		else if(e.getKeyCode() == KeyEvent.VK_D){
			obstacles.clear();
		}
		else if(e.getKeyCode() == KeyEvent.VK_R){
			population = new Population();
			normalSpeed=true;
			progress=false;
			superSpeed = false;
			obstacles.clear();
			Population.target = new Vector(Game.width/2-Rocket.rx/2,20);
			ttx=0;tty=0;
			moveTarget=false;
			Rocket.lifeSpan=450;
			Rocket.maxSpeed=4f;
			Rocket.maxForce=0.3f;
			try {
				reset=true;
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			reset=false;
		}
		else if(e.getKeyCode() ==KeyEvent.VK_UP){
			Rocket.maxSpeed+=0.1f;
		}
		else if(e.getKeyCode() ==KeyEvent.VK_DOWN){
			Rocket.maxSpeed-=0.1f;
		}
		else if(e.getKeyCode() ==KeyEvent.VK_LEFT){
			Rocket.maxForce-=0.01f;
		}
		else if(e.getKeyCode() ==KeyEvent.VK_RIGHT){
			Rocket.maxForce+=0.01f;
		}
	}


	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
