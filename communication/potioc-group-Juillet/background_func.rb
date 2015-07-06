# coding: utf-8

class Background

  attr_reader :squares
  attr_accessor :update_probability

  def initialize width, height, applet
    @applet = applet
    @width, @height = width, height

    light
    set_square_size 35

    @update_probability = 1
    @last_update = 0
  end 

  
  def dark
    # min, max, amp_pos, amp_neg
    Square.set(10, 30, 10, 10)
  end

  def light
    # min, max, amp_pos, amp_neg
    Square.set(220, 250, 10, 10)
  end

  def lighter
    # min, max, amp_pos, amp_neg
    Square.set(220, 255, 2, 10)
  end

  def darker
    # min, max, amp_pos, amp_neg
    Square.set(220, 255, 8, 5 )
  end

  
  
  def set_square_size (size)
    
    Square.size = size
    @squares = Hash.new

    @nb_squares_x = @width  / Square.size + 1
    @nb_squares_y = @height / Square.size + 1
    (0..@nb_squares_x).each do |x|
      (0..@nb_squares_y).each do |y|
        @squares[[x,y]] = Square.new x, y
      end
    end
  end

  def set_left_line
    (1...@nb_squares_y).each do |y|
      @squares[[0,y]].color = 100
    end
  end


 def to_s
   "Background "
 end
 
 def update time

   # get_subset(0.01).each do |coordinates, square|
   #   square.crazy
   # end

   return if $app.random(100) < 10

   get_subset(@update_probability).each do |coordinates, square|
     square.update

     # neighbours =[]
     # area = 1
     # ((coordinates[0] - area)..(coordinates[0] + area)).each do |x|
     #   ((coordinates[1] - area)..(coordinates[1] + area)).each do |y|
     #     neighbours << @squares[[x,y]] if @squares[[x,y]] != nil 
     #   end
     # end
     
     # square.update_neighbours neighbours
   end
 end

  def get_subset probabilty
    @squares.select { $app.random(100) < probabilty }
  end

 
 def draw g
   @squares.each do |coordinates, square|
     square.draw g, coordinates[0], coordinates[1]
   end
 end
 
 
end

class Square

  attr_accessor :color
  attr_reader :x, :y
  attr_reader :alive
  
  def default_color
    (Square.max - Square.min) / 2 + Square.min + Square.increase
  end
                                          
  def initialize x,y 
    @color = default_color;
    @x, @y = x,y

    ## 10 % chances to be alive
#    @alive = $app.random(100) < 20
  end

  def crazy
    @alive = true
  end

  def update
    @color = @color + Square.increase
    @color = $app.constrain(@color, Square.min, Square.max)
  end

  def update_neighbours neighbours
    average = neighbours.map { |square| square.color }.reduce (:+)
    @color = average / neighbours.size
  end

  
  def update_neighbours neighbours
    ## Patch update
#       neighbours.each { |square|  square.update } 

    ## all same values update    
    # average = neighbours.map { |square| square.color }.reduce (:+)
    # @color = average / neighbours.size

    ## average on left
    xinf = neighbours.select { |square| square.x <= @x }
    return if xinf == nil || xinf.size == 0
    c  = xinf.map {|s| s.color }.reduce(:+)
    @color = ((@color * 1) + c) / (xinf.size + 1)


    ## Game of life
    # nb_alive = neighbours.map { |square| square.alive == true ? 1 : 0 }.reduce(:+)
    # if @alive
    #   @alive = nb_alive == 2 or nb_alive == 3
    # else
    #   @alive = true if nb_alive == 3
    # end
    
  end

    
  def draw (graphics, x , y)

    #    graphics.fill(@alive ? 150 : @color)
    # graphics.stroke 180
    # graphics.strokeWeight 1
    graphics.fill(@color)

    graphics.rect(x*(Square.size() + 3),y * (Square.size() +3), Square.size(), Square.size())
#    graphics.rect(x * 10 , y *10 , 10, 10);
  end

  class << self
    attr_accessor :size, :min, :max, :amp_pos, :amp_neg

    def set(min, max, amp_neg, amp_pos)
      @min, @max, @amp_pos, @amp_neg = min, max, amp_pos, amp_neg
    end

    def increase
      $app.random(-@amp_neg, @amp_pos)
    end

  end
  
end
