require 'soby' 

# Give the current folder to Processing.
Processing::App::SKETCH_PATH = __FILE__
$:.unshift File.dirname(__FILE__)

$app =  SobyPlayer.new 1920,1080

sleep 0.2 while not $app.ready? 

presentation = Soby::load_presentation  'papart.rb', 'robot.svg'
Soby::auto_update presentation, __FILE__

