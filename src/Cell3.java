import java.util.ArrayList;
import java.util.LinkedList;

public class Cell3<E extends CellContent3<E>>{
	private LinkedList<E> contents;
	private ArrayList<Cell3<E>> subcells;
	private double minDim;
	private double x, y, z;
	private double l;
	private Cell3<E> parent;
	public Cell3(double minDim, double x, double y, double z, double l){
		contents = new LinkedList<E>();
		this.minDim = minDim;
		this.x = x;
		this.y = y;
		this.z = z;
		this.l = l;
	}
	public Cell3(double minDim, double x, double y, double z, double l, Cell3<E> parent){
		contents = new LinkedList<E>();
		this.minDim = minDim;
		this.x = x;
		this.y = y;
		this.z = z;
		this.l = l;
		this.parent = parent;
	}
	public void add(E e){
		if(subcells != null){
			subcells.get(calculateCell(e)).add(e);
		}else if(contents.size() == 0){
			contents.add(e);
			e.setContainer(this);
		}else if(splitNecessary(e) && l/2 > minDim){
			split();
			subcells.get(calculateCell(e)).add(e);
		}else{
			contents.add(e);
			e.setContainer(this);
		}
	}
	public int numCells(){
		int total = 1;
		if(subcells != null){
			for(Cell3<E> c: subcells){
				total+= c.numCells();
			}
		}
		return total;
	}
	public int numContents(){
		if(subcells != null){
			int total = 0;
			for(Cell3<E> c: subcells){
				total+= c.numContents();
			}
			return total;
		}else{
			return contents.size();
		}
	}
	public boolean remove(E e){
		if(subcells != null){
			return false;
		}else{
			if(contents.remove(e)){
				e.setContainer(null);
				if(contents.size() == 0 && parent != null){
					parent.considerFusing();
				}
				return true;
			}else{
				return false;
			}
		}
	}
	public boolean inBox(E e){
		return x <= e.getX() && x + l > e.getX() && y <= e.getY() && y + l > e.getY() && z <= e.getZ() && z + l > e.getZ();
	}
	public void considerSplitting(E e){
		if(subcells != null){
			return;
		}
		if(splitNecessary(e)){
			split();
		}
	}
	public void considerFusing(){
		if(subcells == null){
			return;
		}
		int numEmpty = 0;
		int totalContents = 0;
		for(Cell3<E> c: subcells){
			if(c.numContents() == 0){
				numEmpty++;
			}
			totalContents+= c.numContents();
		}
		if(numEmpty == subcells.size()){
			subcells = null;
			if(parent != null){
				parent.considerFusing();
			}
		}else if(numEmpty == subcells.size() - 1){
			if(totalContents == 1){
				for(Cell3<E> c: subcells){
					for(E e: c.contents){
						contents.add(e);
						e.setContainer(this);
					}
				}
				subcells = null;
				if(parent != null){
					parent.considerFusing();
				}
			}
		}
	}
	public LinkedList<E> getContents(){
		return contents;
	}
	public double getX(){
		return x;
	}
	public double getY(){
		return y;
	}
	public double getZ(){
		return z;
	}
	public double getL(){
		return l;
	}
	public ArrayList<Cell3<E>> getSubcells(){
		return subcells;
	}
	private void split(){
		subcells = new ArrayList<Cell3<E>>();
		subcells.add(0, new Cell3<E>(minDim, x, y, z, l/2, this));
		subcells.add(1, new Cell3<E>(minDim, x+l/2, y, z, l/2, this));
		subcells.add(2, new Cell3<E>(minDim, x, y+l/2, z, l/2, this));
		subcells.add(3, new Cell3<E>(minDim, x+l/2, y+l/2, z, l/2, this));
		subcells.add(4, new Cell3<E>(minDim, x, y, z+l/2, l/2, this));
		subcells.add(5, new Cell3<E>(minDim, x+l/2, y, z+l/2, l/2, this));
		subcells.add(6, new Cell3<E>(minDim, x, y+l/2, z+l/2, l/2, this));
		subcells.add(7, new Cell3<E>(minDim, x+l/2, y+l/2, z+l/2, l/2, this));
		while(contents.size() > 0){
			E e = contents.removeFirst();
			subcells.get(calculateCell(e)).add(e);
		}
	}
	private boolean splitNecessary(E e){
		if(contents.size() == 0){
			return false;
		}else if(contents.size() == 1){
			return !contents.contains(e);
		}else{
			return true;
		}
	}
	private int calculateCell(E e){
		int x = (int) (2*(e.getX() - this.x)/l);
		int y = (int) (2*(e.getY() - this.y)/l);
		int z = (int) (2*(e.getZ() - this.z)/l);
		return x + y * 2 + z * 4;
	}
}