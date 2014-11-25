# -*- coding: utf-8 -*-
require 'ruby-processing' 

Processing::App::SKETCH_PATH = __FILE__

Processing::App::load_library  :ProCam


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

    @moon = Moon.new

    @papart.startTracking

  end

  def draw 
  end 
end




class Moon < Papartlib::PaperTouchScreen

  def setup
    setDrawingSize(297, 210);
    loadMarkerBoard($app.sketchPath("") + "/data/A3-small1.cfg", 297, 210);
  end

  def draw
    setLocation(0, 0, 0)
    pg = beginDraw2D
    pg.background 20, 200, 200
    pg.endDraw
  end
end

Sketch.new unless defined? $app
