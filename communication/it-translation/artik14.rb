# coding: utf-8
## re-open the class

## Spirograph
## http://www.openprocessing.org/sketch/192792


class SobyPlayer
  attr_accessor :main_presentation, :timeline_presentation


  ## To be overriden by the Presentation Code.
  def custom_setup
    @background_inria_color = loadImage sketchPath + "/data/abstract-bubbles.jpg"
    @inria_logo_white = loadImage sketchPath + "/data/inria-logo-white.png"

    @font = loadFont sketchPath + "/data/LinLibertine-30.vlw"


    @black_line_animation_duration = 300
    @bottom_black_height = 90
    @logo_size = 250
    @draw_logo = true
    @setup_done = true

    @bottom_previous_value = @bottom_black_height
    @bottom_target = @bottom_black_height
    @black_line_animation_start = 1

    @green = color(22, 107, 30)
    init_spiro
    @use_debug = true
  end


  def slide_change
    @do_once = true
  end

  def custom_pre_draw
    background 0
    return unless @setup_done

    # update_black_lines
    # draw_black_lines

    # draw_logo if @draw_logo

    # textFont(@font, 25)
    # fill(255)


  end

  def draw_logo
    image(@inria_logo_white,
          @width - logo_width - 70,
          @height - logo_height - 20,
          logo_width,
          logo_height)
  end

  def custom_post_draw

    return unless @setup_done
    #draw_spirograph

    return unless @use_debug



    if @current_slide_no > 0
      textFont(@font, 25)
      fill(255)
      text_content = "[DEBUG] slide name: " + @slides[@current_slide_no].title.to_s
      text(text_content, 100, @height - 70)
    end

  end


  def set_black_lines_height new_value
    return unless @do_once
    @bottom_previous_value = @bottom_black_height
    @bottom_target = new_value
    @black_line_animation_start = millis

    @do_once = false
  end


  def update_black_lines

    ratio = (millis.to_f - @black_line_animation_start) / @black_line_animation_duration
    return if ratio >= 1

    @bottom_black_height = lerp(@bottom_previous_value,
                                @bottom_target,
                                ratio)
  end

  def draw_black_lines
#    @bottom_black_height |= 200
    speed = 700.0
    line_width = 80
    line_height = 30
    y = @height - @bottom_black_height


    fill @green
    stroke @green

    pushMatrix
    translate(0,0,-1)
    xTimeOffset = (millis / speed) % line_width
    (-line_width).step(@width + line_width, line_width) do |x|

      x = x+ xTimeOffset
      triangle(x, y,
               x + line_width/2,
               y - line_height,
               x + line_width,
               y)
    end
    popMatrix

    fill(@green)
    noStroke
    rect 0, @height - @bottom_black_height, @width, @bottom_black_height

  end

  def logo_ratio
    87.0 / 300.0
  end

  def logo_width
    @logo_size / 1920.0 * @width
  end

  def logo_height
    @logo_size * logo_ratio / 1080 * @height
  end


  def init_spiro
    @bigRadius = 20.0
    @smallRadius = 5.0
    @offset = 80
    @num = 360
    @pos_array = [] # PVector.new[num+1];
  end

  def draw_spirograph (cx, cy)
    fill(0)
    noStroke

    time = millis
    time = time / 50.0

    (0...@num).each do |i|
      @pos_array[i] = spirograph(time + i, @bigRadius, @smallRadius, wave(time, 0, -@offset, @offset));
    end

    pushMatrix
    translate cx, cy
    rotate time / 32.0

    stroke 255

    (0...(@num-1)).each do |i|
      r = wave(3*i, 0, 1, 15)
      strokeWeight r
      line(@pos_array[i].x, @pos_array[i].y, @pos_array[i+1].x, @pos_array[i+1].y);
    end

    popMatrix
  end

  def spirograph(time, big_radius, small_radius, offset)
    x = (big_radius + small_radius) * cos(Processing::PApplet.radians(time) ) - (small_radius + offset)*cos(Processing::PApplet.radians(((big_radius + small_radius) / small_radius) * time) )
    y = (big_radius + small_radius) * sin(Processing::PApplet.radians(time) ) - (small_radius + offset)*sin(Processing::PApplet.radians(((big_radius + small_radius) / small_radius) * time) )
    PVector.new(x, y)
  end

  def wave(time, phase,  min, max)
    return Processing::PApplet.map(sin(Processing::PApplet.radians(time + phase)), -1.0, 1.0, min, max)
  end

  def display_slide_number
    # Slide number
    push_matrix
    translate(@width - 50, @height - 55)
    fill(30)
    strokeWeight(3)
    stroke(190)
    ellipseMode(CENTER)
    ellipse(18, 22, 35, 35)
    fill 255
    noStroke
    textSize(20)

    text_pos = 12
    text_pos = 8 if @current_slide_no >= 10
    text(@current_slide_no.to_s, text_pos, 29)
    pop_matrix
  end

  def key_pressed
    if key == 'g'
      puts "Garbage"
      Java::JavaLang::System.gc
    end

    return if @prez == nil
    if keyCode == LEFT
      prev_slide
    end
    if keyCode == RIGHT
      next_slide
    end

    @frame = 0 if @frame == nil

    if key == 's'
      saveFrame("data/slides/papart-" + @frame.to_s.rjust(3, "0") + ".png")
      @frame = @frame + 1
    end

    #    puts "slide #{@current_slide_no} "
  end



end
