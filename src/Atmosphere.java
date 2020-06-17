import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Atmosphere{
	final static double radius = 5;
	static double maxSpeed = 100;
	final static Vec3 G = new Vec3(0, 1, 0);
	static LinkedList<Particle> things;
	static Cell3<Particle> container;
	static boolean paused;
	public static void main(String[] args){
		int height = Toolkit.getDefaultToolkit().getScreenSize().height;
		int width = Toolkit.getDefaultToolkit().getScreenSize().width;
		container = new Cell3<Particle>(radius, width/2 - height/2, 0, -height/2, height);
		things = new LinkedList<Particle>();
		for(int i = 0; i < 10000; i++){
			double x = Math.random() * container.getL() + container.getX();
			double y = Math.random() * container.getL() + container.getY();
			double z = Math.random() * container.getL() + container.getZ();
			double ii = (Math.random() - 0.5);
			double jj = (Math.random() - 0.5);
			double kk = (Math.random() - 0.5);
			Vec3 velocity = new Vec3(ii, jj, 0);
			velocity.normalize();
			velocity.scale((0.8 + 0.2 * Math.random()) * maxSpeed);
			Particle part = new Particle(new Vec3(x, y, 0), velocity, 1, radius);
			things.add(part);
			container.add(part);
		}
		JFrame jf = new JFrame();
		jf.setSize(width, height);
		jf.setUndecorated(true);
		jf.setLocation(0, 0);
		jf.setVisible(true);
		JPanel jp = new JPanel();
		jp.setBackground(Color.white);
		jf.add(jp);
		jp.updateUI();
		sleep(500);
		Thread run = new Thread(new Renderer(jp.getGraphics(), jp.getWidth(), jp.getHeight()));
		Thread sim = new Thread(new Simulator());
		run.start();
		sim.start();
	}
	private static class Simulator implements Runnable{
		private boolean stop;
		@Override
		public void run() {
			long ct = System.currentTimeMillis();
			while(!stop){
				if(paused){
					sleep(1);
				}else{
					paused = true;
					double dt = 0.001 * (System.currentTimeMillis() - ct);
					step(dt);
					paused = false;
					ct = System.currentTimeMillis();
					sleep(5);
				}
			}
		}
		private void step(double dt){
			for(Particle p: things){
				//Updates the velocity and position of the particle
				change(p, dt);
				
				//checks for any collisions
				collide(p);
				
				//keeps the particles in the box
				contain(p);
				consider(p);
			}
		}
		private void consider(Particle p){
			if(p.getContainer().inBox(p)){
				p.getContainer().considerFusing();
				p.getContainer().considerSplitting(p);
			}else{
				p.getContainer().remove(p);
				container.add(p);
			}
		}
		private void collide(Particle p){
			/*
			 * Version 1.0:
			 * 1. Check only things in the same container, don't worry about things crossing over containers.
			 * 2. Use center of mass frame of reference trick assuming equal masses.
			 */
			for(Particle pp: p.getContainer().getContents()){
				if(pp != p){
					if(Vec3.add(pp, Vec3.negative(p)).getMagSqr() < (pp.radius + p.radius)*(pp.radius + p.radius)){
						//WE HAVE A COLLISION
						Vec3 cM = Vec3.add(p, pp);
						cM.scale(0.5);
						Vec3 cV = Vec3.add(p.velocity, pp.velocity);
						cV.scale(0.5);
						p.velocity.increment(Vec3.negative(cV));
						pp.velocity.increment(Vec3.negative(cV));
						p.velocity.negate();
						pp.velocity.negate();
						p.velocity.increment(cV);
						pp.velocity.increment(cV);
					}
				}
			}
		}
		private void contain(Particle p){
			if(p.getX() - radius < container.getX()){
				p.setX(container.getX() + p.radius);
				p.velocity.setX(p.velocity.getX() * -1);
			}
			if(p.getY() - radius < container.getY()){
				p.setY(container.getY() + p.radius);
				p.velocity.setY(p.velocity.getY() * -1);
			}
			if(p.getZ() - radius < container.getZ()){
				p.setZ(container.getZ() + p.radius);
				p.velocity.setZ(p.velocity.getZ() * -1);
			}
			if(p.getX() + radius > container.getX() + container.getL()){
				p.setX(container.getX() + container.getL() - p.radius);
				p.velocity.setX(p.velocity.getX() * -1);
			}
			if(p.getY() + radius > container.getY() + container.getL()){
				p.setY(container.getY() + container.getL() - p.radius);
				p.velocity.setY(p.velocity.getY() * -1);
			}
			if(p.getZ() + radius > container.getZ() + container.getL()){
				p.setZ(container.getZ() + container.getL() - p.radius);
				p.velocity.setZ(p.velocity.getZ() * -1);
			}
		}
		private void change(Particle p, double dt){
			p.increment(Vec3.mult(p.velocity, dt));
			p.applyForce(dt, G);
		}
	}
	private static class Renderer implements Runnable{
		private boolean stop;
		private Graphics jpg;
		private BufferedImage panel;
		public Renderer(Graphics jpg, int w, int h){
			this.jpg = jpg;
			panel = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		}
		@Override
		public void run() {
			while(!stop){
				if(paused){
					sleep(10);
				}else{
					paused = true;
					render();
					paused = false;
					sleep(20);
				}
			}
		}
		private void render(){
			Graphics g = panel.getGraphics();
			g.setColor(Color.black);
			g.fillRect(0, 0, panel.getWidth(), panel.getHeight());
			g.setColor(new Color(30, 30, 30));
			recursivelyPaint(container, g);
			double maximus = 0;
			for(Particle l: things){
				double speed = l.velocity.getMagnitude();
				if(speed > maximus){
					maximus = speed;
				}
				double s = 3 * l.rad;
				if(s < 1){
					int r = (int) (s * 255);
					g.setColor(new Color(0, 0, r));
				}else if(s < 2){
					int r = (int) ((s - 1) * 255);
					g.setColor(new Color(r, 0, 255 - r));
				}else if(s < 3){
					int r = (int) ((s - 2) * 255);
					g.setColor(new Color(255, r, r));
				}else{
					g.setColor(new Color(255, 255, 255));
				}
				g.fillOval((int) (l.getX() - radius), (int) (l.getY() - radius), (int) (radius * 2), (int) (radius * 2));
			}
			maxSpeed = maximus;
			jpg.drawImage(panel, 0, 0, null);
		}
	}
	private static void sleep(long milis){
		try {
			Thread.sleep(milis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	private static void recursivelyPaint(Cell3<Particle> c3, Graphics g){
		g.drawRect((int)c3.getX(), (int)c3.getY(), (int)c3.getL(), (int)c3.getL());
		if(c3.getSubcells() != null){
			for(Cell3<Particle> a: c3.getSubcells()){
				recursivelyPaint(a, g);
			}
		}
	}
}