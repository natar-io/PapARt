/**
 * diewald_PS3 - Processing Library.
 * 
 * this processing-library provides access to sonys PS3eye camera.
 * 
 * its basically a wrapper of the ps3-library from CodeLaboratories.
 * http://codelaboratories.com/products/eye/driver/
 * http://codelaboratories.com/products/eye/sdk/
 * 
 * 
 * Copyright (c) 2011 Thomas Diewald
 *
 *
 * This source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * A copy of the GNU General Public License is available on the World
 * Wide Web at <http://www.gnu.org/copyleft/gpl.html>. You can also
 * obtain it by writing to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */


package diewald_PS3;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

import diewald_PS3.constants.LIBRARY;
import diewald_PS3.logger.PS3Logger;

/**
 * PS3_Library handles the correct loading and method calls of the the native dll.<br>
 * <br>
 * 
 * @author thomas diewald (c) 2011
 *
 */
class PS3_Library {
  
  private static String CLEyeMulticam_windows_ = "CLEyeMulticam.dll";
  private static String jar_path_ = "";
  private static CLEyeMulticam CLEYE_ = null;
  

  // STATIC
  static{ 
    Native.setProtected(true);
//    boolean info_state  = KinectLogger.TYPE.INFO.isActive();
//    KinectLogger.TYPE.INFO.active(true);
    
    System.out.println(LIBRARY.LABEL.getValue() );
    PS3Logger.log(PS3Logger.TYPE.INFO, null, 
        "os.name              = " + System.getProperty("os.name"),
        "sun.arch.data.model  = " + System.getProperty("sun.arch.data.model") +" bit",
        "java.runtime.version = " + System.getProperty("java.runtime.version" ),
        "java.home            = " + System.getProperty("java.home" )
    );
    
    if(Platform.isWindows()){
      loadLibrary( getCLEyeMulticamForWindows(), CLEyeMulticam_windows_); 
    }   
    
    if(Platform.isLinux()){
      PS3Logger.log(PS3Logger.TYPE.ERROR, null, "FAILED: diewald_PS3 currently doesnt run under Linux");
    }  
    if(Platform.isMac()){
      PS3Logger.log(PS3Logger.TYPE.ERROR, null, "FAILED: diewald_PS3 currently doesnt run under MacOs");
    }    
    
    // doesnt seem to be necessary
//  if( jar_path_.length() != 0){
//    System.setProperty("jna.library.path", jar_path_);
//  }
//  KinectLogger.TYPE.INFO.active(info_state);
//    KinectLogger.TYPE.INFO.active(info_state);
  }
  
  private static final String getCLEyeMulticamForWindows(){
    String path = "";
    try {
      URI uri_ = new URI( PS3_Library.class.getProtectionDomain().getCodeSource().getLocation().getPath() );
      jar_path_ = new File(uri_.getPath()).getParent();
    } catch (URISyntaxException e) {
      return path;
    }
     
    // case 1: default CLEyeMulticam.dll path
    path = jar_path_ + "/windows" + System.getProperty("sun.arch.data.model") + "/";
    if( new File(path + CLEyeMulticam_windows_).exists() ) return path;
    
    // case 2: application CLEyeMulticam.dll path - version 1
    path = jar_path_+"/";
    if( new File(path + CLEyeMulticam_windows_).exists() ) return path;
    
    // case 3: application CLEyeMulticam.dll path - version 2
    path = new File(jar_path_).getParentFile().getPath() + "/";
    if( new File(path + CLEyeMulticam_windows_).exists() ) return path;
    
    return path;
  }
  
  
  
  
  
  ////----------------------------------------------------------------------------
  ////--------------------------- LOAD LIBRARY -----------------------------------
  ////----------------------------------------------------------------------------
  protected static final void loadLibrary(String CLEyeMulticam_path , String CLEyeMulticam_name)  { 
    try {
      CLEyeMulticam CLEYE_TMP = (CLEyeMulticam) Native.loadLibrary(  CLEyeMulticam_path+CLEyeMulticam_name,  CLEyeMulticam.class);
      CLEYE_ = CLEYE_TMP;
      
      PS3Logger.log(PS3Logger.TYPE.INFO, null, 
          "LIBRARY LOADED", 
          "path = \""+CLEyeMulticam_path+CLEyeMulticam_name+"\"" 
      );
    } catch ( UnsatisfiedLinkError e ){
      if( CLEYE_ != null )  
        return;   // library was loaded previously
      boolean error_state = PS3Logger.TYPE.ERROR.isActive();
      PS3Logger.TYPE.ERROR.active(true);
      PS3Logger.log(PS3Logger.TYPE.ERROR, null,  
          "Unable to load library : "+CLEyeMulticam_name, 
          "path = \""+CLEyeMulticam_path+CLEyeMulticam_name+"\"", "try 'MyKinect.loadLibrary( \"your dll path/\", \"CLEyeMulticam.dll\" )'");
      PS3Logger.TYPE.ERROR.active(error_state);
    }
  }
  
  
 
  // GET LIBRARY INSTANCE 
  protected static final CLEyeMulticam get(){
    return CLEYE_;
  }
  
  //LIBRARY LOADED true/false
  protected static final boolean loaded(){
    return ((CLEYE_ == null) ? false : true);
  }
  
  

  
  ////----------------------------------------------------------------------------  
  ////----------------------------------------------------------------------------
  ////---------------------- NATIVE FUNCTIONS OF DLL-- ---------------------------
  ////----------------------------------------------------------------------------
  ////----------------------------------------------------------------------------
  protected interface CLEyeMulticam extends Library {
    
    abstract GUID CLEyeGetCameraUUID(final int idx);
    
    abstract int    CLEyeCameraGetFrame           (Camera camera, ByteBuffer pixels, int waiting_time);
    abstract int    CLEyeCameraGetFrameDimensions (Camera camera, Dimension width, Dimension height);
    abstract int    CLEyeCameraLED	              (Camera camera, int on_off);
    abstract int    CLEyeCameraStart	            (Camera camera);
    abstract int    CLEyeCameraStop	              (Camera camera);
    abstract Camera CLEyeCreateCamera             (GUID guid, int mode, int resolution, float framerate);
    abstract int    CLEyeDestroyCamera	          (Camera camera);
    abstract int    CLEyeGetCameraCount           ();
                                                  
    abstract int    CLEyeGetCameraParameter	      (Camera camera, int param);
    abstract int    CLEyeSetCameraParameter       (Camera camera, int param, int value);
    
  }
  
  protected static class GUID extends Structure implements Structure.ByValue{
    public int   Data1 ;
    public short Data2 ;
    public short Data3 ;
    public byte  Data4[] = new byte[8];
 
    public GUID() { }
  }
  

  protected static class Camera extends PointerType{
    public Camera(){}
    protected Camera( Pointer ptr ){
      super(ptr);
    }
  }
  
  protected static class Dimension extends IntByReference{
    public Dimension(){}
  }
  
}
