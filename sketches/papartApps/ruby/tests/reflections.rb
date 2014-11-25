# -*- coding: utf-8 -*-
require 'ruby-processing' 
 
Processing::App::SKETCH_PATH = __FILE__



Processing::App::load_library :bytedeco, :ProCam

## TODO: check: bundle 
## check: optionally add :require => 'overrides/for_all' to have overrides available in all classes/modules as a built-in (singleton) method.


module Papartlib
  include_package 'fr.inria.papart.procam'
end


class Sketch < Processing::App

#  load_library :bytedeco, :ProCam

  attr_reader :camera_tracking, :display, :papart, :moon

  def setup 

    frameSizeX = 1280
    frameSizeY = 800

    @camera_x = 640
    @camera_y = 480
    
    @useProjector = false
    
    if @useProjector
      size(frameSizeX, frameSizeY, OPENGL)
      @papart = Papartlib::Papart.new self
      @papart.initProjectorCamera(1, "0",
                                  Papartlib::Camera::Type::OPENCV);
      @papart.loadTouchInput(2, 5);
    else 
      size(@camera_x, @camera_y, OPENGL)
      @papart = Papartlib::Papart.new self
      @papart.initKinectCamera(2)
      @papart.loadTouchInputKinectOnly(2, 5)
    end

#    @moon = Moon.new

    @papart.startTracking

  end

  def draw 
  end 

  def sketchWidth ; 1280 ; end 
  def sketchHeight ; 1080 ; end 
  def sketchFullScreen ; false; end  

end


java_import 'fr.inria.papart.exceptions.BoardNotDetectedException'
require 'jruby/core_ext'

class Moon < Java::FrInriaPapartProcam::PaperTouchScreen
## class Moon < Papartlib::PaperTouchScreen 

  def initialize
    super
    drawingSize(297, 210);
    markerBoard($app.sketchPath("") + "/data/A3-small1.cfg", 297, 210);
  end

  def draw
    setLocation(0, 0, 0)
    pg = beginDraw2D
    pg.background 200, 30, 40
    pg.endDraw
  end
end

# Moon.become_java!




Sketch.new unless defined? $app
