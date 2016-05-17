## re-open the class

class SobyPlayer


  def fonce
    background_amplitude 10, 2
    background_min 40
    background_max 180
    background_rate 200
    color_mode nil
    background_constrain = true
  end

  def clair
    background_amplitude 2, 10
    background_min 160
    background_max 250
    background_rate 250
    color_mode nil
    background_constrain = true
    #puts "clair"
  end

  def couleur
    background_amplitude 2, 40
    background_min 0
    background_max 360
    background_rate 400
    color_mode true
    background_constrain = true
  end

  # Overridden methods.
  def custom_setup
    init_background

  end

  def custom_pre_draw
    background 255
    return if not @init_background
    draw_custom_background
  end

  def custom_post_draw

  end


  def init_background
    @init_background = true
    @background = createGraphics @width, @height
    @background_colors = Hash.new
    @square_size = 15
    @nb_squares_x = @width / @square_size + 1
    @nb_squares_y = @height / @square_size + 1
    @last_update = 0
    reset_background
  end

  def draw_custom_background
    if !@is_moving && (millis() >  @last_update + 200)
      update_background
      @last_update = millis()
    end

    image @background, 0, 0, @width, @height
  end


  def reset_background
    @background.beginDraw
    @background.stroke(230)
    @background_min_color = 210
    @background_max_color = 255
    @background_constrain = true

    ## rectangles as background...
    @background.rectMode(CENTER)

    (0...(@nb_squares_x)).each do |x|
      (0...(@nb_squares_y)).each do |y|
        c = @background_min_color + random(25).to_i
        @background.fill(c)
        @background.rect(x*(@square_size+3), y* (@square_size+3), @square_size, @square_size)

        @background_colors[[x,y]] = c
      end
    end
    @background.endDraw

    background_amplitude 4
    background_rate 400
  end

  def background_amplitude neg, pos=neg
    @background_amplitude_neg = neg
    @background_amplitude_pos = pos
  end

  def background_rate rate
    @background_update_rate = rate
  end

  def background_min min
    @background_min_color = min
  end

  def background_max max
    @background_max_color = max
  end

  def color_mode c
    @color_mode = c
  end


  def update_background
    @background.beginDraw

    @background.colorMode(RGB, 255)
    @background.stroke(220)

    # ## Take x random numbers
    (1...@background_update_rate).each do |n|
      #    (1...@background_update_rate).each do |n|
      x = random(@nb_squares_x).to_i
      y = random(@nb_squares_y).to_i

      c = @background_colors[[x,y]] + random(-@background_amplitude_neg, @background_amplitude_pos).to_i

      #      puts c
      c = constrain(c, @background_min_color, @background_max_color) if @background_constrain

      if @color_mode != nil
        @background.colorMode(HSB, 360, 100, 100)
        @background.fill(c, 190, 200)
      else
        @background.colorMode(RGB, 255)
        @background.fill(c)
      end

      #      puts c
      #      @background.fill(c)
      @background.rect(x*(@square_size+3), y* (@square_size+3), @square_size, @square_size)

      @background_colors[[x,y]] = c
    end

    @background.endDraw
  end

end
