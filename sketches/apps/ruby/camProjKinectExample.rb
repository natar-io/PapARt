# -*- coding: utf-8 -*-

require 'jruby_art'
require 'jruby_art/app'

Processing::App::SKETCH_PATH = __FILE__

# require 'ruby-processing'
# Processing::Runner
# Dir["#{Processing::RP_CONFIG['PROCESSING_ROOT']}/core/library/\*.jar"].each{ |jar| require jar }
# Processing::App::SKETCH_PATH = __FILE__

Processing::App::load_library :PapARt, :javacv, :toxiclibscore

module Papartlib
  include_package 'fr.inria.papart.procam'
end

class Sketch < Processing::App

  attr_reader :camera_tracking, :display, :papart, :moon

  def setup

    frameSizeX = 1280
    frameSizeY = 800

    @camera_x = 640
    @camera_y = 480

    @useProjector = false

    if @useProjector

      @papart = PapartLib::Papart.projection(self)
      @papart.loadTouchInput()
    else

      @papart = Papartlib::Papart.seeThrough self
      @papart.loadTouchInput()

      # size(@camera_x, @camera_y, OPENGL)
      # @papart = Papartlib::Papart.new self
      # @papart.initKinectCamera(2)
      # @papart.loadTouchInputKinectOnly()
    end

    @moon = Moon.new

    @papart.startTracking


    def draw

    end
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
    pg.background 40, 200, 200
    pg.endDraw
  end
end

Sketch.new unless defined? $app
