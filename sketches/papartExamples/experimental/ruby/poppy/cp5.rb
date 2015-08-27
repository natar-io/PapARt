require 'jruby/core_ext'

Processing::App.load_library :skatolo

# module skatolo
#   include_package 'fr.inria.papart.skatolo'
# end

class EventHandler 
  attr_reader :applet
  
  def initialize skatolo
    @skatolo = skatolo
  end

  java_signature 'void controlEvent(fr.inria.skatolo.events.ControlEvent)'
  def controlEvent(controlEvent)
    @skatolo.sent_event_to_sketch controlEvent
  end

  EventHandler.become_java!
end

class skatolo < Java::FrInriaskatolo::skatolo

  def initialize (applet)
    @event_handler = EventHandler.new self

    super(applet, @event_handler)
    @applet = applet
  end

  def update
    
    getAll.to_a.each do |controller|
      name = controller.name
      ## There is a method with this name... 

      if not (@applet.respond_to? name + "_value")
        #        puts "please declare a method for " + name
        create_getter_for name
        create_setter_for name
      end
      
    end
  end

  # Event function... 
  def sent_event_to_sketch(controlEvent)
    controller = get(controlEvent.getName)
    name = controlEvent.getName
    value = controlEvent.getValue
    
    ## There is a method with this name... 
    if @applet.respond_to? name

      ## Buttons usually, not arguments. 
      @applet.send(name)  if is_event_class controller.class

      ## Sliders, check arity 
      if is_value_class controller.class

        ## try to send the value 
        @applet.send(name, value)
      end 
    end

  end
  

  def create_getter_for name
    controller = get(name)
    return if is_event_class controller.class

    value = get_controller_value(controller)

#    puts "Creating a getter for " + name

    @applet.create_method(name + "_value") do 
      controller = @skatolo.get(name)
      @skatolo.get_controller_value controller
    end

  end

  def create_setter_for name

    controller = get(name)
    return if is_event_class controller.class

    value = get_controller_value controller
#    puts "Creating a setter for " + name
    
    @applet.create_method(name + "_value=") do |value|
      controller = @skatolo.get(name)
      @skatolo.set_controller_value controller, value
    end

  end


  def get_controller_value controller
    return 1 if is_event_class controller.class
    return controller.getValue if is_value_class controller.class
    return controller.getStringValue  if is_string_value_class controller.class
  end

  def set_controller_value controller, value
    return if is_event_class controller.class
    return controller.setValue value  if is_value_class controller.class
    return controller.setStringValue value  if is_string_value_class controller.class
  end

  
  def is_event_class object_class
    object_class == Java::FrInriaskatoloGuiControllers::Button

  end

  def is_value_class object_class
    object_class == Java::FrInriaskatoloGuiControllers::Slider
  end
  
## TODO
  def is_string_value_class object_class
    False
    #    object_class == Java::FrInriaskatoloGuiControllers::Slider
  end
  

end
