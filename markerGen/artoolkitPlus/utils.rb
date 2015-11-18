def check_arguments

  if ARGV.length != 3
    p "Requires a svg file as argument, no argument passed."
    p "Usage jruby gen.rb <input.svg> <offsetX> <offsetY> "
    p "  e.g. : jruby gen.rb board1.svg 61.0 44.0"
    exit
  end

  if not ARGV[0].end_with? ".svg"
    p "Requires a svg file as argument (ends with .svg)."
    exit
  end


  if not ARGV[0].end_with? ".svg"
    p "Requires a svg file as argument (ends with .svg)."
    exit
  end


end
