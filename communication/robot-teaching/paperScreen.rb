class MyPaper < Papartlib::PaperTouchScreen

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


