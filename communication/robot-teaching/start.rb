require 'soby' 

# Give the current folder to Processing.
Processing::App::SKETCH_PATH = __FILE__

#$app = nil
$app =  SobyPlayer.new 800,600 if $app == nil

sleep 0.2 while not $app.ready? 

$:.unshift File.dirname(__FILE__)

@main_file = 'robot.svg'

def load_prez
  load 'papart.rb'
  @robot_prez = Presentation.new($app, $app.sketchPath(@main_file))
  $app.robot_prez = @robot_prez
end

def start_prez
  $app.set_prez @robot_prez  
end

def find_files
  @files = Dir.glob(File.join(SKETCH_ROOT, "**/*.{svg,glsl}"))
end

find_files
load_prez
start_prez


@time = Time.now 
SLEEP_TIME = 1

t = Thread.new {
  loop do
    if @files.find { |file| FileTest.exist?(file) && File.stat(file).mtime > @time }
      puts 'reloading sketch...'

      @time = Time.now
      
      load 'papart.rb'
      load_prez
      start_prez
      
    end
    sleep SLEEP_TIME
  end
}
  
