public class Particle extends CellContent3<Particle>{
	Vec3 velocity;
	double mass;
	double radius;
	double rad;
	public Particle(double[] position, double[] velocity, double mass, double radius){
		super(position);
		this.velocity = new Vec3(velocity);
		this.mass = mass;
		this.radius = radius;
	}
	public Particle(Vec3 position, Vec3 velocity, double mass, double radius){
		super(position);
		this.velocity = velocity;
		this.mass = mass;
		this.radius = radius;
		rad = Math.random();
	}
	public void applyForce(double deltaT, Vec3 force){
		velocity.increment(Vec3.mult(force, deltaT/mass));
	}
	public double getRadius(){
		return radius;
	}
	public double getMass(){
		return mass;
	}
}