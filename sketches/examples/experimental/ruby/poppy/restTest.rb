# coding: utf-8
require 'json'
require 'rest-client'

class Poppy 

  attr_reader :ip, :port, :url, :motors

  def initialize
    @host = "193.50.110.242"
    @port = "8080"
    @url = "http://" + @host + ":" + @port  
    @motors[]
  end

  def motor_list_text
    RestClient.get @url + '/motor/list.json'
  end

  def motor_list
    @motors = JSON.parse(motorListText).values[0]
  end

  def create_values
    @motors.each do |name|
      instance_variable_set("@#{name}", motor_values(name))
      self.class.send(:attr_accessor, name)
    end
  end

  def motor_values (name) 
    RestClient.get @url + '/motor/' + name + '/register/list.json' , {:accept => :json}
  end

  def send_motor (value)
    json_text = value
#    RestClient.post @url + "/motor/head_y/register/present_position/value.json", json_text, :content_type => :json, :accept => :json
    RestClient.post @url + "/motor/head_z/register/goal_position/value.json", json_text, :content_type => :json 
  end
  
  def test
    RestClient.get @url + "/motor/head_y/register/present_position"
#    RestClient.get @url + "/motor/head_y/register/compliant"

    # json_text = "{\"robot\": {\"get_motors_list\": {\"alias\": \"motors\"}}}"
    # RestClient.post @url, json_text, :content_type => :json, :accept => :json
  end

  
end


json_text = "10"
post_ws = ""
host = "193.50.110.242"
port = "8080"

req = Net::HTTP::Post.new("/motor/head_y/register/goal_position/value.json", initheader = {'Content-Type' =>'application/json'})
#  req.basic_auth @user, @pass
req.body = json_text
response = Net::HTTP.new(host, port).start {|http| http.request(req) }
puts "Response #{response.code} #{response.message}:#{response.body}"

# test ={
#     "name" => "api-workspace",
#     "title" => "API Workspace",
#     "account_id" => "1"
#   }.to_json
