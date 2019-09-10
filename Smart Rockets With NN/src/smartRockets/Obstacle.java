package smartRockets;

import java.awt.Rectangle;

public class Obstacle {
	
	public int x,y,width,height;
	
	public Rectangle bounds;
	
	public Obstacle(int x ,int y,int width,int height){
		this.x=x;
		this.y=y;
		this.width=width;
		this.height=height;
		this.bounds = new Rectangle(x, y, width, height);
	}
	
}
