/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.papart.multitouch;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
interface TouchProvider {
    public boolean hasTouch();
    public void createTouch();
    public Touch getTouch();
    public void deleteTouch();
}
