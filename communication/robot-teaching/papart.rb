# coding: utf-8
Processing::App::load_library :PapARt, :javacv, :toxiclibscore

module Papartlib
  include_package 'fr.inria.papart.procam'
  include_package 'fr.inria.papart.procam.camera'
  include_package 'fr.inria.papart.drawingapp'
end

require_relative 'paperScreen'
require_relative 'background'
require_relative 'skatolo'

class SobyPlayer

  attr_accessor :robot_prez
  attr_reader :background_sketch, :paper
  attr_reader :skatolo

  ## For SKATOLO
  def create_method(name, &block)
    self.class.send(:define_method, name, &block)
  end

  # Overridden methods.
  def custom_setup

#    @dirBlur = loadShader(sketchPath "blur.frag");
    @background_sketch = Background.new @width, @height, self unless @background_sketch != nil
    @skatolo = Skatolo.new self  unless @skatolo != nil

  end


  def init_ar
    return if @is_ar_initalized
    return if Papartlib::Papart::getPapart != nil

    @papart = Papartlib::Papart.new self

    # @papart.initKinectCamera 1
    # @papart.loadTouchInputKinectOnly

    @papart.initCamera
    @camera = @papart.getCameraTracking
    @display = @papart.getARDisplay
    @display.manualMode

    @paper = MyPaper.new

    @papart.startTracking
    @is_ar_initalized = true
  end



  def custom_pre_draw
    background 255

    return if @background_sketch == nil
    @background_sketch.update millis

    image @background_sketch.graphics, 0, 0, @width, @height

  end

  def reset_sketch
    @skatolo.remove "my_slider"
    @skatolo.remove "init_ar"

    @processing_demo = nil
    @papart_demo = nil
    @is_ar_initalized = nil
    @draw_ar = nil
  end

  def papart_demo
    draw_ar

    return if @papart_demo != nil
    @draw_ar = true
    @skatolo.addButton("init_ar")
      .setPosition(50, 50)
      .setSize(50, 50)
    @papart_demo = true
    processing_demo
  end

  def processing_demo
    return if @processing_demo != nil
    @skatolo.addSlider("my_slider")
      .setPosition(100, 50)
      .setValue(0.1)
      .setRange(-1, 1)
      .setSize(400, 40)
    @skatolo.update
    @processing_demo = true
  end

  def is_draw_ar
    return @draw_ar
  end

  # def processing_demo
  #   return if @processing_demo != nil
  #   @processing_demo = true
  #   puts "Loading processing"
  #   `processing`
  # end

  def draw_ar
    if @draw_ar
      @display.drawScreens
      im = @camera.getPImage

      w = 1280
      h = 1024

      if im != nil
        pushMatrix
        translate((@width - w) / 2, (@height - h) /2, 0)
        image im, 0, 0, w, h
        #      Papartlib::DrawUtils::drawImage(
        draw_y_inverted(g, @display.render, 0, 0, w, h)
        popMatrix
      end
    end

  end

  def draw_y_inverted(g, img, x, y, w, h)
       g.pushMatrix();
        g.translate(x, y);
        g.beginShape(QUADS);
        g.textureMode(NORMAL);
        g.texture(img);
        g.vertex(0, 0, 0, 1);
        g.vertex(0, h, 0, 0);
        g.vertex(w, h, 1, 0);
        g.vertex(w, 0, 1, 1);
        g.endShape();
        g.popMatrix();
  end

  def custom_post_draw


    if @skatolo != nil
      @skatolo.draw

    end

    # fill(200)
    # ellipse mouse_x, mouse_y, 100, 100


    # if @is_moving
    #   angle = get_angle
    #   @dirBlur.set("blurOffset",
    #                1.0 / @width * sin(angle),
    #                1.0 / @height * cos(angle))

    #   2.times do
    #     filter(@dirBlur)
    #   end
    # end

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
