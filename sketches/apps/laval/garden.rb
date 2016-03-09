class Garden < Papartlib::PaperTouchScreen
  include Lego

  def settings
    setDrawingSize 297, 210
    loadMarkerBoard($app.sketchPath + "/garden.svg", 297, 210)
  end

  def setup
    @kinect_projector = $app.papart.loadCalibration Papartlib::Papart::kinectTrackingCalib
    @kinect_projector.invert
    @vector = Processing::PVector.new
    @fountain = $app.loadImage($app.sketchPath + "/data/fountain.png")
    @last_seen_fountain = 0
  end

  def touch_and_object_detection
    touchList.get3DTouchs.each do |touch|
      next if touch.touchPoint == nil

      @touch_heights = get_touch_heights touch
      @touch_colors = get_touch_colors touch

      @min, @max = @touch_heights.minmax

      check_fountain touch
      check_grass touch

      # duplo height : 19mm

      ## With this, blue is ~ 220 degrees


    end
  end

  def get_touch_heights(touch)
    touch.touchPoint.getDepthDataElements.map do |dde|
      table_point = dde.projectedPoint.z
    end
  end

  def get_touch_colors(touch)
    touch.touchPoint.getDepthDataElements.map do |dde|
      @kinect_projector.mult(Processing::PVector.new(dde.depthPoint.x,
                                                     dde.depthPoint.y,
                                                     dde.depthPoint.z),
                             @vector)
      getColorFrom3D @vector
    end
  end


  def check_fountain(touch)

    check_if_fountain 2000 #ms

    return if @max > 100
    return unless touch_table and max_height_is_below 55

    colored = select_colored_pixels
    return if colored.size < 5

#    p "Here"

    colorMode Processing::PConstants::HSB, 360

    hues = colored.map do |c|
      hue(c)
    end

    hues = select_middle_third(hues)
    mean = hues.reduce(:+) / hues.size

    colorMode Processing::PConstants::RGB, 255

    #    p touch.touchPoint.attachedValue
    # large blue
    if is_blue(mean)
      # todo : set annotation to the touch.

      if is_new_touch(touch)
        touch.touchPoint.attachedValue = 10
      else
        touch.touchPoint.attachedValue = touch.touchPoint.attachedValue + 1
      end

      # ellipse touch.position.x, touch.position.y, 20, 20

      if touch.touchPoint.attachedValue > 30
        @fountain_pos = Processing::PVector.new(touch.position.x, touch.position.y)
        @last_seen_fountain = $app.millis
      end
    end
  end

  def check_if_fountain(timeout)
    @fountain_pos = nil if @last_seen_fountain + timeout < $app.millis
  end


  def is_new_touch(touch) ; touch.touchPoint.attachedValue == -1 ; end

  def is_blue(hsb_value); hsb_value > 190 and hsb_value < 230 ;  end

  def select_colored_pixels
    @touch_colors.select do |c|
      saturation(c) > 100
    end
  end

  def select_middle_third(hues)
    length = hues.size
     hues.sort.slice(length / 3, length /3)
  end

  def touch_table ; @min < 10 ; end
  def max_height_is_below(h) ; @max > h ; end


  def check_grass(touch)

  end


  def drawOnPaper
    background 0, 0, 0

    touch_and_object_detection

    if(@fountain_pos)
      image(@fountain, @fountain_pos.x-50, @fountain_pos.y-50, 100, 100)
    end
  end
end
