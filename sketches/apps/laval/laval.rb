# -*- coding: utf-8 -*-

require 'jruby_art'
require 'jruby_art/app'

Processing::App::SKETCH_PATH = __FILE__
Processing::App::load_library :PapARt, :javacv, :toxiclibscore, :skatolo

module Papartlib
  include_package 'fr.inria.papart.procam'
  include_package 'fr.inria.papart.procam.camera'
end

module Processing
  include_package 'processing.opengl'
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
      @papart.loadTouchInput()
    else
      @papart = Papartlib::Papart.seeThrough self
    end

    @lego_house = LegoHouse.new
    @color_screen = MyColorPicker.new
    @garden = Garden.new
    @papart.startTracking

    def draw

    end
  end
end


module Lego
  def lego_size ; 8 ; end
  def lego_small_thickness ; 3.33 ; end
  def lego_thickness ; 10 ; end
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

class Garden < Papartlib::PaperTouchScreen
  include Lego

  def settings
    setDrawingSize 297, 210
    loadMarkerBoard($app.sketchPath + "/garden.svg", 297, 210)
  end

  def setup
    @kinect_projector = $app.papart.loadCalibration Papartlib::Papart::kinectTrackingCalib
    @kinect_projector.invert


  end

  def drawOnPaper
    background 0, 0, 0

    # pimage = cameraTracking.getPImage
    # image(pimage, 0, 0, 100, 100)
    # pimage.loadPixels
    # p pimage.pixels[100]

    vector = Processing::PVector.new


    pushMatrix
    touchList.get3DTouchs.each do |touch|
      next if touch.touchPoint == nil


      touch_heights = touch.touchPoint.getDepthDataElements.map do |dde|
        table_point = dde.projectedPoint.z
      end

      min, max = touch_heights.minmax

      next if max > 100
      # duplo height : 19mm

      ## With this, blue is ~ 220 degrees
      colorMode Processing::PConstants::RGB, 255

      if max > 55 && min < 10
         # p minmax[1]


        translate 0, 10
        pushMatrix
        x = 0
        touch_pixels = touch.touchPoint.getDepthDataElements.map do |dde|
          @kinect_projector.mult(Processing::PVector.new(dde.depthPoint.x,
                                                         dde.depthPoint.y,
                                                         dde.depthPoint.z),
                                 vector)
          c = getColorFrom3D vector
          fill(red(c), green(c), blue(c))
          # rect 0, 0, 10, 10
          x = x +10
          translate 10, 0

          if x > 300
            x = 0
            translate -300, 10
          end
          c
        end

        colorMode Processing::PConstants::HSB, 360
        hues = touch_pixels.map do |c|
          hue(c)
        end

        length = hues.size
        hues = hues.sort.slice(length / 3, length /3)
        mean = hues.reduce(:+) / hues.size

#        p mean
        # p touch_pixels
        popMatrix

        # large blue
        if(mean > 190 && min < 230)
          ellipse touch.position.x, touch.position.y, 20, 20
        end

      end
    end
    popMatrix

#    drawTouch 10
  end

end



class LegoHouse < Papartlib::PaperScreen
  include Lego

  def settings
    setDrawingSize 297, 210
    # loadMarkerBoard(Papartlib::Papart::markerFolder + "A3-small1.svg", 297, 210)
    loadMarkerBoard($app.sketchPath + "/house.svg", 297, 210)
    setDrawAroundPaper
  end

  def setup
  end

  def drawAroundPaper
    setLocation 0, 0 , 0
    # background 100, 100
#    fill 0, 100, 0
    # rect(0, 0, drawingSize.x / 2, drawingSize.y/2)
    # fill 200, 100, 20
    pointLight 200, 200, 200, $app.mouse_x, 400 -  $app.mouse_y, 100
    pushMatrix
    translate 0, 0, -80
    rect($app.mouse_x, 400 - $app.mouse_y, 10, 10)
    popMatrix

    translate(100, 100);

    fill 255
    strokeWeight 1
    stroke 0, 200, 0

    # fist floor
    translate 0, 0, 0
    small_brick(6, 8)
    move 6, 2
    small_brick(8, 6)
    move -6, -2

    ## walls between 1 and 2nd floor
    move_up_small 1

    fill(0, 255, 0)

    floor_image


    move 1, 1
    floor_image
    move -1, -1


    move 0, 6

    fill(200, 0, 0)
    brick 14, 2
    move_up 1
    brick 14, 2

    # text "Hello", 100, 100

    noStroke

  end

  def floor_image
    #rect(0, 0, lego_size, lego_size)
    if $floor != nil
      image($floor, 0, 0,lego_size*2, lego_size*2)
    end
  end


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
