###### Warning #########
### NOT USED ANYMORE ###
###### Warning #########

class Cinema < Papartlib::PaperTouchScreen

  include_package 'processing.video'
  include_package 'processing.video.Movie'
  include_package 'org.gestreamer.elements'

  def settings
    setDrawingSize 100, 50
    loadMarkerBoard($app.sketchPath + "/cinema.svg", 100, 50)
#    setDrawAroundPaper
  end

  def setup
#    @movie = Movie.new $app, "data/video.mp4"

    id = ($app.random 14).to_i.to_s.rjust(2, '0')
    id = "0" + id + ".mp4"
    @movie = Movie.new $app, "data/videos/" + id
    @movie.loop
    @movie.volume 0
  end

  def drawOnPaper
    # background 255, 255, 255
    @movie.volume 0.0
    # fill 0, 200, 0
    # rect 0, 0, drawingSize.x, drawingSize.y
    @movie.read
    #    image @movie, 0, 0, 30, 30
    # imageMode Processing::PConstants::CENTER
    # translate drawingSize.x/2, drawingSize.y/2
    # rotateX Processing::PConstants::PI
    imageMode Processing::PConstants::CORNER
    image @movie, 0, 0, drawingSize.x , drawingSize.y
  end


end
