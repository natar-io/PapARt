# -*- coding: utf-8 -*-

require 'jruby_art'
require 'jruby_art/app'

Processing::App::SKETCH_PATH = __FILE__
Processing::App::load_library :PapARt, :javacv, :toxiclibscore, :skatolo

module Papartlib
  include_package 'fr.inria.papart.procam'
end

class Sketch < Processing::App

  attr_reader :papart, :paper_screen

  def settings
    @use_projector = true
    fullScreen(P3D) if @use_projector
    size(300, 300, P3D) if not @use_projector
  end

  def setup
    if @use_projector
      @papart = Papartlib::Papart.projection(self)
#      @papart.loadTouchInput()
    else
      @papart = Papartlib::Papart.seeThrough self
    end

    @paper_screen = MyPaperScreen.new
    @papart.startTracking

    def draw

    end
  end
end




class MyPaperScreen < Papartlib::PaperScreen

  def settings
    setDrawingSize 297, 210
    loadMarkerBoard(Papartlib::Papart::markerFolder + "A3-small1.svg", 297, 210)
    setDrawAroundPaper
  end

  def setup
  end

  def drawAroundPaper
    setLocation 0, 0 , 0
    background 100, 100
#    fill 0, 100, 0
    # rect(0, 0, drawingSize.x / 2, drawingSize.y/2)
    # fill 200, 100, 20

    translate(100, 100);

    fill 255
    strokeWeight 1
    stroke 0, 200, 0

    translate 0, 0, 0
    small_brick(6, 8)
    move 6, 2
    small_brick(8, 6)
    move -6, -2
    move_up_small 1

    move 0, 6

    fill(200, 0, 0)
    brick 14, 2
    move_up 1
    brick 14, 2

    # text "Hello", 100, 100

    noStroke





  end

  def lego_size ; 8 ; end
  def lego_small_thickness ; 3.33 ; end
  def lego_thickness ; 10 ; end

  def move_up_small height
    translate(0, 0, -height * lego_small_thickness)
  end

  def move_up height
    translate(0, 0, -height * lego_thickness)
  end


  def move(w, h)
    translate(w * lego_size, h*lego_size)
  end

  def small_brick(w, h)
    pushMatrix

    brick_w = w* lego_size
    brick_h = h* lego_size

    translate(brick_w / 2, brick_h / 2, -lego_small_thickness/2)

    box(brick_w, brick_h, lego_small_thickness)
    popMatrix
  end

  def brick(w, h)
    pushMatrix

    brick_w = w* lego_size
    brick_h = h* lego_size

    translate(brick_w / 2, brick_h / 2, -lego_thickness/2 )

    box(brick_w, brick_h, lego_thickness )
    popMatrix
  end


end

Sketch.new unless defined? $app
