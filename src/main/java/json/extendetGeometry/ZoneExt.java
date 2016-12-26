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

import java.util.ArrayList;

/**
 * Класс, расширяющий базовый {@link Zone}.
 * Предназначен для полей, которые не входят в *.json файл с геометрией
 * <p>
 * Created by boris on 17.12.16.
 */
public class ZoneExt extends Zone<LightExt, SensorExt, SpeakerExt> {
    /**
     * Минимальное и максимальное значение по оси Z
     */
    private double zMin = Double.MAX_VALUE;
    private double zMax = -Double.MIN_VALUE;
    /**
     * Направление движения по лестнице. (+3 - вверх, -3 - вниз)
     */
    private int    direction;
    /**
     * Номер эвакуационного выхода, в который идет движение из текущего
     * помещения
     */
    private int    numberExit;
    /**
     * Индекс, которые равен такому же индексу эвакуационного выхода. Признак
     * того, что помещение уже обрботано
     */
    private int    nTay;
    /**
     * Время движения до эвакуационного выхода
     */
    private double timeToReachExit;

    /**
     * Списко дверей, которые соединяются с зоной
     */
    private ArrayList<TransitionExt> transitionList = new ArrayList<>();

    /**
     * @return Минимальное значение по оси Z в пределах зоны
     */
    public double getMinZ() {
        // Если однажды посчитали, в дальнейшем возвращаем
        if (zMin == Double.MAX_VALUE) return zMin;

        for (int i = 0; i < getXyz().length; i++) {
            final double z = getXyz(0, i, 2);
            if (z <= zMin) zMin = z;
        }
        return zMin;
    }

    /**
     * @return Максимальное значение по оси Z в пределах зоны
     */
    public double getMaxZ() {
        if (zMax == -Double.MIN_VALUE) return zMax;

        for (int i = 0; i < getXyz().length; i++) {
            final double z = getXyz(0, i, 2);
            if (z >= zMax) zMax = z;
        }
        return zMax;
    }

    /**
     * Перепад высот в пределах зоны
     */
    public double getHeightDifference() {
        return getMaxZ() - getMinZ();
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

    /**
     * Позволяет задать направление движения по лестнице
     *
     * @param direction -3 - вниз  {@link Direction#DOWN}, +3 - вверх {@link
     *                  Direction#UP}
     */
    public void setDirection(int direction) {
        this.direction = direction;
    }

    /**
     * Позволяет увеличить количество людей в зоне на заданное число
     *
     * @param people - количество людей
     */
    public void addPeople(double people) {
        setNumOfPeople(getNumOfPeople() + people);
    }

    /**
     * Позволяет удалить заданное количество людей из зоны
     *
     * @param people количество людей
     */
    public void removePeople(double people) {
        setNumOfPeople(getNumOfPeople() - people);
    }

    public void setNumberExit(int numberExit) {
        this.numberExit = numberExit;
    }

    public void setNTay(int nTay) {
        this.nTay = nTay;
    }

    /**
     * Позволяет установить время достижения эвакуационного выхода из
     * текущего помещения
     *
     * @param timeToReachExit время
     */
    public void setTimeToReachExit(double timeToReachExit) {
        this.timeToReachExit = timeToReachExit;
    }

    /**
     * @return Списко дверей, которые соединятются с текущей зоной
     */
    public ArrayList<TransitionExt> getTransitionList() {
        return transitionList;
    }

    /**
     * @param i номер двери (индекс списка)
     *
     * @return Дверь по индексу ({@link TransitionExt}
     */
    public TransitionExt getTransition(int i) {
        return getTransitionList().get(i);
    }

    /**
     * @param tUuid строковый идентификатор двери
     *
     * @return Дверь по uuid ({@link TransitionExt}
     */
    public TransitionExt getTransition(String tUuid) {
        for (TransitionExt t : getTransitionList()) {
            if (t.getId().equals(tUuid)) return t;
        }
        throw new NullPointerException(
                "The Transition is not find. Incorrect UUID or the Transition"
                        + " does not exist");
    }

    /**
     * Позволяет добавить дверь в список
     *
     * @param t экземпляр класса {@link TransitionExt}
     */
    void addTransition(TransitionExt t) {
        getTransitionList().add(t);
    }
}
