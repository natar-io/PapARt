/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2014-2016 Inria
 * Copyright (C) 2011-2013 Bordeaux University
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.inria.papart.calibration;

///*
// * Copyright (C) 2014 Jeremy Laviole laviole@rea.lity.tech.
// *
// * This library is free software; you can redistribute it and/or
// * modify it under the terms of the GNU Lesser General Public
// * License as published by the Free Software Foundation; either
// * version 2.1 of the License, or (at your option) any later version.
// *
// * This library is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// * Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public
// * License along with this library; if not, write to the Free Software
// * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
// * MA 02110-1301  USA
// */
//package fr.inria.papart.depthcam.calibration;
//
//import org.junit.Test;
//import static org.junit.Assert.*;
//import processing.core.PVector;
//
///**
// *
// * @author Jeremy Laviole laviole@rea.lity.tech
// */
//public class HomographyCreatorTest {
//
//    static final int NB_POINTS = 10;
//    static final int DIMS = 3;
//
//    HomographyCreator instance;
//
//    public HomographyCalibration createHomography() {
//        instance = new HomographyCreator(DIMS, DIMS, NB_POINTS);
//        createPoints();
//        assertTrue(instance.getHomography() != null);
//        return instance.getHomography();
//    }
//
//    @Test
//    public void testAddPoints() {
//        instance = new HomographyCreator(DIMS, DIMS, NB_POINTS);
//        createPoints();
//        assertTrue(instance.getHomography() != null);
//    }
//
//    private void createPoints() {
//        for (int i = 0; i < NB_POINTS - 1; i++) {
//            boolean computed = instance.addPoint(new PVector(), new PVector());
//            assertFalse(computed);
//        }
//        boolean computed = instance.addPoint(new PVector(), new PVector());
//        assertTrue(computed);
//    }
//
//}
