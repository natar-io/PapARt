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

import processing.xml.*;

/**
 * @invisible GLTexturedPoint stores an (x, y) point and an array of associated
 *            texture coordinates (s[i], t[i]).
 */
public class GLTexturedPoint {
  public GLTexturedPoint() {
    x = y = 0.0f;
    initTexCoordArrays(1);
  }

  public GLTexturedPoint(XMLElement xml) {
    x = y = 0.0f;

    int n = xml.getChildCount();
    if (n <= 1)
      initTexCoordArrays(1);
    else
      initTexCoordArrays(n - 1);

    XMLElement child;
    String name, attr0, attr1, attr2, attr3;
    int tIdx = 0;
    float sVal, tVal, dsVal, dtVal;
    for (int i = 0; i < n; i++) {
      child = xml.getChild(i);
      name = child.getName();
      if (name.equals("coord")) {
        attr0 = child.getStringAttribute("x");
        attr1 = child.getStringAttribute("y");
        attr2 = child.getStringAttribute("dx");
        attr3 = child.getStringAttribute("dy");

        if (attr0.equals("x"))
          x = -1.0f;
        else
          x = child.getFloatAttribute("x");

        if (attr1.equals("y"))
          y = -1.0f;
        else
          y = child.getFloatAttribute("y");

        if (attr2 == null)
          dx = 0.0f;
        else if (attr2.equals("dx"))
          dx = -1.0f;
        else
          dx = child.getFloatAttribute("dx");

        if (attr3 == null)
          dy = 0.0f;
        else if (attr3.equals("dy"))
          dy = -1.0f;
        else
          dy = child.getFloatAttribute("dy");
      } else if (name.equals("texcoord")) {
        attr0 = child.getStringAttribute("s");
        attr1 = child.getStringAttribute("t");
        attr2 = child.getStringAttribute("ds");
        attr3 = child.getStringAttribute("dt");

        if (attr0.equals("s"))
          sVal = -1.0f;
        else
          sVal = child.getFloatAttribute("s");

        if (attr1.equals("t"))
          tVal = -1.0f;
        else
          tVal = child.getFloatAttribute("t");

        if (attr2 == null)
          dsVal = 0.0f;
        else if (attr2.equals("ds"))
          dsVal = -1.0f;
        else
          dsVal = child.getFloatAttribute("ds");

        if (attr3 == null)
          dtVal = 0.0f;
        else if (attr3.equals("dt"))
          dtVal = -1.0f;
        else
          dtVal = child.getFloatAttribute("dt");

        setTexCoords(tIdx, sVal, tVal);
        setTexCoordsDelta(tIdx, dsVal, dtVal);
        tIdx++;
      }
    }
  }

  public GLTexturedPoint(int n) {
    x = y = 0.0f;
    initTexCoordArrays(n);
  }

  public GLTexturedPoint(float x, float y, float s, float t) {
    this.x = x;
    this.y = y;
    initTexCoordArrays(1);
    this.s[0] = s;
    this.t[0] = t;
  }

  public void setTexCoords(int n, float s, float t) {
    if (n < ntex) {
      this.s[n] = s;
      this.t[n] = t;
    }
  }

  public void setTexCoordsDelta(int n, float ds, float dt) {
    if (n < ntex) {
      this.ds[n] = ds;
      this.dt[n] = dt;
    }
  }

  public void setAsUndefined() {
    x = y = -1.0f;
    dx = dy = -1.0f;
    for (int i = 0; i < ntex; i++) {
      s[i] = t[i] = -1.0f;
      ds[i] = dt[i] = -1.0f;
    }
  }

  public void scaleCoords(int w, int h) {
    x *= w;
    y *= h;
  }

  public void genTexCoords(int w, int h) {
    for (int i = 0; i < ntex; i++) {
      s[i] = x / w;
      t[i] = y / h;
      ds[i] = 1.0f / w;
      dt[i] = 1.0f / h;
    }
  }

  public void initTexCoordArrays(int n) {
    ntex = n;
    s = new float[ntex];
    t = new float[ntex];
    ds = new float[ntex];
    dt = new float[ntex];
    for (int i = 0; i < ntex; i++) {
      s[i] = t[i] = 0.0f;
      ds[i] = dt[i] = 0.0f;
    }
  }

  public float x, y, dx, dy;
  public float[] s, t, ds, dt;
  public int ntex;
}
