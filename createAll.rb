#!/bin/ruby
# coding: utf-8


`sh downloadExamples.sh`
`sh createLibs.sh`


output_names=["realsense"]
              # "kinect",
              # "hardware2",
              # "hardware3"]

output_names.each do |release_name|
  %x(git checkout release-#{release_name})
  %x(sh build.sh)  
  %x(sh createRedist.sh #{release_name}) 
end


