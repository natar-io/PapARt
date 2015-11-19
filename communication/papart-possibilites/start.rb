# require_relative '../../lib/soby'
require 'soby'


#Processing::App::SKETCH_PATH = __FILE__
$:.unshift File.dirname(__FILE__)

## Presentation - relative elements

$app = nil
Processing::PShapeSVG.TEXT_QUALITY = 2.0
$app =  SobyPlayer.new 1920, 1080

sleep 0.2 while not $app.ready?

file = File.dirname(__FILE__) + '/presentation.svg'

main_presentation = Soby::load_presentation 'artik14.rb', file
#timeline_presentation = Soby::load_presentation nil, 'timeline_presentation.svg'

$app.main_presentation = main_presentation

Soby::auto_update main_presentation, __FILE__

$app.getSurface.getNative.setUndecorated true
