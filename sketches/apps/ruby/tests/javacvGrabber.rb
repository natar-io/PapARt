require 'ruby-processing' 
Processing::App::SKETCH_PATH = __FILE__  unless defined? Processing::App::SKETCH_PATH




module Papartlib
  include_package 'fr.inria.papart.kinect'
  include_package 'fr.inria.papart.multitouchKinect'  ## for touchInput 
  include_package 'fr.inria.papart.procam'
end

class Sketch < Processing::App

  load_library :ProCam

  java_import 'org.bytedeco.javacv.FFmpegFrameGrabber'
  java_import 'fr.inria.papart.procam.Utils'

  def setup()
    size(1024, 768, OPENGL)
    
    x = 0
    y = 0
    w = 1024
    h = 768
    @grabber = JavaCV::FFmpegFrameGrabber(":0.0+" + x.to_s + "," + y.to_s)
    @grabber.setFormat("x11grab")
    @grabber.setImageWidth(w)
    @grabber.setImageHeight(h)
    @grabber.start();

    @cap_image = createImage(w, h, RGB);

  end

  def draw()

    background 0

    begin 
      img = @grabber.grab
      Utils.IplImageToPImage(img, false, @cap_image)
      image(@cap_image, 0, 0)
    rescue 
      puts "error"
    end

  end 
end

