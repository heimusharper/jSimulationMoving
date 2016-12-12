/******************************************************************************
 Copyright (C) 2016 Galiullin Marat

 Project website:       http://eesystem.ru
 Organization website:  http://rintd.ru

 --------------------- DO NOT REMOVE THIS NOTICE ------------------------------
 This file is part of SimulationMoving.

 SimulationMoving is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 SimulationMoving is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with SimulationMoving. If not, see <http://www.gnu.org/licenses/>.
 ------------------------------------------------------------------------------

 This code is in BETA; some features are incomplete and the code
 could be written better.
 *****************************************************************************/

package json.geometry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

/**
 * Класс, описывающий зону внутри помещения
 *
 * @author mag
 */
public class Zone {

    /**
     * Идетнификатор зоны в формате UUID. <br>
     * <hr>
     * UUID представляет собой 16-байтный (128-битный) номер. <br>
     *
     * @Example В шестнадцатеричной системе счисления UUID выглядит как:<br>
     * 550e8400-e29b-41d4-a716-446655440000
     */
    private String id;

    /**
     * Высота потолка зоны в метрах
     */
    private double ceilingHeight;

    /**
     * Тип зоны по пожарной нагрузке. Цифровой идентификатор вида пространства.
     * <ol>
     * <li>Жилые помещения гостиниц, общежитий и т. д.</li>
     * <li>Столовая, буфет, зал ресторана</li>
     * <li>Зал театра, кинотеатра, клуба, цирка</li>
     * <li>Гардероб</li>
     * <li>Хранилища библиотек, архивы</li>
     * <li>Музеи, выставки</li>
     * <li>Подсобные и бытовые помещения, лестничная клетка</li>
     * <li>Административные помещения, учебные классы школ, ВУЗов, кабинеты поликлиник</li>
     * <li>Магазины</li>
     * <li>Зал вокзала</li>
     * <li>Стоянки легковых автомобилей</li>
     * <li>Стоянки легковых автомобилей (с двухуровневым хранением)</li>
     * <li>Стадионы</li>
     * <li>Спортзалы</li>
     * <li>Торговый зал гипермаркета</li>
     * </ol>
     */
    private int fireType;

    /**
     * Количество людей в зоне
     */
    private double numOfPeople;

    /**
     * Примечание. Дополнительная информация.
     */
    private String note;

    /**
     * Геометрия зоны.<br>
     * [кольца][точки][x,y,z]
     */
    private double[][][] xyz;

    /**
     * Светофоры и указатели, расположенные в данной зоне
     */
    private Light[] lights;

    /**
     * Сенсоры, расположенные в зоне
     */
    private Sensor[] sensors;

    /**
     * Аудио оповещатели, расположенные в зоне
     */
    private Speaker[] speakers;

    @Override
    public String toString() {
        return id;
    }

    /**
     * Приватный метод, возвращающий полигон на структуре XY
     */
    private Polygon getPolygon() {

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
            for (int k = 1; k < xyz.length; k++) { // Начиная с первого кольца (не с нулевого)
                geomInt[k - 1] = new Coordinate[xyz[k].length];
                for (int l = 0; l < xyz[k].length; l++) {
                    geomInt[k - 1][l] = new Coordinate(xyz[k][l][0], xyz[k][l][1]);
                }
                internalRings[k - 1] = new LinearRing(new CoordinateArraySequence(geomInt[k - 1]), mGF);
            } // for
        } // if

        return new Polygon(new LinearRing(new CoordinateArraySequence(geomOut), mGF), internalRings, mGF);
    }

    /**
     * @return Площадь зоны
     */
    public double getArea() {
        Polygon mP = getPolygon();
        if (mP != null)
            return mP.getArea();
        else return 0;
    }

    /**
     * Метод, возвращающий периметр
     */
    public double getPerimeter() {
        Polygon mP = getPolygon();
        if (mP != null)
            return mP.getLength();
        else return 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getCeilingHeight() {
        return ceilingHeight;
    }

    public void setCeilingHeight(double ceilingHeight) {
        this.ceilingHeight = ceilingHeight;
    }

    public int getFireType() {
        return fireType;
    }

    public void setFireType(int fireType) {
        this.fireType = fireType;
    }

    /**
     * @return Количество людей в зоне
     */
    public double getNumOfPeople() {
        return numOfPeople;
    }

    public void setNumOfPeople(double numOfPeople) {
        this.numOfPeople = numOfPeople;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double[][][] getXyz() {
        return xyz;
    }

    public void setXyz(double[][][] xyz) {
        this.xyz = xyz;
    }

    public double getXyz(int i, int j, int k) {
        return xyz[i][j][k];
    }

    public Light[] getLights() {
        return lights;
    }

    public void setLights(Light[] lights) {
        this.lights = lights;
    }

    public Sensor[] getSensors() {
        return sensors;
    }

    public void setSensors(Sensor[] sensors) {
        this.sensors = sensors;
    }

    public Speaker[] getSpeakers() {
        return speakers;
    }

    public void setSpeakers(Speaker[] speakers) {
        this.speakers = speakers;
    }
}
