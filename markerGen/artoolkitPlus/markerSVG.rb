require 'ostruct'
require 'base64'


class Marker
  attr_reader :mat, :width, :height

  def initialize (mat, width, height)
    @mat, @width, @height = mat, width, height
  end
end


class MarkerBoard

  include_package 'processing.core'

  include Papart

  attr_accessor :markers, :pshape, :svg, :transform
  attr_accessor :width, :height, :matrix

  ## Parse the Imagse

  def initialize (url)
    @url = url
    @svg = Nokogiri::XML(open(@url)).children[1];

    load_markers
  end

  def save_as file_name

    File.open(file_name, 'w') do |output|

      output.puts "# multimarker definition file for ARToolKit (format defined by ARToolKit)\n"
      output.puts "# Papart MarkerBoard please fill the marker IDs. "
      output.write "\nNumber of Markers\n"
      output.puts @markers.length.to_s + "\n"

      @markers.each do |marker|
        output.puts "\n# marker"
        output.puts "0 ## FILL THE ID manually"

        w = marker.width
        half_width = -w/2

        output.puts w.to_s
        output.puts half_width.to_s + " " + half_width.to_s

        m = marker.mat

        output.puts(m.m00.to_s + " " + m.m01.to_s + " " + m.m02.to_s + " " + m.m03.to_s)
        output.puts(m.m10.to_s + " " + m.m11.to_s + " " + m.m12.to_s + " " + m.m13.to_s)
        output.puts(m.m20.to_s + " " + m.m21.to_s + " " + m.m22.to_s + " " + m.m23.to_s)

      end
    end

    p file_name + " saved !"
  end

  def load_markers

    ## Inkscape is 90 DPI -> we should downscale to 25.40 DPI to get
    ##  1 pixel per mm...
    $scale = (25.4 / 90.0)
    @markers = []

    scale = 1

    puts "Loading the markers..."

    @svg.css("image").each do |marker|

      # Get the transformation
      transform, w, h = get_global_transform marker

      transform.m03 = (transform.m03 * scale).round(3)
      transform.m13 = (transform.m13 * scale).round(3)
      transform.m23 = (transform.m23 * scale).round(3)
      w = (w * scale).round(3)
      h = (h * scale).round(3)

      transform.print
      @markers.push Marker.new(transform, w, h)
    end


    @markers = @markers.sort_by { |marker| marker.mat.m03 }

  end


  def to_s
    "markers #{@markers.size}"
  end

end
