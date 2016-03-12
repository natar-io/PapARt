class LegoHouse < Papartlib::PaperScreen
  include Lego
  include_package 'processing.video'
  include_package 'processing.video.Movie'
  include_package 'org.gestreamer.elements'


  def settings
    setDrawingSize lego_size*32, lego_size*32
    # loadMarkerBoard(Papartlib::Papart::markerFolder + "A3-small1.svg", 297, 210)
    loadMarkerBoard($app.sketchPath + "/house.svg", 297, 210)
    setDrawAroundPaper
  end

  def setup
    @lego_shader = loadShader($app.sketchPath + "/pixLightFrag.glsl",
                              $app.sketchPath + "/pixLightVert.glsl")
    init_video
  end

  def drawAroundPaper

    if not @load_texture
      @wall_texture = $app.loadImage($app.sketchPath + "/data/texture/wall1.png")
      @floor_texture = $app.loadImage($app.sketchPath + "/data/texture/floor4.png")
      @logo_texture =  $app.loadImage($app.sketchPath + "/data/logo.png")
      @load_texture = true
    end

    check_location
    setLocation 0, 0 , 0
    useManualLocation false

    shader @lego_shader

    touch_light

    @first_floor_drawn = true
    @second_floor_drawn = false

    # draw_around_house
    first_floor_ground
    couch
    first_floor_walls


    second_floor_ground
    second_floor_walls

    resetShader
  end

  def touch_light
    return if $touch_light == nil or $touch_light.x == -1
    pushMatrix
    move_up_small 2
    move 2, 18
    move_up_small 2
    #    rect(0, 0, 20, 20)
    px = lego_size * 20 * $touch_light.x
    py = lego_size * 12 * (1.0-$touch_light.y)

    pushMatrix
    translate px, py, 80
    pointLight 255, 255, 255, 0, 0, 0
    #pointLight 255, 200, 10, 0, 0, 0
    popMatrix


    move_up_small 1

    fill 255, 180
    ellipse(px, py , lego_size , lego_size)
    popMatrix
  end


  def ground_wall_color
    @first_floor_drawn ? $app.color(255, 244, 203) : 0
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
    noStroke
    fill ground_wall_color
    small_brick 20, 12

    draw_floor_and_carpet if @first_floor_drawn

    popMatrix
  end

  def draw_floor_and_carpet
    move_up_small 1.2
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
    fill ground_wall_color

    move 0, 20
    brick 2, 10, 5

    draw_fireplace
    move 1, 10
    brick 21, 2, 5

    popMatrix
  end

  def draw_fireplace

    pushMatrix
    move 2.05,0
    if @wall_texture and @first_floor_drawn
      pushMatrix
      rotateY Processing::PConstants::HALF_PI
      #rect 0, 0, lego_size * 5, lego_size * 10
      image @wall_texture, 0, 0, lego_size * 6, lego_size * 10
      popMatrix
    end
    popMatrix

  end

  def second_floor_ground_color
    @second_floor_drawn ? $app.color(39, 113, 201) : 0
  end
  def second_floor_ground_wall
    @second_floor_drawn ? $app.color(39, 113, 201) : 0
  end


  def second_floor_ground
    pushMatrix
    # go to beginning of house
    move_up_small 3  ## ground + 1st floor
    move_up 5        ## wall height
    move 0, 21

    noStroke
    # house floor
    fill  second_floor_ground_color
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

    # |
    fill second_floor_ground_wall
    brick 1, 12, 2

    # ----
    move(1, 11)
    brick 22, 1, 2

    move_up_small 2
    move(2, -1)
    draw_tv
    draw_logo

    popMatrix
  end

  def draw_tv
    pushMatrix
    ## Movie
    @movie.volume 0.0
    @movie.read

    screen_w = 100
    screen_h = 50

    rotateX -Processing::PConstants::HALF_PI
    imageMode Processing::PConstants::CENTER
    translate screen_w/2, 50/2

    scale(1, -1, 1)
    image @movie, 0, 0, screen_w, 50
#    image $video_capture, 0, 0, screen_w, 50 if $video_capture
    scale(1, -1, 1)
    imageMode Processing::PConstants::CORNER
    popMatrix
  end


  def draw_logo
    pushMatrix
    ## Movie
    @movie.volume 0.0
    @movie.read

    screen_w = lego_size * 8
    screen_h = 20

    rotateX -Processing::PConstants::HALF_PI
    imageMode Processing::PConstants::CENTER
    translate lego_size * 2 + screen_w/2, 50 + 20/2
    scale(1, -1, 1)
    #    image @movie, 0, 0, screen_w, 50
    image @logo_texture, 0, 0, screen_w, 20
    scale(1, -1, 1)
    imageMode Processing::PConstants::CORNER
    popMatrix
  end



  def couch
    pushMatrix
    move_up_small 2 # floors
    move_up 1       # feet
    move_up_small 2 # cushions

    move 2, 18
    move 16, 2

    if @first_floor_drawn
      fill 138, 203, 47
    else
      fill 138/2, 203/2, 47/2
    end

    small_brick 3, 8

    popMatrix
  end

  def canvas
    pushMatrix

    popMatrix
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


  def init_video
    id = ($app.random 14).to_i.to_s.rjust(2, '0')
    id = "0" + id + ".mp4"
    @movie = Movie.new $app, "data/videos/" + id
    @movie.loop
    @movie.volume 0
  end



end
