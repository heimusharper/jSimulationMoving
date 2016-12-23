/*******************************************************************************
 * Copyright (C) 2016 Chirkov Boris <b.v.chirkov@udsu.ru>
 *
 * Project website:       http://eesystem.ru
 * Organization website:  http://rintd.ru
 *
 * --------------------- DO NOT REMOVE THIS NOTICE -----------------------------
 * SafetyZone is part of jSimulationMoving.
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

import json.geometry.Zone;

import java.util.List;

/**
 * Безопасная зона. Выделена в отделный объект для удобства и разделения
 * функционала (есть поля, которые не нужны классу {@link ZoneExt}).
 * Не имеет пожарной нагрузки и размеров.
 * <p>
 * Created by boris on 23.12.16.
 */
public class SafetyZone extends ZoneExt {

    /* Количество выходов, которые соединяют безопасную зону и здание (т.е.
    количество эвакуационных выходов) */
    private long numOfExits;

    /* Список дверей, которые содиняют безопаную зону и здание */
    private List<?> transitions;

    // Инициализация параметров
    {
        setId("sz0");
        setCeilingHeight(3.0);
        setNumOfPeople(0);
        setNote("Safety Zone");
        setType(Zone.FLOOR);
    }

    /**
     * @return Количество проемов, которые соединяют безопасную зону и здание
     * (количество эвакуационных выходов)
     */
    public long getNumOfExits() {
        return numOfExits;
    }

    /**
     * Позволяет задать количество проемов, которые соединяют безопасную
     * зону и здание (количество эвакуационных выходов)
     *
     * @param numOfExits - количество проемов
     */
    public void setNumOfExits(long numOfExits) {
        this.numOfExits = numOfExits;
    }

    /**
     * @return Список проемов, которые соединяют безопасную зону и здание
     * (количество эвакуационных выходов)
     */
    public List<?> getTransitions() {
        return transitions;
    }

    /**
     * Позволяет задать список проемов, которые соединяют безопасную
     * зону и здание (количество эвакуационных выходов)
     *
     * @param transitions - список проемов
     */
    public void setTransitions(List<?> transitions) {
        this.transitions = transitions;
    }

}
