class MyPaper < Papartlib::PaperScreen

  def setup
    setDrawingSize(297, 210);
    loadMarkerBoard($app.sketchPath("") + "/data/A3-small1.cfg", 297, 210);
  end

  def draw

    return unless $app.is_draw_ar
    begin
      setLocation(0, 0, 0)
      pg = beginDraw2D
      background(23, 105, 137, 160)
      translate(100, 100, 0)
      scale 0.8
      noStroke
      fill(255, 150)
      (1..23).each do |i| 
        rect 0, 10, 80, 23- i 
        rect 0, 100, 70, 23 - i
        rotate( $app.my_slider_value )
      end 
      
      pg.endDraw
    rescue Exception => e  
      puts e.message
    end 
#    puts "draw ended"
  end
end


