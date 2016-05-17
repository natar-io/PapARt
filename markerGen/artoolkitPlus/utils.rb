def check_arguments

  if ARGV.length != 1
    p "Requires a svg file as argument, no argument passed."
    p "Usage jruby gen.rb <input.svg> "
    p "  e.g. : jruby gen.rb board1.svg"
    exit
  end

  if not ARGV[0].end_with? ".svg"
    p "Requires a svg file as argument (ends with .svg)."
    exit
  end


end
