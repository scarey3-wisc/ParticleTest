public class Vec3{
	private double[] vals;
	public Vec3(double[] input){
		vals = input;
	}
	public Vec3(double x, double y, double z){
		vals = new double[]{x, y, z};
	}
	public Vec3(){
		vals = new double[]{0,0,0};
	}
	public Vec3(Vec3 copy){
		vals = copy.vals.clone();
	}
	public void setValues(double[] input){
		vals = input;
	}
	public void increment(Vec3 add){
		for(int i = 0; i < vals.length; i++){
			vals[i] += add.vals[i];
		}
	}
	public void scale(double mult){
		for(int i = 0; i < vals.length; i++){
			vals[i] *= mult;
		}
	}
	public void negate(){
		for(int i = 0; i < vals.length; i++){
			vals[i] *= -1;
		}
	}
	public double getMagSqr(){
		double total = 0;
		for(double d: vals){
			total += d*d;
		}
		return total;
	}
	public double getMagnitude(){
		return Math.sqrt(getMagSqr());
	}
	public void normalize(){
		scale(1/getMagnitude());
	}
	public Vec3 clone(){
		Vec3 nova = new Vec3();
		for(int i = 0; i < nova.vals.length; i++){
			nova.vals[i] = vals[i];
		}
		return nova;
	}
	public double getX(){
		return vals[0];
	}
	public void setX(double x){
		vals[0] = x;
	}
	public double getY(){
		return vals[1];
	}
	public void setY(double y){
		vals[1] = y;
	}
	public double getZ(){
		return vals[2];
	}
	public void setZ(double z){
		vals[2] = z;
	}
	public double[] getVals(){
		return vals;
	}
	public Vec3 getUnitVector(){
		Vec3 c = clone();
		c.normalize();
		return c;
	}
	public static Vec3 add(Vec3 one, Vec3 two){
		Vec3 nova = new Vec3();
		for(int i = 0; i < nova.vals.length; i++){
			nova.vals[i] += one.vals[i] + two.vals[i];
		}
		return nova;
	}
	public static Vec3 negative(Vec3 one){
		Vec3 nova = new Vec3();
		for(int i = 0; i < nova.vals.length; i++){
			nova.vals[i] -= one.vals[i];
		}
		return nova;
	}
	public static Vec3 mult(Vec3 one, double a){
		Vec3 nova = new Vec3();
		for(int i = 0; i < nova.vals.length; i++){
			nova.vals[i] += one.vals[i]*a;
		}
		return nova;
	}
	public static double dot(Vec3 one, Vec3 two){
		double total = 0;
		for(int i = 0; i < one.vals.length; i++){
			total += one.vals[i]*two.vals[i];
		}
		return total;
	}
	public static Vec3 cross(Vec3 one, Vec3 two){
		double i = one.vals[1] * two.vals[2] - one.vals[2] * two.vals[1];
		double j = one.vals[2] * two.vals[0] - one.vals[0] * two.vals[2];
		double k = one.vals[0] * two.vals[1] - one.vals[1] * two.vals[0];
		return new Vec3(i, j, k);
	}
}