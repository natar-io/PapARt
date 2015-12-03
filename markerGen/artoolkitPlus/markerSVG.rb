require 'ostruct'
require 'base64'


class Marker
  attr_reader :mat, :width, :height, :id

  def initialize (mat, width, height, id)
    @mat, @width, @height, @id = mat, width, height, id
  end
end


class MarkerBoard

  include_package 'processing.core'

  include Papart

  attr_accessor :markers, :pshape, :svg, :transform
  attr_accessor :width, :height, :matrix

  ## Inkscape is 90 DPI -> we should downscale to 25.40 DPI to get
  ##  1 pixel per mm...
  def pixel_to_mm ; (25.4 / 90.0) ; end
  def mm_to_pixel ; 1.0 / pixel_to_mm ; end

  ## Parse the Imagse

  def initialize (url)
    @url = url
    @svg = Nokogiri::XML(open(@url)).children[1]

    @height = compute_height
    load_markers
  end


  def compute_height
    height_text = @svg.attributes["height"].value

    ## convert
    if height_text.end_with? "mm"
      height = (height_text.split "mm")[0].to_f
      return height * mm_to_pixel
    else

      height = (height_text.split "px")[0].to_f
      return height

    end

    p "Error: finding the height of the SVG, create it with Inkscape "
    p "Error: save as an Inkscape SVG file with size in pixels or millimeters."
    exit
  end

  def save_as file_name

    File.open(file_name, 'w') do |output|

      output.puts "# multimarker definition file for ARToolKit (format defined by ARToolKit)\n"
      output.puts "# Papart MarkerBoard please fill the marker IDs. "
      output.write "\n#Number of Markers\n"
      output.puts @markers.length.to_s + "\n"

      @markers.each do |marker|
        output.puts "\n# marker"
        output.puts marker.id.to_s

        w = marker.width
        half_width = w/2

        output.puts w.to_s

        offset_x = half_width
        offset_y = half_width

        output.puts offset_x.to_s + " " + offset_y.to_s

        m = marker.mat

        output.puts(m.m00.to_s + " " + m.m01.to_s + " " + m.m02.to_s + " " + m.m03.to_s)
        output.puts(m.m10.to_s + " " + m.m11.to_s + " " + m.m12.to_s + " " + m.m13.to_s)
        output.puts(m.m20.to_s + " " + m.m21.to_s + " " + m.m22.to_s + " " + m.m23.to_s)

      end
    end

    p file_name + " saved !"
  end


  def load_markers

    scale = pixel_to_mm
    @markers = []

    puts "Loading the markers..."

    @svg.css("rect").each do |rect|

      id_text = rect.attributes["id"].value
      # next if not id_text.start_with? "rect"

      ## get as is int
      #      id = (id_text.split("rect")[1]).to_i

      # Get the transformation
      transform, w, h = get_global_transform rect

      transform.scale(1, -1, 1)
      transform.translate(0, -h, 0)


      ## Going to mm sizes instead of pixels
      transform.m03 = transform.m03 * scale
      transform.m13 = transform.m13 * scale
      transform.m23 = transform.m23 * scale

      # round to obtain human-readable text file.
      transform.m03 = transform.m03.round(3)
      transform.m13 = transform.m13.round(3)
      transform.m23 = transform.m23.round(3)

      p "Rect " + id_text.to_s + " found in "
      transform.print
    end



    @svg.css("image").each do |marker|

      id_text = marker.attributes["id"].value
      next if not id_text.start_with? "marker"

      ## get as is int
      id = (id_text.split("marker")[1]).to_i

      # Get the transformation
      transform, w, h = get_global_transform marker

      transform.scale(1, -1, 1)
      transform.translate(0, -h, 0)


      ## Going to mm sizes instead of pixels
      transform.m03 = transform.m03 * scale
      transform.m13 = transform.m13 * scale
      transform.m23 = transform.m23 * scale

      # round to obtain human-readable text file.
      transform.m03 = transform.m03.round(3)
      transform.m13 = transform.m13.round(3)
      transform.m23 = transform.m23.round(3)
      w = (w * scale).round(3)
      h = (h * scale).round(3)

      @markers.push Marker.new(transform, w, h, id)
    end


    @markers = @markers.sort_by { |marker| marker.id }
    # @markers = @markers.sort_by { |marker| marker.mat.m03 }
    puts @markers.size.to_s + " marker(s) found"

  end


  def to_s
    "markers #{@markers.size}"
  end

end
