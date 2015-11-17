# -*- coding: utf-8 -*-
require 'ruby-processing' 
Processing::App::SKETCH_PATH = __FILE__

module Papartlib
  include_package 'fr.inria.papart.kinect'
  include_package 'fr.inria.papart.multitouchKinect'  ## for touchInput 
  include_package 'fr.inria.papart.procam'
end

$scale = 3

class Sketch < Processing::App

  load_library :bytedeco, :ProCam

  attr_reader :camera_tracking, :display, :papart, :moon
  include Papartlib


## Undecorated frame 
  # def init
  #   frame.removeNotify
  #   frame.setUndecorated true
  #   frame.addNotify
  #   super.init
  # end

  def setup 

    # frame.removeNotify
    # frame.setUndecorated true
    # frame.addNotify

    frameSizeX = 1280
    frameSizeY = 800

    @camera_x = 640
    @camera_y = 480
    
    @useProjector = true
    
    if @useProjector
      size(frameSizeX, frameSizeY, OPENGL)
      @papart = Papartlib::Papart.new self
      @papart.initProjectorCamera(1, "0",
                                  Papartlib::Camera::OPENCV_VIDEO);
      @papart.loadTouchInput(2, 5);
    else 
      size(@camera_x, @camera_y, OPENGL)
      @papart = Papartlib::Papart.new self
      @papart.initKinectCamera(2)
      @papart.loadTouchInputKinectOnly(2, 5)
    end



    @touch_input = @papart.getTouchInput
    @display = @papart.getDisplay
    @camera_tracking = @papart.getCameraTracking

    boardSize = PVector.new(280,  160)
    boardResolution = 3
    markerFile = sketchPath("") + "/data/markers/moon/moon.cfg";
    markerBoard =  Papartlib::MarkerBoard.new(sketchPath("") + "/data/markers/moon/moon.cfg",
                                           "Moon Board",
                                           boardSize.x, boardSize.y);

    @moon = Moon.new(self, markerBoard, boardSize, boardResolution,
                     @camera_tracking, @display, @touch_input);
    @camera_tracking.trackSheets(true);

    frame.setSize(frameSizeX, frameSizeY);
  end

  def draw 
  end 

  def sketchWidth ; 1280 ; end 
  def sketchHeight ; 1080 ; end 

  def sketchFullScreen 
    true 
  end

  def key_pressed



#    frame.dispose();

    # frame.setUndecorated(false);
    # frame.pack();
    # frame.setLocationRelativeTo(nil);
    
    # frame.setVisible(true);

  # frame.removeNotify
  #   frame.setUndecorated(true)
  #   frame.setVisible(true);
  #   frame.addNotify
  end

end


java_import 'fr.inria.papart.exceptions.BoardNotDetectedException'

class Moon < Papartlib::PaperTouchScreen 

java_import 'java.awt.Robot'
java_import 'java.awt.Rectangle'
java_import 'java.awt.image.WritableRaster'
java_import 'java.awt.event.InputEvent'

# Use processing functions like in a sketch.
  include Processing::Proxy

  def initialize (parent, board, size, resolution,
                  camera, display, touchInput)
    super(parent, board, size, resolution,
          camera, display, touchInput)

    @robot = Robot.new
    @rectangle = Rectangle.new(1948, 78, 1024, 600)

    im = @robot.create_screen_capture @rectangle
    @image = PImage.new(im)

  end

  def draw
    begin 

      setLocation(100, 0, 0)
      pg = beginDraw2D
      
      pg.background 0
      bi = @robot.create_screen_capture @rectangle
      
      bi_width = bi.getWidth
      bi_height = bi.getHeight
      
      @image.loadPixels
      raster = bi.getRaster
      raster.getDataElements(0, 0, bi_width, bi_height, @image.pixels);
      @image.updatePixels

      touch_list = get_touch_list
      drawing_size = get_drawing_size
      resolution = get_resolution

      
      # if @image 
      pg.image(@image, 0, 0, drawing_size.x,   drawing_size.y)
               # @rectangle.width,
               # @rectangle.height)
      # end

#      puts touchList.isEmpty
      #      $touchList = touchList
      
      #    binding.pry      

      unless touch_list.isEmpty 
        touch_list.to_a.each do |touch| 
          next if touch.is3D
          p = touch.p 
          puts p 

          x = @rectangle.x + p.x * drawing_size.x * resolution
          y = @rectangle.y + p.y * drawing_size.y * resolution

          # @robot.mouse_move x, y
          # @robot.mousePress InputEvent::BUTTON1_MASK
          # @robot.delay(100)
          # @robot.mouseRelease InputEvent::BUTTON1_MASK

        end
      end


      pg.endDraw
    rescue 
      puts "Error"
    end
  end

end
