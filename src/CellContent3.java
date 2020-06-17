public abstract class CellContent3<E extends CellContent3<E>> extends Vec3{
	private Cell3<E> container;
	public CellContent3(Vec3 input){
		super(input);
	}
	public CellContent3(double[] input){
		super(input);
	}
	public Cell3<E> getContainer(){
		return container;
	}
	public void setContainer(Cell3<E> input){
		this.container = input;
	}
}