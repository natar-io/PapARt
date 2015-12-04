# -*- coding: utf-8 -*-

require 'jruby_art'
require 'jruby_art/app'

Processing::App::SKETCH_PATH = __FILE__
Processing::App::load_library :PapARt, :javacv, :toxiclibscore

module Papartlib
  include_package 'fr.inria.papart.procam'
end

class Sketch < Processing::App

  attr_reader :papart, :paper_screen

  @use_projector = false

  def settings
    fullScreen(P3D) if @use_projector
    size(300, 300, P3D) if not @use_projector
  end

  def setup
    if @use_projector
      @papart = PapartLib::Papart.projection(self)
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
#    loadMarkerBoard(Papartlib::Papart::markerFolder + "test.cfg", 297, 210)
    loadMarkerBoard(Papartlib::Papart::markerFolder + "A3-small1.cfg", 297, 210)
  end

  def setup

  end

  def set_board name
    board = Papartlib::MarkerBoardFactory.create(Papartlib::Papart::markerFolder + name, 297, 210)
    setMarkerBoard board
  end

  def drawOnPaper
    setLocation 61, -44.5 , 0
    background 100, 100
#    fill 0, 100, 0
    # rect(0, 0, drawingSize.x / 2, drawingSize.y/2)
    # fill 200, 100, 20
    # translate 0, 0, 1
    rect 10, 10, 100, 30
    text "Hello", 100, 100
  end
end

Sketch.new unless defined? $app
