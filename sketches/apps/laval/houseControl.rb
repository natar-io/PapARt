require_relative 'skatolo'

class HouseControl < Papartlib::PaperTouchScreen

  include_package 'fr.inria.skatolo'
  include_package 'fr.inria.skatolo.events'
  include_package 'fr.inria.skatolo.gui.controllers'
  include_package 'fr.inria.skatolo.gui.Pointer'

  ## For skatolo
  def create_method(name, &block)
    self.class.send(:define_method, name, &block)
  end

  def settings
    setDrawingSize 210, 297
    loadMarkerBoard($app.sketchPath + "/house-control.svg", 210, 297)
    #    setDrawAroundPaper
  end

  def setup

  end

  def create_buttons
    @skatolo = Skatolo.new $app, self

    @level0_button = @skatolo.addHoverButton("level0_button")
                     .setPosition(40, 200)
                     .setSize(280, 40)
  end

  def level0_button
    puts "button pressed"
  end

  def drawOnPaper
    background 100

    updateTouch
    updateButtons

  end


end
