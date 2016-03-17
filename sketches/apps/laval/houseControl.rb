# coding: utf-8
require_relative 'skatolo'

class HouseControl < Papartlib::PaperTouchScreen

  include_package 'fr.inria.skatolo'
  include_package 'fr.inria.skatolo.events'
  include_package 'fr.inria.skatolo.gui.controllers'
  include_package 'fr.inria.skatolo.gui.Pointer'

  ## For skatolo
  def create_method(name, &block)
    self.class.send(:define_method, name, &block)
  end

  def settings
    setDrawingSize 210, 297
    loadMarkerBoard($app.sketchPath + "/house-control.svg", 210, 297)
  end

  def setup
    create_buttons
    init_capture
  end

  def init_capture
    @boardView = Papartlib::TrackedView.new self
    @boardView.setCaptureSizeMM Processing::PVector.new(150, 100)

    picSize = 16
    @boardView.setImageWidthPx(110 * 2);
    @boardView.setImageHeightPx(60 * 2);

    origin = Processing::PVector.new 30, 170
    @boardView.setBottomLeftCorner(origin);
    @boardView.init
  end

  def create_buttons
    @skatolo = Skatolo.new $app, self

    @skatolo.getMousePointer.disable
    @skatolo.setAutoDraw false

    @level0_button = @skatolo.addHoverButton("rdc")
                     .setPosition(61, 200)
                     .setSize(36, 22)

    @level0_toggle = @skatolo.addHoverToggle("toggle")
                     .setPosition(61, 220)
                     .setSize(36, 22)

    $touch_light = Processing::PVector.new
  end

  def rdc
    puts "button pressed"
  end

  def toggle(value)
    puts "toggle pressed", value
  end



  def drawOnPaper
    background 80, 80, 80
    updateTouch
    drawTouch

    rect_w = 150
    rect_h = 100
    rect_offset_x = 30
    rect_offset_y = 9.7

    $touch_light.x = -1

    touchList.get2DTouchs.each do |touch|
      next if touch.position.x < rect_offset_x || touch.position.x > rect_offset_x + rect_w
      next if touch.position.y < rect_offset_y || touch.position.y > rect_offset_y + rect_h

      $touch_light.x = (touch.position.x - rect_offset_x) / rect_w
      $touch_light.y = (touch.position.y - rect_offset_y) / rect_h

      ellipse touch.position.x, touch.position.y, 10, 10
    end


    Papartlib::SkatoloLink.updateTouch touchList, @skatolo
    @skatolo.draw getGraphics

    out = @boardView.getViewOf cameraTracking
    $video_capture = out

    # out.filter Processing::PConstants::INVERT
    image(out, 28, 25, 16, 16) if out != nil


  end


end
