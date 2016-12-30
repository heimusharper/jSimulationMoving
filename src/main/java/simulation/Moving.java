/******************************************************************************
 Copyright (C) 2016 Kolodkin Vladimir

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
 ******************************************************************************/

package simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import json.extendetGeometry.BIMExt;

/**
 * Класс моделирования
 */
public class Moving implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Moving.class);
    private BIMExt              bim;

    public Moving(BIMExt bim) {
        this.bim = bim;
    }

    @Override
    public void run() {
        Traffic traffic = new Traffic(bim);
        // Максимальное кол-во проходов по циклу (Для избежания зацикливания)
        log.info("Running thread for simulation moving");
        // Максимальное кол-во проходов по циклу (Для избежания зацикливания)
        int acceptRepeat = 500;

        double timeModel = 0.0; // Текущее время моделирования эвакуации, c
        double time = 10; // Интервал моделирования эвакуации, c

        for (int i = 0; i < acceptRepeat; i++) {
            traffic.footTraffic(time);
            timeModel += time;
        }

        log.info("Finish simulation moving");
    }

}
