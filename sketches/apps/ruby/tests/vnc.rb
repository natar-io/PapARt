
java_import 'java.awt.Robot'
java_import 'java.awt.Rectangle'
java_import 'java.awt.image.WritableRaster'

require 'ruby-processing' 
Processing::App::SKETCH_PATH = __FILE__  unless defined? Processing::App::SKETCH_PATH


module Vnc
  include_package 'com.glavsoft.viewer'
  include_package 'com.glavsoft.viewer.cli'
end

java_import 'javax.swing.SwingUtilities'


class Sketch < Processing::App

  load_library :tvnjviewer

  def setup()
    size(800, 600, OPENGL)
    
    @viewer = Vnc::Viewer.new 
    SwingUtilities.invokeLater @viewer

  end

  def draw()
    background 0

  end 
end

