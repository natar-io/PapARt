#!/bin/ruby
# coding: utf-8


# %x(sh downloadExamples.sh)

current_branch = %x(git rev-parse --abbrev-ref HEAD).chomp

%x(sh build.sh)  
%x(sh createRedist.sh withExamples ex)
%x(sh createRedist.sh default) 

output_names=["realsense",
              "kinect",
              "hardware2",
              "hardware3"]

output_names.each do |release_name|
  puts "Get the version: " + release_name
  %x(git checkout release-#{release_name})

  ## For now the codebase is the same, only the data changes. 
  #  puts "Build it."
  #  %x(sh build.sh)  
  puts "Create redistribuable."
  %x(sh createRedist.sh #{release_name}) 
end

%x(git checkout #{current_branch})
