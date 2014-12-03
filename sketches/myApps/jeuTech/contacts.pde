// The Nature of Code
// <http://www.shiffman.net/teaching/nature>
// Spring 2010
// Box2DProcessing example

// ContactListener to listen for collisions!

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.contacts.Contact;

class CustomListener implements ContactListener {
    CustomListener() {
    }

    // This function is called when a new collision occurs
    void beginContact(Contact cp) {

	//	println("Contact");

    	// Get both fixtures
    	Fixture f1 = cp.getFixtureA();
    	Fixture f2 = cp.getFixtureB();
    	// Get both bodies
    	Body b1 = f1.getBody();
    	Body b2 = f2.getBody();
    	// Get our objects that reference these bodies
    	Object o1 = b1.getUserData();
    	Object o2 = b2.getUserData();


	boolean missile1 = o1.getClass() == Missile.class;
	boolean missile2 = o2.getClass() == Missile.class;

	boolean castle1 = o1.getClass() == Castle.class;
	boolean castle2 = o2.getClass() == Castle.class;
       

	if(missile1 && missile2 || !castle1 && !castle2)
	    return;

	if(missile1 || missile2){
	    if(missile1) {
		hit(o2, o1);
	    }	else {
		hit(o1, o2);
	    }
	}

    	// If object 1 is a Box, then object 2 must be a particle
    	// Note we are ignoring particle on particle collisions

    }

    void hit(Object castleO, Object missileO){
	Castle castle = (Castle) castleO;
	Missile missile = (Missile) missileO;

	// castle.isHit();
	// missile.die();
    }

     
    void endContact(Contact contact) {
	// TODO Auto-generated method stub
    }
     
    void preSolve(Contact contact, Manifold oldManifold) {
	// TODO Auto-generated method stub
    }
     
    void postSolve(Contact contact, ContactImpulse impulse) {
	// TODO Auto-generated method stub
    }
}
