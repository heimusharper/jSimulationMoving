/*******************************************************************************
 * Copyright (C) 2016 Chirkov Boris <b.v.chirkov@udsu.ru>
 *
 * Project website:       http://eesystem.ru
 * Organization website:  http://rintd.ru
 *
 * --------------------- DO NOT REMOVE THIS NOTICE -----------------------------
 * ZoneExt is part of jSimulationMoving.
 *
 * jSimulationMoving is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jSimulationMoving is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jSimulationMoving. If not, see <http://www.gnu.org/licenses/>.
 * -----------------------------------------------------------------------------
 *
 * This code is in BETA; some features are incomplete and the code
 * could be written better.
 ******************************************************************************/

package json.extendetGeometry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import json.geometry.Zone;

/**
 * Created by boris on 17.12.16.
 */
public class ZoneExt extends Zone<LightExt, SensorExt, SpeakerExt> {

    private double heightDifference = -1;

    /**
     * Перепад высот в пределах зоны
     */
    public double getHeightDifference() {
        if (heightDifference > -1) return heightDifference;

        double zMin = Double.MAX_VALUE;
        double zMax = -Double.MIN_VALUE;
        for (int i = 0; i < getXyz().length; i++) {
            final double z = getXyz(0, i, 2);
            if (z >= zMax) zMax = z;
            if (z <= zMin) zMin = z;
        }
        heightDifference = zMax - zMin;
        return heightDifference;
    }

    /**
     * Приватный метод, возвращающий полигон на структуре XY
     */
    private Polygon getPolygon() {
        double[][][] xyz = getXyz();
        if (xyz == null)
            return null; // если геометрии нет, то и в полигон не превратить
        GeometryFactory mGF = new GeometryFactory();
        // переструктурируем геометрию колец в Coordinate[][]
        // внешнее кольцо
        Coordinate[] geomOut = new Coordinate[xyz[0].length];
        for (int l = 0; l < xyz[0].length; l++) {
            geomOut[l] = new Coordinate(xyz[0][l][0], xyz[0][l][1]);
        }
        // внутренние кольца
        LinearRing[] internalRings = null;
        Coordinate[][] geomInt;
        if (xyz.length >= 2) { // если внутренние кольца есть
            geomInt = new Coordinate[xyz.length - 1][];
            internalRings = new LinearRing[xyz.length - 1];
            // Начиная с первого кольца (не с нулевого)
            for (int k = 1; k < xyz.length; k++) {
                geomInt[k - 1] = new Coordinate[xyz[k].length];
                for (int l = 0; l < xyz[k].length; l++) {
                    geomInt[k - 1][l] = new Coordinate(xyz[k][l][0],
                            xyz[k][l][1]);
                }
                internalRings[k - 1] = new LinearRing(
                        new CoordinateArraySequence(geomInt[k - 1]), mGF);
            } // for
        } // if

        return new Polygon(
                new LinearRing(new CoordinateArraySequence(geomOut), mGF),
                internalRings, mGF);
    }

    /**
     * @return Площадь зоны
     */
    public double getArea() {
        Polygon mP = getPolygon();
        if (mP != null) return mP.getArea();
        else return 0;
    }

    /**
     * @return Периметр зоны
     */
    public double getPerimeter() {
        Polygon mP = getPolygon();
        if (mP != null) return mP.getLength();
        else return 0;
    }
}
