#!/bin/ruby
# coding: utf-8


# %x(sh downloadExamples.sh)
# %x(sh createLibs.sh)

current_branch = %x(git rev-parse --abbrev-ref HEAD).chomp

output_names=["realsense"]
              # "kinect",
              # "hardware2",
              # "hardware3"]

output_names.each do |release_name|
  puts "Get the version: " + release_name
  %x(git checkout release-#{release_name})
  puts "Build it."
  %x(sh build.sh)  
  puts "Create redistribuable."
  %x(sh createRedist.sh #{release_name}) 
end

%x(git checkout #{current_branch})
