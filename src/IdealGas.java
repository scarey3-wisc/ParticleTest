import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class IdealGas{
	final static double radius = 1;
	static double maxSpeed = 100;
	static double minSpeed = 80;
	final static Vec3 G = new Vec3(0, 1, 0);
	static LinkedList<Particle> things;
	static Cell3<Particle> container;
	static boolean paused;
	public static void main(String[] args){
		int height = Toolkit.getDefaultToolkit().getScreenSize().height;
		int width = Toolkit.getDefaultToolkit().getScreenSize().width;
		container = new Cell3<Particle>(radius, width/2 - height/2, height/2 - height/2, -height/2, height);
		things = new LinkedList<Particle>();
		for(int i = 0; i < 10000; i++){
			double x = Math.random() * container.getL() + container.getX();
			double y = Math.random() * container.getL() + container.getY();
			double z = 0;//Math.random() * container.getL() + container.getZ();
			double ii = (Math.random() - 0.5);
			double jj = (Math.random() - 0.5);
			double kk = 0;//(Math.random() - 0.5);
			Vec3 velocity = new Vec3(ii, jj, kk);
			velocity.normalize();
			velocity.scale((0.8 + 0.2*Math.random()) * maxSpeed);
			Particle part = new Particle(new Vec3(x, y, z), velocity, 1, radius);
			things.add(part);
			container.add(part);
		}
		/*for(int i = 0; i < 1000; i++){
			double x = (Math.random()-0.5) * container.getL()/16 + container.getX() + container.getL()/2;
			double y = (Math.random()-0.5) * container.getL()/16 + container.getY() + container.getL()/2;
			double z = 0;//Math.random() * container.getL() + container.getZ();
			double ii = (Math.random() - 0.5);
			double jj = (Math.random() - 0.5);
			double kk = 0;//(Math.random() - 0.5);
			Vec3 velocity = new Vec3(ii, jj, kk);
			velocity.normalize();
			velocity.scale((0.8 + 0.2*Math.random()) * maxSpeed * 2);
			Particle part = new Particle(new Vec3(x, y, z), velocity, 1, radius);
			things.add(part);
			container.add(part);
		}*/
		JFrame jf = new JFrame();
		jf.setExtendedState(JFrame.MAXIMIZED_BOTH);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setLocation(0, 0);
		jf.setVisible(true);
		JPanel jp = new JPanel();
		jp.setBackground(Color.black);
		jf.add(jp);
		jp.updateUI();
		sleep(500);
		Thread run = new Thread(new Renderer(jp.getGraphics(), jp.getWidth(), jp.getHeight(), true));
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
				p.increment(Vec3.mult(p.velocity, dt));
				p.applyForce(dt, G);
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
				if(p.getContainer() == null){
				}else if(p.getContainer().inBox(p)){
					p.getContainer().considerFusing();
					p.getContainer().considerSplitting(p);
				}else{
					p.getContainer().remove(p);
					if(container.inBox(p)){
						container.add(p);
					}else{
						container.inBox(p);
					}
				}
			}
		}
	}
	private static class Renderer implements Runnable{
		private boolean stop;
		private Graphics2D jpg;
		private BufferedImage panel;
		private boolean box;
		public Renderer(Graphics jpg, int w, int h, boolean box){
			this.jpg = (Graphics2D) jpg;
			this.box = box;
			panel = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		}
		@Override
		public void run() {
			while(!stop){
				if(paused){
					sleep(10);
				}else{
					paused = true;
					if(box) {
						renderColoredBox();
					}else {
						renderParticlesAndBox();
					}
					paused = false;
					sleep(20);
				}
			}
		}
		private void renderColoredBox() {
			Graphics g = panel.getGraphics();
			g.setColor(Color.black);
			g.fillRect(0, 0, panel.getWidth(), panel.getHeight());
			g.setColor(new Color(30, 30, 30));
			g.drawRect((int)container.getX(), (int)container.getY(), (int)container.getL(), (int)container.getL());
			double[] result = recursivelyPaintAverages(container, g, maxSpeed, minSpeed);
			
			maxSpeed = result[0];
			minSpeed = result[1];
			AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05F);
		    jpg.setComposite(ac);
			jpg.drawImage(panel, 0, 0, null);
		}
		private void renderParticlesAndBox(){
			Graphics g = panel.getGraphics();
			g.setColor(Color.black);
			g.fillRect(0, 0, panel.getWidth(), panel.getHeight());
			g.setColor(new Color(30, 30, 30));
			g.drawRect((int)container.getX(), (int)container.getY(), (int)container.getL(), (int)container.getL());
			recursivelyPaint(container, g);
			double maximus = 0;
			double minimus = Double.POSITIVE_INFINITY;
			for(Particle l: things){
				double speed = l.velocity.getMagnitude();
				if(speed > maximus){
					maximus = speed;
				}
				if(speed < minimus) {
					minimus = speed;
				}
				double s = 3 * (speed - minSpeed)/(maxSpeed - minSpeed);
				if(s < 0){
					g.setColor(new Color(0, 0, 0));
				}else if(s < 1){
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
			minSpeed = minimus;
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
	private static double[] recursivelyPaintAverages(Cell3<Particle> c3, Graphics g, double minSpeed, double maxSpeed) {
		if(c3.getSubcells() == null && c3.getContents().size() > 0) {
			double maximus = 0;
			double minimus = Double.POSITIVE_INFINITY;
			double totalVelocity = 0;
			for(Particle p: c3.getContents()) {
				double s = p.velocity.getMagnitude();
				totalVelocity += s;
				if(s > maximus) {
					maximus = s;
				}
				if(s < minimus) {
					minimus = s;
				}
			}			
			double s = 3 * (totalVelocity / c3.getContents().size() - minSpeed)/(maxSpeed - minSpeed);
			if(s < 0){
				g.setColor(new Color(0, 0, 0));
			}else if(s < 1){
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
			g.fillRect((int)(c3.getX()+0.5), (int)(c3.getY()+0.5), (int)(c3.getL()+0.5), (int)(c3.getL()+0.5));
			return new double[] {maximus, minimus};
		} else if(c3.getSubcells() != null){
			double maximus = 0;
			double minimus = Double.POSITIVE_INFINITY;
			for(Cell3<Particle> a: c3.getSubcells()){
				double[] result = recursivelyPaintAverages(a, g, minSpeed, maxSpeed);
				if(result[0] > maximus) {
					maximus = result[0];
				}
				if(result[1] < minimus) {
					minimus = result[1];
				}
			}
			return new double[] {maximus, minimus};
		}else {
			return new double[] {0, Double.POSITIVE_INFINITY};
		}
	}
	private static void recursivelyPaint(Cell3<Particle> c3, Graphics g){
		if(c3.getSubcells() != null){
			g.drawLine((int)(c3.getX() + c3.getL()/2), (int) (c3.getY()), (int) (c3.getX() + c3.getL()/2), (int) (c3.getY() + c3.getL()));
			g.drawLine((int)(c3.getX()), (int) (c3.getY() + c3.getL()/2), (int) (c3.getX() + c3.getL()), (int) (c3.getY() + c3.getL()/2));
			for(Cell3<Particle> a: c3.getSubcells()){
				recursivelyPaint(a, g);
			}
		}
	}
}