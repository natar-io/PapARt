#!/bin/ruby


platforms = ["linux", "windows", "macosx"]
archs = ["x86_64", "x86", "arm", "arm64"]


def build(platform, arch)

  puts "Build " + platform

  %x(mvn clean)
  # if(platform == "linux")
  %x(cp pom-#{platform}.xml pom.xml)
  #else
  #  %x(cp pom-other.xml pom.xml)
  #end

  puts "Get the library"
  # mvn -Djavacpp.platform=macosx-arm64 dependency:copy-dependencies -f pom-macosx.xml
  %x(mvn -Djavacpp.platform=#{platform}-#{arch} dependency:copy-dependencies -f pom-#{platform}.xml)

  #%x(rm target/dependency/*linux*)   if platform.eql? "windows"
  #%x(rm target/dependency/*linux*)   if platform.eql? "macosx"

  `mv target/dependency target/library`
  `mv target javacv`
  `mv javacv/library/javacv-1.5.7.jar javacv/library/javacv.jar`

  puts "compress library"
  `tar -zcf javacv-#{platform}-#{arch}.tgz javacv`
  `rm -rf javacv`

  puts "done javacv-" + platform + "-" + arch

  # %x(rm pom.xml)
end

# build("linux", "armhf")
# build("android", "arm")
#build("linux", "x86_64")
build("macosx", "arm64")
#build("windows", "x86_64")
# build("macosx", "x86_64")
# build("windows", "x86")

# mvn -Dplatform=windows-x86_64 dependency:copy-dependencies
