# -*- coding: utf-8 -*-
require 'ruby-processing' 
Processing::App::SKETCH_PATH = __FILE__

module Papartlib
  include_package 'fr.inria.papart.kinect'
  include_package 'fr.inria.papart.multitouchKinect'  ## for touchInput 
  include_package 'fr.inria.papart.procam'
end


class Sketch < Processing::App

  load_library :ProCam

  include Papartlib

  def setup 

    frame.setResizable(true) unless frame == nil 

  end

  def draw 
    background 0


  end 

  def sketchWidth ; 800 ; end 
  def sketchHeight ; 600 ; end 
  def sketchRenderer; OPENGL; end

  def sketchFullScreen; false; end

  def key_pressed
  end

end

