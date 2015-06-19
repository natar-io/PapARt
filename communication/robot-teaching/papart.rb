Processing::App::load_library :PapARt, :javacv, :toxiclibscore

module Papartlib
  include_package 'fr.inria.papart.procam'
  include_package 'fr.inria.papart.procam.camera'
  include_package 'fr.inria.papart.drawingapp'
end

class MyPaper < Papartlib::PaperScreen

  def setup
    puts "Setup"
    setDrawingSize(297, 210);
    loadMarkerBoard($app.sketchPath("") + "/data/A3-small1.cfg", 297, 210);
    puts "Setup finished"
  end

  def draw
#    puts "Draw"
    begin
      setLocation(0, 0, 0)
      pg = beginDraw2D
      pg.background 40, 200, 200
      pg.endDraw
    rescue Exception => e  
      puts e.message
    end 
#    puts "draw ended"
  end
end



class SobyPlayer

  attr_accessor :robot_prez

  # Overridden methods. 
  def custom_setup 
    @my_background = load_image(sketchPath "data/background.png")

#    @dirBlur = loadShader(sketchPath "blur.frag");

    return if Papartlib::Papart::getPapart != nil
    
    @papart = Papartlib::Papart.new self
    @papart.initCamera
    
    @camera = @papart.getCameraTracking
    @display = @papart.getARDisplay
    @display.manualMode

    @paper = MyPaper.new

    @papart.startTracking

    @draw_ar = false
    @init_background = true    
  end

  def custom_pre_draw
    background 255

    return if not @init_background
    # image(@my_background, 0, 0, @width, @height)


    if @draw_ar    
      @display.drawScreens
      im = @camera.getPImage
      if im != nil
        image im, 0, 0, 800, 600
        #      Papartlib::DrawUtils::drawImage(
        draw_y_inverted(g, @display.render, 0, 0, 800, 600)
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
    
    return if not @init_background
    
    image(@my_background, 0, 0, @width, @height)

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
  
  
end
