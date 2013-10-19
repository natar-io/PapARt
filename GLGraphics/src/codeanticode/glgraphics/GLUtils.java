/**
 * This package provides classes to facilitate the handling of opengl textures, glsl shaders and 
 * off-screen rendering in Processing.
 * @author Andres Colubri
 * @version 0.95
 *
 * Copyright (c) 2008 Andres Colubri
 *
 * This source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * A copy of the GNU General Public License is available on the World
 * Wide Web at <http://www.gnu.org/copyleft/gpl.html>. You can also
 * obtain it by writing to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package codeanticode.glgraphics;

import javax.media.opengl.*;
import processing.core.PConstants;

/**
 * @invisible This class provides some utilities functions.
 */
public class GLUtils implements PConstants, GLConstants {

  static public int parsePrimitive(int type) {
    if (type == POINTS)
      return GL.GL_POINTS;
    else if (type == POINT_SPRITES)
      return GL.GL_POINTS;
    else if (type == LINES)
      return GL.GL_LINES;
    else if (type == LINE_STRIP)
      return GL.GL_LINE_STRIP;
    else if (type == LINE_LOOP)
      return GL.GL_LINE_LOOP;
    else if (type == LINE_ADJACENCY)
      return GL.GL_LINES_ADJACENCY_EXT;    
    else if (type == TRIANGLES)
      return GL.GL_TRIANGLES;
    else if (type == TRIANGLE_FAN)
      return GL.GL_TRIANGLE_FAN;
    else if (type == TRIANGLE_STRIP)
      return GL.GL_TRIANGLE_STRIP;
    else if (type == TRIANGLE_ADJACENCY)
      return GL.GL_TRIANGLES_ADJACENCY_EXT;      
    else if (type == QUADS)
      return GL.GL_QUADS;
    else if (type == QUAD_STRIP)
      return GL.GL_QUAD_STRIP;
    else if (type == POLYGON)
      return GL.GL_POLYGON;  
    else {
      System.err.println("Unrecognized geometry mode. Using points.");
      return GL.GL_POINTS;
    }  
  }
  
  static public int parsePrimitive(String type) {
    String typeUp = type.toUpperCase();    
    if (typeUp.equals("POINTS"))
      return GL.GL_POINTS;
    else if (typeUp.equals("POINT_SPRITES"))
      return GL.GL_POINTS;
    else if (typeUp.equals("LINE_STRIP"))
      return GL.GL_LINE_STRIP;
    else if (typeUp.equals("LINE_LOOP"))
      return GL.GL_LINE_LOOP;    
    else if (typeUp.equals("LINE_ADJACENCY"))
      return GL.GL_LINES_ADJACENCY_EXT;
    else if (typeUp.equals("LINES"))
      return GL.GL_LINES;
    else if (typeUp.equals("TRIANGLE_STRIP"))
      return GL.GL_TRIANGLE_STRIP;
    else if (typeUp.equals("TRIANGLE_FAN"))
      return GL.GL_TRIANGLE_FAN;
    else if (typeUp.equals("TRIANGLE_ADJACENCY"))
      return GL.GL_TRIANGLES_ADJACENCY_EXT;    
    else if (typeUp.equals("TRIANGLES"))
      return GL.GL_TRIANGLES;
    else if (typeUp.equals("QUAD_STRIP"))
      return GL.GL_QUAD_STRIP;
    else if (typeUp.equals("QUADS"))
      return GL.GL_QUADS;
    else if (typeUp.equals("POLYGON"))
      return GL.GL_POLYGON;
    else {
      System.err.println("Unrecognized geometry mode. Using points.");
      return GL.GL_POINTS;
    }
  }

  static public int parseVBOMode(String modeStr) {
    if (modeStr.equals("STATIC"))
      return GL.GL_STATIC_DRAW_ARB;
    else if (modeStr.equals("DYNAMIC"))
      return GL.GL_DYNAMIC_DRAW_ARB;
    else if (modeStr.equals("STREAM"))
      return GL.GL_STREAM_COPY;
    else {
      System.err.println("Unrecognized VBO mode mode. Using static.");
      return GL.GL_STATIC_DRAW_ARB;
    }
  }

  static void printFramebufferError(int status) {
    if (status == GL.GL_FRAMEBUFFER_COMPLETE_EXT)
      return;
    else if (status == GL.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT) {
      System.err
          .println("Frame buffer is incomplete (GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT)");
    } else if (status == GL.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT) {
      System.err
          .println("Frame buffer is incomplete (GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT)");
    } else if (status == GL.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT) {
      System.err
          .println("Frame buffer is incomplete (GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT)");
    } else if (status == GL.GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT) {
      System.err
          .println("Frame buffer is incomplete (GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT)");
    } else if (status == GL.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT) {
      System.err
          .println("Frame buffer is incomplete (GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT)");
    } else if (status == GL.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT) {
      System.err
          .println("Frame buffer is incomplete (GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT)");
    } else if (status == GL.GL_FRAMEBUFFER_UNSUPPORTED_EXT) {
      System.err
          .println("Frame buffer is incomplete (GL_FRAMEBUFFER_UNSUPPORTED_EXT)");
    } else {
      System.err.println("Frame buffer is incomplete (unknown error code)");
    }
  }
}
