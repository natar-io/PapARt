# -*- coding: utf-8 -*-

require 'jruby_art'
require 'jruby_art/app'

Processing::App::SKETCH_PATH = __FILE__
Processing::App::load_library :PapARt, :javacv, :toxiclibscore, :skatolo, :video

module Papartlib
  include_package 'fr.inria.papart.procam'
  include_package 'fr.inria.papart.procam.camera'
  include_package 'fr.inria.papart.multitouch'
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
require_relative 'houseControl'

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
    # @color_screen = MyColorPicker.new
    @garden = Garden.new
#    @cinema = Cinema.new
    @house_control = HouseControl.new
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



Sketch.new unless defined? $app
