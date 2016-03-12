class Garden < Papartlib::PaperTouchScreen
  include Lego

  def settings
    setDrawingSize 400, 400
    loadMarkerBoard($app.sketchPath + "/garden.svg", 400, 400)
    setDrawAroundPaper
  end

  def setup
    @kinect_projector = $app.papart.loadCalibration Papartlib::Papart::kinectTrackingCalib
    @kinect_projector.invert
    @vector = Processing::PVector.new
    @fountain = $app.loadImage($app.sketchPath + "/data/fountain.png")
    @last_seen_fountain = 0
    @garden_tiles = $app.loadImage($app.sketchPath + "/data/tiles.png")

    @grass_texture = []
  end


  def draw_tree
    pushMatrix
    translate 100, 0
    noStroke
    scale 30, 30
    draw_texture 0, 0.14, 0.28, 0.32
    popMatrix
  end

  def draw_grass
    pushMatrix
    translate 150, 0
    noStroke

    pushMatrix
    scale 30, 30
    draw_texture 0.1473, 0.0638 , 0, 0.12
    popMatrix

    x,w,y,h = grass_coord
    #p grass_coord

    translate 50, 0
    pushMatrix
    scale 30
    draw_texture x,w,y,h

    popMatrix

    popMatrix
  end

  def grass_coord
    x_range = 3
    y_range = 8

    x_offset = 0.14740
    y_offset = 0
    y_spacing = 0.00380
    x_spacing = 0.00200

    x_size = 0.0637
    y_size = 0.1217

    x = ($app.random x_range).to_i
    y = ($app.random y_range).to_i
    # x = 2
    # y = 2

    x = x_offset + x * (x_size + x_spacing)
    y = y_offset + y * (y_size + y_spacing)
    return [x, x_size, y, y_size]
  end


  def draw_texture(start_x, width_x, start_y, width_y, w=1)
    beginShape
    textureMode Processing::PConstants::NORMAL

    end_x = start_x + width_x
    end_y = 1 - (start_y + width_y)
    start_y = 1 - start_y

    texture @garden_tiles
    vertex(0, 0, start_x, end_y)
    vertex(0, w, start_x, start_y)
    vertex(w, w, end_x, start_y)
    vertex(w, 0, end_x, end_y)
    endShape
  end

  def tile_width ; (drawingSize.x / @tile_size).to_i  ; end
  def tile_height ; (drawingSize.y / @tile_size).to_i  ; end

  def drawAroundPaper
    # background 0, 0, 0

    updateTouch  ## TODO: why is this necessary
#     touch_and_object_detection

    draw_tree
#    draw_grass

    pushMatrix
    noStroke

    @tile_size = 30
    if not @grass_texture_init
      @grass_texture = []

      (0..tile_height).each do |y|
        (0..tile_width).each do |x|
          @grass_texture << grass_coord
        end
      end
      @grass_texture_init = true
    end

    (1..tile_height).each do |y_coord|
      (1..tile_width).each do |x_coord|
        translate(x_coord * @tile_size,
                  y_coord * @tile_size)
        x,w,y,h = @grass_texture[y_coord * tile_width + x_coord]

#        p x,w,y,h
        draw_texture x,w,y,h, @tile_size

        translate(-x_coord * @tile_size,
                  -y_coord * @tile_size)
      end
    end

    #p tile_height
    #   x,w,y,h = grass_coord
#   draw_texture x,w,y,h, 30

    draw_texture 0.1473, 0.0638 , 0, 0.12, 30
    popMatrix

    if(@fountain_pos)
      image(@fountain, @fountain_pos.x-50, @fountain_pos.y-50, 100, 100)
    end


    # hide hands & objects
    # Projector POV

    # projector = getDisplay
    # projector.loadModelView
    # applyMatrix(projector.getExtrinsics)

    # fill(0)
    # touchList.each do |touch|
    #   #p "ok" if touch.is3D

    #   size = touch.is3D ? 15 : 5
    #   touch.touchPoint.getDepthDataElements.map do |dde|
    #     @kinect_projector.mult(Processing::PVector.new(dde.depthPoint.x,
    #                                                    dde.depthPoint.y,
    #                                                    dde.depthPoint.z),
    #                            @vector)
    #     translate(@vector.x, @vector.y, @vector.z)
    #     rect(0, 0, size, size)
    #     translate(-@vector.x, -@vector.y, -@vector.z)
    #   end
    # end

  end

  def touch_and_object_detection

    fill 200
    noStroke

    touchList.get2DTouchs.each do |touch|
      next if touch.touchPoint == nil
      is_grass = check_grass touch
      ellipse touch.position.x, touch.position.y, 10, 10 if is_grass

      is_finger = check_finger touch
      fill 200, 0, 0
      ellipse touch.position.x, touch.position.y, 10, 10 if is_finger

    end

    touchList.get3DTouchs.each do |touch|
      next if touch.touchPoint == nil
