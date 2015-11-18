module Papart

  include_package 'processing.core'

  def read_transform (text)
    if(text.match("matrix"))
      text = text.split(/[,\(]/)  # split with  , or  (
      return PMatrix2D.new(text[1].to_f, text[3].to_f, text[5].to_f, \
                           text[2].to_f, text[4].to_f, text[6].to_f)   if(text.size == 7)
    end

    if(text.match("translate"))
      text = text.split(/[,\(]/)
      return PMatrix2D.new(1, 0, text[1].to_f, 0, 1, text[2].to_f)
    end
  end


  def get_global_transform element
    e = element
    global_transform = PMatrix3D.new

    attr = e.attributes

    global_transform.translate(attr["x"].value.to_f, attr["y"].value.to_f)

    while  e.class != Nokogiri::XML::Document do
      t = e.attribute("transform")
      if (t != nil)
        tr = read_transform(t.value)
        global_transform.preApply(tr)
      end
      e = e.parent
    end

    [global_transform , attr["width"].value.to_f, attr["height"].value.to_f]
  end

  def get_angle (mat)
    Math.atan2(mat.m01, mat.m00)
  end


  def create_mat4x4(mat)
    Matrix4x4.new(mat.m00, mat.m01, mat.m02, mat.m03,\
                  mat.m10, mat.m11, mat.m12, mat.m13,\
                  mat.m20, mat.m21, mat.m22, mat.m23,\
                  mat.m30, mat.m31, mat.m32, mat.m33)
  end

end
