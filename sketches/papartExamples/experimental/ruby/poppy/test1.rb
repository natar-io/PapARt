# -*- coding: utf-8 -*-

require 'ruby-processing' 
require 'jruby/core_ext'

Processing::Runner 
Dir["#{Processing::RP_CONFIG['PROCESSING_ROOT']}/core/library/\*.jar"].each{ |jar| require jar }
Processing::App::SKETCH_PATH = __FILE__   unless defined? Processing::App::SKETCH_PATH

require './cp5' 

class MyApp < Processing::App

  attr_reader :cp5
  attr_accessor :once, :button, :slider

  def create_method(name, &block)
    self.class.send(:define_method, name, &block)
  end
  
  def setup
    size 800, 800, OPENGL

    
    @cp5 = ControlP5.new self


    @button = @cp5.addButton("button")
              .setPosition(40, 200)
              .setSize(280, 40)
    
    @slider = @cp5.addSlider("slider1")
              .setPosition(0, 0)
              .setSize(150, 20)
    
    @cp5.update
       
  end

  def draw 
    background slider1_value 

    # puts mouse_x
    # self.slider1_value = mouse_x
  end

  def mouse_dragged
    slider1_value = mouse_x
  end
  
  def button
    puts "Button pressed"
    self.slider1_value = 10
  end

  def slider1 value
    puts "Slider event " + value.to_s
  end

  def test
    puts "test"
  end

end



MyApp.new  unless defined? $app
