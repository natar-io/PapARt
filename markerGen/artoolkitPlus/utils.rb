def check_arguments
  if ARGV.length == 0
    p "Requires a svg file as argument, no argument passed."
    p "Usage ruby gen.rb <input.svg> <output.cfg>"
    exit
  end
  if not ARGV[0].end_with? ".svg"
    p "Requires a svg file as argument (ends with .svg)."
    exit
  end
end
