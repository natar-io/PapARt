# -*- coding: utf-8 -*-


## To add in sketches
## Processing::App::SKETCH_PATH = __FILE__
## class Sketch < Processing::App

require 'ruby-processing' 

### Version 1

# add the current folder to the ruby path.
$:.unshift File.dirname(__FILE__)
require 'sketch1.rb'
Sketch.new


## Version 2

## ---------------------
require 'ruby-processing' 
runner = Processing::Runner.new
runner.parse_options(['live', 'sketch1.rb'])
runner.execute!
