/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.depthcam.devices;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public interface DepthCameraDevice {
    
    public int colorWidth();
    public int colorHeight();
    public int colorSize();
    
    public int depthWidth();
    public int depthHeight();
    public int depthSize();
}
