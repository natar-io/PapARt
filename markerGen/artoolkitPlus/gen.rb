require 'nokogiri'  # for XML.
require 'jruby_art' # for matrices
require 'jruby_art/app'

require 'java'

# For the other files, we need to load the libraries
#Processing::App::load_library 'video', 'toxiclibscore'


require_relative 'transforms'
require_relative 'markerSVG'
require_relative 'utils'

check_arguments
input_name = ARGV[0]

output_name = "output.cfg"
output_name = ARGV[1] if ARGV[1] != nil

board = MarkerBoard.new input_name
board.save_as output_name
