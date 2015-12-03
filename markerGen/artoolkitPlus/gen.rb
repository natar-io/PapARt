## To make it work:
# install jruby 9.0.x.x ,
# linux:   apt-get install jruby // yaourt -S jruby etc...
# windows: install from the website.

# install the required gems:
# jgem install nokogiri jruby_art


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

output_name = (ARGV[0].split ".svg")[0] + ".cfg"

board = MarkerBoard.new input_name
#board.set_offset(offset_x, offset_y)
board.save_as output_name
