import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class CellVisualizer{
	final static double radius = 5;
	static LinkedList<Loc> things;
	static Cell3<Loc> container;
	public static void main(String[] args){
		int height = Toolkit.getDefaultToolkit().getScreenSize().height;
		int width = Toolkit.getDefaultToolkit().getScreenSize().width;
		container = new Cell3<Loc>(radius, width/2 - height/2, 0, -height/2, height);
		things = new LinkedList<Loc>();
		for(int i = 0; i < 100; i++){
			double x = Math.random() * container.getL() + container.getX();
			double y = Math.random() * container.getL() + container.getY();
			Loc newLoc = new Loc(new Vec3(x, y, 0), radius);
			things.add(newLoc);
			container.add(newLoc);
		}
		System.out.println(container.numCells());
		JFrame jf = new JFrame();
		jf.setSize(width, height);
		jf.setExtendedState(JFrame.MAXIMIZED_BOTH);
		jf.setLocation(0, 0);
		jf.setVisible(true);
		JPanel jp = new JPanel();
		jp.setBackground(Color.white);
		jf.add(jp);
		jp.updateUI();
		ThingUpdater tu = new ThingUpdater();
		jp.addMouseListener(tu);
		jp.addMouseMotionListener(tu);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		BufferedImage bi = new BufferedImage(jp.getWidth(), jp.getHeight(), BufferedImage.TYPE_INT_RGB);
		while(true){
			Graphics g = bi.getGraphics();
			g.setColor(Color.white);
			g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
			g.setColor(new Color(150, 150, 150));
			recursivelyPaint(container, g);
			g.setColor(Color.red);
			for(Loc l: things){
				g.fillOval((int) (l.getX() - radius), (int) (l.getY() - radius), (int) (radius * 2), (int) (radius * 2));
			}
			Graphics gg = jp.getGraphics();
			gg.drawImage(bi, 0, 0, null);
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	private static void recursivelyPaint(Cell3<Loc> c3, Graphics g){
		g.drawRect((int)c3.getX(), (int)c3.getY(), (int)c3.getL(), (int)c3.getL());
		if(c3.getSubcells() != null){
			for(Cell3<Loc> a: c3.getSubcells()){
				recursivelyPaint(a, g);
			}
		}
	}
	private static class ThingUpdater implements MouseListener, MouseMotionListener{
		private Loc selected;
		@Override
		public void mouseClicked(MouseEvent arg0) {
			Thread.yield();
			Loc found = null;
			Vec3 nova = new Vec3(arg0.getX(), arg0.getY(), 0);
			for(Loc l: things){
				if(Vec3.add(l, Vec3.mult(nova, -1)).getMagnitude() < 2*l.radius){
					found = l;
					break;
				}
			}
			if(found == null){
				Loc newLoc = new Loc(nova, radius);
				things.add(newLoc);
				container.add(newLoc);
			}else{
				things.remove(found);
				Cell3<Loc> cont = found.getContainer();
				cont.remove(found);
			}
		}
		@Override
		public void mouseEntered(MouseEvent arg0) {}
		@Override
		public void mouseExited(MouseEvent arg0) {}
		@Override
		public void mousePressed(MouseEvent arg0) {
			Vec3 nova = new Vec3(arg0.getX(), arg0.getY(), 0);
			for(Loc l: things){
				if(Vec3.add(l, Vec3.mult(nova, -1)).getMagnitude() < l.radius){
					selected = l;
					break;
				}
			}
		}
		@Override
		public void mouseReleased(MouseEvent arg0) {
			selected = null;
		}
		@Override
		public void mouseDragged(MouseEvent arg0) {
			if(selected != null){
				selected.setValues(new double[]{arg0.getX(), arg0.getY(), 0});
				Cell3<Loc> cont = selected.getContainer();
				if(cont.inBox(selected)){
					cont.considerFusing();
					cont.considerSplitting(selected);
				}else{
					cont.remove(selected);
					container.add(selected);
				}
			}
		}
		@Override
		public void mouseMoved(MouseEvent arg0) {}
	}
	private static class Loc extends CellContent3<Loc>{
		private double radius;
		public Loc(Vec3 input, double radius) {
			super(input);
			this.radius = radius;
		}
	}
}