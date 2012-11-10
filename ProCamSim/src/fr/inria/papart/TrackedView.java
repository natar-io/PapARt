/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart;

/**
 *
 * @author jeremylaviole
 */
public class TrackedView {

    private MarkerBoard board;

    public TrackedView(MarkerBoard board, int outWidth, int outHeight) {
        this.board = board;
         // TODO: find Simulation ?
    }

    public MarkerBoard getBoard() {
        return this.board;
    }
}