#      ellipse(touch.position.x, touch.position.y, 20, 20)
      check_fountain touch
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

  def check_grass(touch)

    return true if touch.touchPoint.attachedValue > 100

    #     colored = get_touch_colors touch
    colored = select_colored_pixels(get_touch_colors touch)
#    p colored.size

    @debug_color = false
    if @debug_color
      pushMatrix
      colored.each_with_index do |px_color, i|
        fill red(px_color), green(px_color), blue(px_color)
        rect 0, 0, 3, 3
        translate 4, 0
        if i > 10
          translate( -4*100, 4) if i % 100 == 0
        end
      end
      popMatrix
    end

    #ellipse touch.position.x, touch.position.y, 10, 10
    return if colored.size < 5

#     p colored.size
    colorMode Processing::PConstants::HSB, 360

    hues = colored.map do |c|
      h = hue(c)
    end



    if @debug_color
      pushMatrix
      translate 0, 60
      hues.each_with_index do |px_color, i|
        fill px_color, 200, 200
        rect 0, 0, 3, 3
        translate 4, 0
        if i > 10
          translate( -4*100, 4) if i % 100 == 0
        end
      end
      popMatrix
    end

#     hues = select_middle_third(hues)
    mean = hues.reduce(:+) / hues.size

    #p mean
    nb_greens = hues.map{|c| is_green(c) ? 1 : 0 }.reduce(:+)
    green_ratio = nb_greens.to_f / colored.size.to_f

    colorMode Processing::PConstants::RGB, 255



    #    p green_ratio
    if green_ratio > 0.5
      # todo : set annotation to the touch.

      # p "green"
      if is_new_touch(touch)
        touch.touchPoint.attachedValue = 1
      else
         touch.touchPoint.attachedValue = touch.touchPoint.attachedValue + 1
      end
      # fill 200
      # ellipse touch.position.x, touch.position.y, 100, 100
      return true
    end
  end



def check_finger(touch)
#    return true if touch.touchPoint.attachedValue > 100

    #     colored = get_touch_colors touch
    colored = select_colored_pixels(get_touch_colors(touch), 80)
    return if colored.size < 5
    colorMode Processing::PConstants::HSB, 360
    hues = colored.map { |c| h = hue(c) }



    mean = hues.reduce(:+) / hues.size
    nb_hand = hues.map{|c| is_skin(c) ? 1 : 0 }.reduce(:+)
    hand_ratio = nb_hand.to_f / colored.size.to_f
#     p mean
    colorMode Processing::PConstants::RGB, 255

    return hand_ratio > 0.5
  end



  def check_fountain(touch)

    touch_heights = get_touch_heights touch
    min, max = touch_heights.minmax

    check_if_fountain 2000 #ms

    return if max > 100
    return unless touch_table(min) and max_height_is_below(max,55)

    colored = select_colored_pixels(get_touch_colors touch)

    return if colored.size < 5

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
  def is_green(hsb_value); hsb_value > 110 and hsb_value < 190 ;  end
  def is_skin(hsb_value); (hsb_value > 0 and hsb_value < 48) or hsb_value < 320;  end

  def select_colored_pixels touch_colors, amt=100
    touch_colors.select do |c|
      saturation(c) > amt
    end
  end

  def select_middle_third(hues)
    length = hues.size
     hues.sort.slice(length / 3, length /3)
  end

  def touch_table(min) ; min < 10 ; end
  def max_height_is_below(max, h) ; max > h ; end



end
