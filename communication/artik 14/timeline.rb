# coding: utf-8
## re-open the class

class SobyPlayer
  attr_accessor :main_presentation, :timeline_presentation

  ## To be overriden by the Presentation Code.
  def custom_setup
    @debug = true
  end


  def slide_change
    @do_once = true
  end


  def custom_pre_draw
    background 255

  end

  def custom_post_draw
    return unless @use_debug

    if @current_slide_no > 0
      textFont(@font, 25)
      fill(255)
      text_content = "[DEBUG] slide name: " + @slides[@current_slide_no].title.to_s
      text(text_content, 100, @height - 70)
    end

  end

  ## disable slide number
  def display_slide_number
  end

end
