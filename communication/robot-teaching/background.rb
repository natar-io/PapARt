class Background

  attr_accessor :square_size
  attr_reader :graphics
  
  def fonce
    amplitude 15, 2
    min 30
    max 250
    rate 200
    color_mode nil
    constrain = true
  end

  def clair 
    amplitude 10, 30
    min 200
    max 250
    rate 250
    color_mode nil
    constrain = true
    #puts "clair" 
  end

  def couleur
    amplitude 20, 40
    min 0
    max 360
    rate 400
    @color_saturation = 50
    @color_lum = 80
    color_mode true 
    constrain = true
  end

  def initialize width, height, applet
    @applet = applet
    @graphics = applet.createGraphics width, height
    @width, @height = width, height
    @colors = Hash.new
    @square_size = 20
    @nb_squares_x = @width / @square_size + 1
    @nb_squares_y = @height / @square_size + 1
    @last_update = 0
    @rate = 1000
    @color_saturation = 50
    @color_lum = 100

    reset
    clair
  end 

  def reset
    @graphics.beginDraw    
    @graphics.stroke(230)
    @min_color = 210
    @max_color = 255
    @constrain = true

    ## rectangles as background...
    @graphics.rectMode(@applet.class::CENTER)

    (0...(@nb_squares_x)).each do |x|
      (0...(@nb_squares_y)).each do |y|
        c = generate_color
        @graphics.fill(c)
        @graphics.rect(x*(@square_size+3), y* (@square_size+3), @square_size, @square_size) 

        @colors[[x,y]] = c
      end
    end
    @graphics.endDraw

    amplitude 4
    rate 400
  end

  def generate_color
    (@max_color - @min_color) / 2 + @min_color + @applet.random(-10, 10).to_i
  end
  

  def amplitude neg, pos=neg
    @amplitude_neg = neg
    @amplitude_pos = pos
  end

  def rate rate
    @update_rate = rate
  end

  def min min
    @min_color = min
  end 

  def max max
    @max_color = max
  end 

  def color_mode c
    @color_mode = c
  end 


  def update(time)
    return if  (time <  @last_update + 200)
    @last_update = time
    
    @graphics.beginDraw

    set_color_mode

    if @color_mode == nil
      update_squares_gray
    else 
      update_squares_colored
    end
      
    @graphics.endDraw
  end

  def update_squares_colored
    subset_squares_to_update do |x, y|
      c = update_color x,y
      @graphics.fill(c, @color_saturation, @color_lum)
      @graphics.rect(x*(@square_size+3), y* (@square_size+3), @square_size, @square_size) 
    end
  end

  def update_squares_gray
    subset_squares_to_update do |x, y|
      c = update_color x,y
      @graphics.fill(c)
      @graphics.rect(x*(@square_size+3), y* (@square_size+3), @square_size, @square_size) 
    end
  end
  
  def update_color x,y
    c = @colors[[x,y]] + @applet.random(-@amplitude_neg, @amplitude_pos).to_i
    c = @applet.constrain(c, @min_color, @max_color) if @constrain
    @colors[[x,y]] = c
    c
  end
  
  
  def subset_squares_to_update
    (1...@update_rate).each do |n|
      x = @applet.random(@nb_squares_x).to_i
      y = @applet.random(@nb_squares_y).to_i
      yield x,y
    end      
  end

  def set_color_mode
    @graphics.colorMode(@applet.class::RGB, 255)
    @graphics.stroke(220)

    if @color_mode == nil
      @graphics.colorMode(@applet.class::RGB, 255)
    else 
      @graphics.colorMode(@applet.class::HSB, 360, 100, 100)
    end

  end
  

end
