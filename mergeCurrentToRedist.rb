#!/bin/ruby




current_branch = %x(git rev-parse --abbrev-ref HEAD).chomp

output_names=["realsense",
              "kinect",
              "hardware2",
              "hardware3"]

output_names.each do |release_name|
  puts %x(git checkout release-#{release_name})
  puts %x(git merge --commit -m "automatic merge for deployment" #{current_branch})

  puts %x(git push github release-#{release_name})
end

puts %x(git checkout #{current_branch})
