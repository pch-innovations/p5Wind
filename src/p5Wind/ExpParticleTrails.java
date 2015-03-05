package p5Wind;


import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import toxi.geom.AxisAlignedCylinder;
import toxi.geom.LineStrip3D;
import toxi.geom.Plane;
import toxi.geom.Vec3D;
import toxi.physics3d.VerletParticle3D;
import toxi.physics3d.VerletPhysics3D;
import toxi.physics3d.behaviors.AttractionBehavior3D;
import toxi.physics3d.behaviors.ConstantForceBehavior3D;
import toxi.physics3d.behaviors.GravityBehavior3D;
import toxi.physics3d.constraints.CylinderConstraint;
import toxi.physics3d.constraints.MaxConstraint;
import toxi.physics3d.constraints.SphereConstraint;
import toxi.processing.ToxiclibsSupport;

public class ExpParticleTrails extends PApplet {

	public static void main(String args[]) {
		PApplet.main(new String[] { "--present", "ExpParticleTrails" });
	}

	ToxiclibsSupport gfx;
	int frameCount;

	int NUM_PARTICLES = 5;
	int DIFFUSION = 50;
	int TRAIL_LENGTH = 5;
	int MAX_AGE = 100;
	boolean showConstraints = false; // C
	boolean showTrails = true; // T
	boolean recording = false; // R

	VerletPhysics3D physics;
	List<ParticleTrail> trails = new ArrayList<ParticleTrail>();

	SphereConstraint sphere1, sphere2;
	CylinderConstraint cylinder;
	MaxConstraint floor;
	AttractionBehavior3D magnet;
	ConstantForceBehavior3D wind;
	GravityBehavior3D gravity;

	public void setup() {
		size(1024, 576, P3D);
		gfx = new ToxiclibsSupport(this);
		initPhysics();
	}
	
	public void initPhysics() {
		physics = new VerletPhysics3D();// Vec3D.Y_AXIS.scale(0.1f), 50, 0, 1);

		sphere1 = new SphereConstraint(new Vec3D(0, 0, 0), 100, false);
		//cylinder = new CylinderConstraint(new XAxisCylinder(Vec3D.ZERO, 100f, 200f));
		//sphere2 = new SphereConstraint(new Vec3D(100, 0, 0), 100, false);
		floor = new MaxConstraint(Vec3D.Axis.Y, 0.0f);
		
		magnet = new AttractionBehavior3D(new VerletParticle3D(50, -20, 0), 200, 150, 0.5f);
		//magnet = new AttractionBehavior3D(new VerletParticle3D(200, -20, 0), 500, 150, 1);
		physics.addBehavior(magnet);
		wind = new ConstantForceBehavior3D(new Vec3D(30, 0, 0));
		physics.addBehavior(wind);
		gravity = new GravityBehavior3D(new Vec3D(0, 1f, 0));
		physics.addBehavior(gravity);
		
	}

	public void updatePhysics() {
		Vec3D startPos = new Vec3D(-300, -50, 0);
		for (int i = 0; i < NUM_PARTICLES; i++) {
			VerletParticle3D p = new VerletParticle3D(
					random(startPos.x - DIFFUSION * 10, startPos.x + DIFFUSION),
					random(startPos.y - DIFFUSION, startPos.y + DIFFUSION),
					random(startPos.z - DIFFUSION, startPos.z + DIFFUSION), 
					0.01f);
			physics.addParticle(p);
			p.addConstraint(sphere1);
			//p.addConstraint(cylinder);
			//p.addConstraint(sphere2);
			p.addConstraint(floor);

			ParticleTrail t = new ParticleTrail(p);
			trails.add(t);
		}
		physics.update();

		// remove aged particles and their trails
		List<ParticleTrail> agedTrails = new ArrayList<ParticleTrail>();
		for (ParticleTrail t : trails) {
			t.update();
			if (t.getAge() > MAX_AGE) {
				agedTrails.add(t);
			}
		}
		for (ParticleTrail t: agedTrails) {
			physics.removeParticle(t.head);
			trails.remove(t);
		}
		
	}

	public void draw() {
		updatePhysics();
		frameCount++;

		background(0);
		lights();
		noStroke();

	    
	    // weird origin displacement..
		translate(width / 2, height / 2, -50);
		rotateX(-0.33f);
		rotateY(frameCount/100f);
	    
		// constraints
		if (showConstraints) {
		    gfx.origin(new Vec3D(), 100);
			noFill();
			stroke(100);
			gfx.sphere(sphere1.sphere, 10, true);
			//gfx.cylinder(cylinder.getCylinder(), 10, true);
			//gfx.sphere(sphere2.sphere, 10, true);
			stroke(255, 0, 0);
			strokeWeight(5);
			gfx.point(magnet.getAttractor());
		}

		// floor
		fill(60);
		noStroke();
		gfx.plane(Plane.XZ, 1000);

		// particles
		fill(0);
		stroke(255);

		if (showTrails) {
			strokeWeight(2);
			for (ParticleTrail t : trails) {
				gfx.lineStrip3D(t.getTrail());
			}
		} else {
			strokeWeight(4);
			for (VerletParticle3D p : physics.particles) {
				point(p.x, p.y, p.z);
			}
		}
		

		if (recording) {
			saveFrame("frames/######.tga");
			/* 
			 * compile video like this:
			 *    ffmpeg -f image2 -framerate 60 -i %06d.tga -vcodec mpeg4 -r 30 output.mp4
			 *    
			 */
		}
	}
	

	public void keyPressed() {

		if (key == 'c') {
			showConstraints = !showConstraints;
		}
		if (key == 't') {
			showTrails = !showTrails;
		}
		if (key == 'r') {
			recording = !recording;
		}
		
		// for playing around with magnet attractor vs. wind speed:
		
		if (key == '1') {
			magnet.setRadius(magnet.getRadius() + 10);
			println("magnet radius: " + magnet.getRadius());
		}
		if (key == '2') {
			magnet.setRadius(magnet.getRadius() - 10);
			println("magnet radius: " + magnet.getRadius());
		}
		if (key == '3') {
			magnet.setStrength(magnet.getStrength() + 10);
			println("magnet strength: " + magnet.getStrength());
		}
		if (key == '4') {
			magnet.setStrength(magnet.getStrength() - 10);
			println("magnet strength: " + magnet.getStrength());
		}
		if (key == '5') {
			magnet.getAttractor().setX(magnet.getAttractor().x + 10);
			println("magnet x: " + magnet.getAttractor().x);
		}
		if (key == '6') {
			magnet.getAttractor().setX(magnet.getAttractor().x - 10);
			println("magnet x: " + magnet.getAttractor().x);
		}
		if (key == '7') {
			physics.removeBehavior(wind);
			wind.getForce().setX(wind.getForce().x + 1);
			physics.addBehavior(wind);
			println("wind: ", wind.getForce().x);
		}
		if (key == '8') {
			physics.removeBehavior(wind);
			wind.getForce().setX(wind.getForce().x - 1);
			physics.addBehavior(wind);
			println("wind: ", wind.getForce().x);
		}
	}


	class ParticleTrail {

		VerletParticle3D head;
		int age = 0;
		LineStrip3D trail = new LineStrip3D();

		public ParticleTrail(VerletParticle3D head) {
			this.head = head;
		}

		public LineStrip3D getTrail() {
			return trail;
		}
		
		public int getAge() {
			return age;
		}

		public void update() {
			trail.add(head.getPreviousPosition().copy());
			if (trail.getVertices().size() > TRAIL_LENGTH) {
				trail.getVertices().remove(0);
			}
			age++;
		}

	}


}

