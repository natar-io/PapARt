# -*- coding: utf-8 -*-

require 'jruby_art'
require 'jruby_art/app'

Processing::App::SKETCH_PATH = __FILE__
Processing::App::load_library :PapARt, :javacv, :toxiclibscore, :skatolo, :video

module Papartlib
  include_package 'fr.inria.papart.procam'
  include_package 'fr.inria.papart.procam.camera'
end

module Processing
  include_package 'processing.opengl'
  include_package 'processing.video'
  include_package 'processing.video.Movie'
  include_package 'org.gestreamer.elements'
end

# java_signature
require 'jruby/core_ext'

require_relative 'lego'
require_relative 'garden'
require_relative 'house'
require_relative 'video'

class Sketch < Processing::App

  attr_reader :papart, :paper_screen
  attr_accessor :save_house_location, :load_house_location, :move_house_location


  java_signature 'void movieEvent(processing.video.Movie)'
  def movieEvent(movie)
    movie.read
    p "Movie Event !"
  end

  def settings
    @use_projector = true
    fullScreen(P3D) if @use_projector
    size(300, 300, P3D) if not @use_projector
  end

  def setup


    if @use_projector
      @papart = Papartlib::Papart.projection(self)
      @papart.loadTouchInput()
    else
      @papart = Papartlib::Papart.seeThrough self
    end

    @lego_house = LegoHouse.new
    @color_screen = MyColorPicker.new
    @garden = Garden.new
    @cinema = Cinema.new
    @papart.startTracking
  end

  def draw

  end

  def key_pressed (arg = nil)
    @save_house_location = true if key == 'h'
    @load_house_location = true if key == 'H'
    @move_house_location = true if key == 'm'
  end
end



class MyColorPicker < Papartlib::PaperScreen
  include Lego

  def settings
    setDrawingSize 100, 100
    loadMarkerBoard($app.sketchPath + "/cap1.svg", 100, 100)

  end

  def setup

    @boardView = Papartlib::TrackedView.new self
    @boardView.setCaptureSizeMM Processing::PVector.new(lego_size*2, lego_size*2)

    picSize = 16
    @boardView.setImageWidthPx(picSize);
    @boardView.setImageHeightPx(picSize);

    origin = Processing::PVector.new 10, 55
    @boardView.setBottomLeftCorner(origin);
    @boardView.init
  end

  def drawOnPaper
    background 0, 0, 0

    fill 255
    noStroke
    rect(10-2, 100 - 55 - 20, 20, 20)

    out = @boardView.getViewOf cameraTracking
    # out.filter Processing::PConstants::INVERT
    image(out, 28, 25, 16, 16) if out != nil
    $floor = out if out != nil
  end

end




Sketch.new unless defined? $app
