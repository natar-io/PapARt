# -*- coding: utf-8 -*-
require 'ruby-processing' 
Processing::App::SKETCH_PATH = __FILE__ unless defined? Processing::App::SKETCH_PATH

module Papartlib
  include_package 'fr.inria.papart.kinect'
  include_package 'fr.inria.papart.multitouchKinect'  ## for touchInput 
  include_package 'fr.inria.papart.procam'
end


module JavaCV
  include_package 'org.bytedeco.javacpp.helper'
  include_package 'org.bytedeco.javacpp.opencv_core'
  include_package 'org.bytedeco.javacpp'
  include_package 'org.bytedeco.javacv'

end

class Sketch < Processing::App

  load_library :ProCam

  java_import ('org.bytedeco.javacpp.opencv_core'){ :OpencvCore }
  java_import ('org.bytedeco.javacpp.helper.opencv_highgui'){ :OpencvHighgui }
  IplImage = OpencvCore::IplImage  unless defined? :IplImage
  


  include Papartlib

  def setup 

    @papart = Papartlib::Papart.new self
    @papart.initCamera(1, "0", Papartlib::Camera::OPENCV_VIDEO)
    @camera = @papart.get_camera_tracking

    frame.setResizable(true) unless frame == nil 


# 516x491
    # @clock = OpencvHighgui::cvLoadImageBGRA("/home/jiii/sketchbook/data/clock_texture.jpg")
    # @cam_image = Papartlib::CamImage.new(self, 515, 491)
    # @cam_image.update(@clock)

  end

  def draw 
    background 0

   # @cam_image.update(@clock)
   # image(@cam_image, 0, 0)


#    image(@camera.getImage, 0, 0)

  end 



## Replace the size() instruction
  def sketchWidth ; 640 ; end 
  def sketchHeight ; 480 ; end 
  def sketchRenderer; OPENGL; end
  def sketchFullScreen; false; end

  def key_pressed
  end

end


Sketch.new unless defined? $app
