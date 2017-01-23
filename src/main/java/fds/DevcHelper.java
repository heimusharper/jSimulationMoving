/*
 * Copyright (C) 2017 Chirkov Boris <b.v.chirkov@udsu.ru>
 *
 * Project website:       http://eesystem.ru
 * Organization website:  http://rintd.ru
 *
 * --------------------- DO NOT REMOVE THIS NOTICE -----------------------------
 * DevcHelper is part of jSimulationMoving.
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

package fds;

import json.extendetGeometry.SensorExt;

/**
 * Структура строки выходного файла FDS
 * <p>
 * Created by boris on 19.01.17.
 */
public class DevcHelper {
    static final int TIME = 0;

    private String id;
    private double value;
    private int    type;
    private double time;

    static int identifyType(String strType) {
        switch (strType) {
        case "SD":
            return SensorExt.SMOKE;
        case "TD":
            return SensorExt.TEMPERATURE;
        case "Time":
            return TIME;
        default:
            return SensorExt.UNKNOWN;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override public String toString() {
        return "DevcHelper{" + "id='" + id + '\'' + ", value=" + value + ", type=" + type + ", time=" + time + '}';
    }
}
