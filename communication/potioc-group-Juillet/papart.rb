# coding: utf-8

require './background_func'
require './cp5'

class SobyPlayer

  attr_accessor :robot_prez
  attr_reader :background_sketch, :paper
  attr_reader :cp5

  ## For CP5
  def create_method(name, &block)
    self.class.send(:define_method, name, &block)
  end

  # Overridden methods. 
  def custom_setup 
    @background_sketch = Background.new @width, @height, self unless @background_sketch != nil

    @cp5 = ControlP5.new self  unless @cp5 != nil
    @bg = @background_sketch
  end

  def lighter
    @background_sketch.lighter
  end
  
  def custom_pre_draw
    background 255

    
    return if @background_sketch == nil
    @background_sketch.update millis
    @background_sketch.draw g
    
  end

  def reset_sketch
    # @cp5.remove "my_slider"
    # @cp5.remove "init_ar"

    # @processing_demo = nil
    # @papart_demo = nil
    # @is_ar_initalized = nil
    # @draw_ar = nil
  end

  def custom_post_draw
    if @cp5 != nil
      @cp5.draw
    end
  end
  
  def get_angle
    atan2(mouse_x - @width / 2, @height / 2 - mouse_y)
  end
  
  def slide_change 
    @once = nil
  end


  
  ## Disable slide number
  # def display_slide_number 
  # end

  def display_slide_number 
    # Slide number
    push_matrix
    translate(@width - 50, @height - 60)

    # ellipse inside
    fill(151, 20, 38)
    noStroke
    # stroke(190)
    # strokeWeight(3)
    ellipseMode(CENTER)
    ellipse(18, 22, 35, 35)

    fill(255)
    noStroke 
    textSize(20)
    text(@current_slide_no.to_s, 12, 30) if @current_slide_no < 10
    text(@current_slide_no.to_s, 6, 30) if @current_slide_no >= 10
    pop_matrix
  end
  
end
