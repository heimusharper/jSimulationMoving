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

/**
 * Класс, описывающий помещение.
 * Каждое помещение, в свою очередь, состоит из зон.
 *
 * @author mag
 */
public class Room {

    /**
     * Наименование помещения
     */
    private String name;

    /**
     * Идетнификатор помещения в формате UUID. <br>
     * <hr>
     * UUID представляет собой 16-байтный (128-битный) номер. <br>
     *
     * @Example В шестнадцатеричной системе счисления UUID выглядит как:<br>
     * 550e8400-e29b-41d4-a716-446655440000
     */
    private String id;

    /**
     * Высота потолка помещения, в метрах. Используется для внутренних зон, как значение по-умолчанию
     */
    private double ceilingHeight;

    /**
     * Тип помещения по пожарной нагрузке. Используется для внутренних зон, как значение по-умолчанию
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
     * Количество людей в помещении. Используется для внутренних зон, как пропорциональное по площади значение
     * по-умолчанию.
     */
    private int numOfPeople;

    /**
     * Примечание. Дополнительная информация.
     */
    private String note;

    /**
     * Геометрия всего помещения.<br>
     * [кольца][точки][x,y,z]
     * геометрия целого помещения. (В зонах приводится геометрия отдельных зон)
     */
    private double[][][] xyz;

    /**
     * Список зон в данном помещении
     */
    private Zone[] zones;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getNumOfPeople() {
        return numOfPeople;
    }

    public void setNumOfPeople(int numOfPeople) {
        this.numOfPeople = numOfPeople;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getXyz(int i, int j, int k) {
        return xyz[i][j][k];
    }

    public double getXyz(int i, int j) {
        return xyz[0][i][j];
    }

    public boolean isInternalRing() {
        return xyz.length == 1;
    }

    public double[][][] getXyz() {
        return xyz;
    }

    public void setXyz(double[][][] xyz) {
        this.xyz = xyz;
    }

    public Zone[] getZones() {
        return zones;
    }

    public void setZones(Zone[] zones) {
        this.zones = zones;
    }

    public Zone getZone(int i) {
        return zones[i];
    }
}