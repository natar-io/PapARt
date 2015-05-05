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

	//boolean castle1 = o1.getClass() == Castle.class;
	//boolean castle2 = o2.getClass() == Castle.class;

	
	// TODO: hit ?
	// if(missile2 && castle1){
	    //	    hit(o1, o2);
	// }

	if(missile1 && missile2){
	    explode(o1, o2);
	}


	// if(missile1 && castle2){
	//     hit(o2, o1);
	// }

    	// If object 1 is a Box, then object 2 must be a particle
    	// Note we are ignoring particle on particle collisions

    }

    void explode(Object missileO1, Object missileO2){

	Missile missile0 = (Missile) missileO1;
	Missile missile1 = (Missile) missileO2;

	// if(missile0.faction == missile1.faction)
	//     return;

	if(missile1.level > missile0.level){
	    missile0.hit();
	} else {
	    if(missile1.level < missile0.level){
		missile1.hit();
	    }
	}
	
    }


//  void hit(Object castleO, Object missileO){
//	Castle castle = (Castle) castleO;
//	Missile missile = (Missile) missileO;
//
//	missile.hit();
//	castle.isHit();
//    }

     
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
