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

    @rect_w = 150
    @rect_h = 100
    @rect_offset_x = 30
    @rect_offset_y = 9.7

    @boardView = Papartlib::TrackedView.new self
    @boardView.setCaptureSizeMM Processing::PVector.new(@rect_w, @rect_h)

    @boardView.setImageWidthPx(110 * 2);
    @boardView.setImageHeightPx(60 * 2);

    origin = Processing::PVector.new @rect_offset_x,  @rect_offset_y
    @boardView.setBottomLeftCorner(origin);
    @boardView.init
  end

  def create_buttons

    if @skatolo == nil
      @skatolo = Skatolo.new $app, self
      @skatolo.getMousePointer.disable
      @skatolo.setAutoDraw false
    end

    @level0_button = @skatolo.addHoverButton("rdc")
                     .setPosition(12.5, 135)
                     .setSize(36, 22)

    @skatolo.addHoverButton("touch")
      .setPosition(62.5, 135)
      .setSize(36, 22)
    @skatolo.addHoverButton("cinema")
      .setPosition(112.5, 135)
      .setSize(36, 22)
    @skatolo.addHoverButton("perso")
      .setPosition(162.5, 135)
      .setSize(36, 22)

    @skatolo.addHoverButton("grass")
      .setPosition(90, 70)
      .setSize(36, 22)


    # @level0_toggle = @skatolo.addHoverToggle("toggle")
      #                  .setPosition(61, 220)
      #                  .setSize(36, 22)

    $touch_light = Processing::PVector.new
  end

  def rdc
    $app.lego_house.mode=LegoHouse::FIRST_FLOOR_LIGHT
  end

  def touch
    $app.lego_house.mode=LegoHouse::FIRST_FLOOR_LIGHT_TOUCH
  end

  def cinema
    $app.lego_house.mode=LegoHouse::SECOND_FLOOR_CINEMA
  end

  def perso
    $app.lego_house.mode=LegoHouse::SECOND_FLOOR_CAPTURE
  end


  def grass
    $app.garden.reset_grass
  end

  # def toggle(value)
  #   puts "toggle pressed", value
  # end


  def drawOnPaper
    background 50
    updateTouch


    $touch_light.x = -1

    touchList.get2DTouchs.each do |touch|

      if touch.position.y < drawingSize.y - @rect_offset_y - @rect_h
        ellipse touch.position.x, touch.position.y, 15, 15
      end

      next if touch.position.x < @rect_offset_x || touch.position.x > @rect_offset_x + @rect_w
      next if touch.position.y > drawingSize.y - @rect_offset_y || touch.position.y <
                                                                   drawingSize.y - @rect_offset_y - @rect_h

      #next if touch.position.y < drawingSize.y - @rect_offset_y || touch.position.y > drawingSize.y - @rect_offset_y + @rect_h
      $touch_light.x = (touch.position.x - @rect_offset_x) / @rect_w
      $touch_light.y = (drawingSize.y - touch.position.y + @rect_offset_y) / @rect_h

      # debug
      if $app.lego_house.mode == LegoHouse::FIRST_FLOOR_LIGHT_TOUCH
        ellipse touch.position.x, touch.position.y, 10, 10
      end
    end


    Papartlib::SkatoloLink.updateTouch touchList, @skatolo
    @skatolo.draw getGraphics

    out = @boardView.getViewOf cameraTracking
    $video_capture = out

    # out.filter Processing::PConstants::INVERT
    # image(out, 28, 25, 16, 16) if out != nil


  end


end
