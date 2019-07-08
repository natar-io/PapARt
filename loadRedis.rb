require "redis"

@redis = Redis.new

def read_to_redis(file, output_key)
  puts "Reading " + file.to_s
  content = File.read(file)
  puts "Set contents to " + output_key.to_s
  @redis.set(output_key, content)
end
  
read_to_redis "cameraConfiguration.xml", "papart:cameraConfiguration"
read_to_redis "TouchColorCalibration.xml", "papart:touchColorCalib"
read_to_redis "ColorZoneCalibration.xml", "papart:colorZoneCalibration"
read_to_redis "Touch2DCalibration.xml", "papart:touchCalibration"
read_to_redis "Touch3DCalibration.xml", "papart:touchCalibration3D"

(0...2).each {|i| read_to_redis "TouchCalibration#{i.to_s}.xml", "papart:touchCalibrations:#{i.to_s}"}
