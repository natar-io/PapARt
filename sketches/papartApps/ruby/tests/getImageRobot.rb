
java_import 'java.awt.Robot'
java_import 'java.awt.Rectangle'
java_import 'java.awt.image.WritableRaster'


require 'ruby-processing' 
Processing::App::SKETCH_PATH = __FILE__


class Sketch < Processing::App

  def setup()
    size(480, 800, OPENGL)
    
    @robot = Robot.new
    @rectangle = Rectangle.new(1948, 78, 480 , 800)

    im = @robot.create_screen_capture @rectangle
    @image = PImage.new(im)

  end

  def draw()
    background 0

    bi = @robot.create_screen_capture @rectangle

    bi_width = bi.getWidth
    bi_height = bi.getHeight

    ## @image.loadPixels
    raster = bi.getRaster
    raster.getDataElements(0, 0, bi_width, bi_height, @image.pixels);
    @image.updatePixels
    
    if @image 
      image(@image, 0, 0, @width, @height)
    end

  end 
end

