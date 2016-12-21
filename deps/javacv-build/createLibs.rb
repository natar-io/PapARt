#!/bin/ruby


platforms = ["linux", "windows"]
archs = ["x86_64", "x86"]


def build(platform, arch)

  puts "Build " + platform
  %x(cp pom-#{platform}.xml pom.xml)
  
  puts "Get the library"

  %x(mvn -Dplatform=#{platform}-#{arch} dependency:copy-dependencies)

  %x(rm target/dependency/*linux*)   if platform.eql? "windows"

  `mv target/dependency target/library`
  `mv target javacv`
  `mv javacv/library/javacv-1.3.jar javacv/library/javacv.jar`
  
  puts "compress library"
  `tar -zcf javacv-#{platform}-#{arch}.tgz javacv`
  `rm -rf javacv`
  
  puts "done javacv-" + platform + "-" + arch
  
  %x(rm pom.xml)
end

build("linux", "x86_64")
build("windows", "x86_64")
build("windows", "x86")

# mvn -Dplatform=windows-x86_64 dependency:copy-dependencies
