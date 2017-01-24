/*
 * Copyright (C) 2017 Chirkov Boris <b.v.chirkov@udsu.ru>
 *
 * Project website:       http://eesystem.ru
 * Organization website:  http://rintd.ru
 *
 * --------------------- DO NOT REMOVE THIS NOTICE -----------------------------
 * SensorExt is part of jSimulationMoving.
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
 */

package json.extendetGeometry;

import json.geometry.Sensor;

/**
 * Класс, расширяющий базовый {@link Sensor}
 * Предназначен для полей, которые не входят в *.json файл с геометрией
 * <p>
 * Created by boris on 17.12.16.    Modification of 23.01.2017
 */
public class SensorExt extends Sensor {
    /* STATUSES */
    /**
     * Статус узла - неактивен
     */
    public static final int INACTIVE = 0;
    /**
     * Статус узла - активен
     */
    public static final int ACTIVE   = 1;

    /* TYPES */
    /**
     * Типа узла - неопределен
     */
    public static final int UNKNOWN       = -1;
    /**
     * Типа узла - температурный
     */
    public static final int T_TEMPERATURE = 1;
    /**
     * Типа узла - дымовой
     */
    public static final int T_SMOKE       = 2;

    /* LIMITING VALUES */
    /**
     * Критическое значение параметра - по температуре (70 градусов по Цельсию)
     */
    public static final double V_TEMPERATURE = 70.0;
    /**
     * Критическое значение параметра - по видимости (20 метров)
     */
    public static final double V_VISIBLE     = 20.0;

    /**
     * Состояние сенсорного узла - Активен или Не активен
     */
    private int    status;
    /**
     * Значение по температуре
     */
    private double temperature;
    /**
     * Значение по дальности видимости
     */
    private double visible;

    {
        setStatus(INACTIVE);
    }

    public boolean isTemperature() {
        return super.getType().equalsIgnoreCase("TEMPERATURE");
    }

    public boolean isSmoke() {
        return super.getType().equalsIgnoreCase("SMOKE");
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getVisible() {
        return visible;
    }

    public void setVisible(double visible) {
        this.visible = visible;
    }
}
