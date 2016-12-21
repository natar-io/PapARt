#!/bin/ruby

puts "Get the library"

%x(mvn dependency:copy-dependencies)

`mv target/dependency target/library`
`mv target reflections`

`mv reflections/library/reflections-0.9.9.jar reflections/library/reflections.jar`

puts "compress library"
`tar -zcf reflections.tgz reflections`


`rm -rf reflections`

puts "done reflections"

# mvn -Dplatform=windows-x86_64 dependency:copy-dependencies
