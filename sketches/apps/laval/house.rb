class LegoHouse < Papartlib::PaperScreen
  include Lego

  def settings
    setDrawingSize lego_size*32, lego_size*32
    # loadMarkerBoard(Papartlib::Papart::markerFolder + "A3-small1.svg", 297, 210)
    loadMarkerBoard($app.sketchPath + "/house.svg", 297, 210)
    setDrawAroundPaper
  end

  def setup
    @lego_shader = loadShader($app.sketchPath + "/pixLightFrag.glsl",
                              $app.sketchPath + "/pixLightVert.glsl")
  end

  def check_location
    saveLocationTo $app.sketchPath + "/house.xml" if $app.save_house_location
    p "Saving house location !" if $app.save_house_location
    $app.save_house_location = false if $app.save_house_location

    loadLocationFrom $app.sketchPath + "/house.xml" if $app.load_house_location
    p "Load and fix house location !" if $app.load_house_location
#    useManualLocation true if $app.load_house_location
    $app.load_house_location = false if $app.load_house_location

    useManualLocation false if $app.move_house_location
    $app.move_house_location = false if $app.move_house_location
  end

  def draw_around_house
    pushMatrix
    fill 193, 187, 70
    small_brick 32, 32
    popMatrix
  end

  def first_floor_ground
    pushMatrix
    # go to beginning of house
    move_up_small 1
    move 2, 18

    # house floor
    fill 91, 145, 237
    small_brick 20, 12

    move_up_small 1.2
    fill 0, 255, 100
    # rect(0, 0, lego_size * 18, lego_size * 12)

    # setResolution 4
    tiling =  2

    tile_w = 21
    tile_h = 10
    nb_tiles_w = 20.0
    nb_tiles_h = 12.0

    tiling_w = nb_tiles_w / tile_w
    tiling_h = nb_tiles_h / tile_h

    if @floor_texture
      textureWrap Processing::PConstants::REPEAT
      beginShape
      texture @floor_texture
      vertex(0, 0, 0, 0);

      vertex(lego_size * nb_tiles_w, 0,
             tiling_w, 0)

      vertex(lego_size * nb_tiles_w,
             nb_tiles_h * lego_size,
             tiling_w,
             tiling_h)

      vertex(0, lego_size * nb_tiles_h,
             0, tiling_h)
      endShape
    end

    popMatrix
  end

  def table
    pushMatrix
    move_up_small 1
    move 10, 18
    translate -12, 11, -25
    fill 0, 200, 0
    rect 0, 0, 38, 38
    popMatrix
  end


  def first_floor_walls
    pushMatrix

    move_up_small 2

    noStroke
    fill 117, 136, 167

    move 0, 20
    brick 2, 10, 5

    pushMatrix
    move 2,0
    if @wall_texture
      pushMatrix
      rotateY Processing::PConstants::HALF_PI
      #rect 0, 0, lego_size * 5, lego_size * 10
      image @wall_texture, 0, 0, lego_size * 6, lego_size * 10
      popMatrix
    end
    popMatrix

    move 1, 10
    brick 21, 2, 5

    popMatrix
  end


  def second_floor_ground
    pushMatrix
    # go to beginning of house
    move_up_small 3  ## ground + 1st floor
    move_up 5        ## wall height
    move 0, 21

    noStroke
    # house floor
    fill 76, 120, 172
    small_brick 3, 11

    move 3, 8
    small_brick 19, 3
    popMatrix
  end

  def second_floor_walls
    pushMatrix

    noStroke
    # go to beginning of house
    move_up_small 2  ## ground + 1st floor + 2ndfloor height
    move_up 5        ## wall height
    move_up_small 1
    move(-1, 21)

    fill 66, 100, 152
    # house floor
    #    fill 229, 126, 245
    brick 1, 12, 2

    move(1, 11)
    brick 22, 1, 2

    # move 3, 8
    # small_brick 19, 3
    popMatrix
  end



  def red_light
    pushMatrix
    move_up_small 2
    move 12, 25
    # c = $app.millis % 3000.0  / 3000 * 255
    # pointLight c, 0, 0, 0, 0, 40
    fill 255, 0, 0
    move_up_small 1
    rect(0, 0 , lego_size * 2, lego_size)
    popMatrix
  end

  def couch
    pushMatrix
    move_up_small 2 # floors
    move_up 1       # feet
    move_up_small 2 # cushions

    move 2, 18
    move 16, 2

    fill 138, 203, 47
    small_brick 3, 8

    popMatrix
  end

  def canvas
    pushMatrix

    popMatrix
  end

  def drawAroundPaper

    if not @load_texture
      @wall_texture = $app.loadImage($app.sketchPath + "/data/texture/wall1.png")
      @floor_texture = $app.loadImage($app.sketchPath + "/data/texture/floor4.png")
      @load_texture = true
    end

    check_location
    setLocation 0, 0 , 0
    useManualLocation false
    # shader @lego_shader
    #    resetShader

    pushMatrix
    translate 100, 100, 400
#    pointLight 255, 255, 255, 0, 0, 0
#    pointLight 255, 255, 255, 0, 0, 0
    popMatrix

     red_light

    draw_around_house
    first_floor_ground
    # table
    couch

    first_floor_walls

    second_floor_ground
    second_floor_walls

  end


  def mouse_light
    pointLight 200, 200, 200, $app.mouse_x, 400 -  $app.mouse_y, 100
    pushMatrix
    translate 0, 0, -80
    rect($app.mouse_x, 400 - $app.mouse_y, 10, 10)
    popMatrix
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

  def brick(w, h, thickness = 1)
    pushMatrix

    brick_w = w* lego_size
    brick_h = h* lego_size

    translate(brick_w / 2, brick_h / 2, -lego_thickness * thickness/2 )

    box(brick_w, brick_h, lego_thickness * thickness)
    popMatrix
  end


end
