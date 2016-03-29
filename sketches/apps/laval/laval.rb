# -*- coding: utf-8 -*-


## Linux drivers Blueman
## Bluetooth remote https://weblog.sh/~maarten/unified-remote-with-bluetooth-on-arch-4JFboT57g
## sudo modprobe btusb
## sudo blueman-applet
## son avec pavucontrol

require 'jruby_art'
require 'jruby_art/app'

Processing::App::SKETCH_PATH = __FILE__
Processing::App::load_library :PapARt, :javacv, :toxiclibscore, :skatolo, :video

module Papartlib
  include_package 'fr.inria.papart.procam'
  include_package 'fr.inria.papart.procam.camera'
  include_package 'fr.inria.papart.multitouch'
  include_package 'fr.inria.papart.drawingapp'
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
  attr_reader :lego_house, :garden

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


    # @color_screen = MyColorPicker.new
    #    @cinema = Cinema.new

    @lego_house = LegoHouse.new
    @house_control = HouseControl.new
    @garden = Garden.new
    @papart.startTracking

    @projector = @papart.getDisplay
    @projector.manualMode
  end

  def draw
    noCursor
    noStroke

    background 0
    imageMode Processing::PConstants::CENTER

#     @projector.clear

    # @projector.drawScreens
    @projector.drawScreensOver
     Papartlib::DrawUtils::drawImage(self.g,
                                     @projector.render, width/2, height/2, width, height)
     #p "draw hous"
    # Papartlib::DrawUtils::drawImage(self.g,
    #                                 @projector.render, 0, 0, width, height)

#     p $screenPos_many.size

     fill 0
     return unless defined? $screenPos_many
     $screenPos_many.each do |id|
       next if id == nil
       id.each do |pos|
         rect(pos.x, pos.y, 16, 16)
         #       p pos.to_s
       end
     end


  end

  def key_pressed (arg = nil)
    @save_house_location = true if key == 'h'
    @load_house_location = true if key == 'H'
    @move_house_location = true if key == 'm'

    # Key to control the house
    if key == 'a'
      @lego_house.mode = LegoHouse::FIRST_FLOOR_LIGHT
    end

    if key == 'g'
      @garden.reset_grass
    end

    LegoHouse.modes.each do |mode|
      @lego_house.mode = mode if key == mode.to_s
    end

    if key == '+'
      @lego_house.volume_up
    end

    if key == '-'
      @lego_house.volume_down
    end

    # if key == LegoHouse::FIRST_FLOOR_LIGHT.to_s
    #   @lego_house.mode = LegoHouse::FIRST_FLOOR_LIGHT_TOUCH
    # end

  end


end



Sketch.new unless defined? $app
