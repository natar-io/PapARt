/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.depthcam;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class TouchAttributes {

    public static TouchAttributes NO_ATTRIBUTES = new TouchAttributes(false, false, false);
    
    protected boolean isInTouch;
    protected boolean isUnderTouch;
    protected boolean isOverTouch;

    public TouchAttributes(boolean inTouch, boolean underTouch, boolean overTouch){
        this.isInTouch = inTouch;
        this.isUnderTouch = underTouch;
        this.isOverTouch = overTouch;
    }
    
    public boolean isInTouch() {
        return isInTouch;
    }

    public void setIsInTouch(boolean isInTouch) {
        this.isInTouch = isInTouch;
    }

    public boolean isUnderTouch() {
        return isUnderTouch;
    }

    public void setIsUnderTouch(boolean isUnderTouch) {
        this.isUnderTouch = isUnderTouch;
    }

    public boolean isOverTouch() {
        return isOverTouch;
    }

    public void setIsOverTouch(boolean isOverTouch) {
        this.isOverTouch = isOverTouch;
    }

}
