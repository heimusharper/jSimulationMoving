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

import json.extendetGeometry.BIMExt;
import json.extendetGeometry.BIMLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Класс моделирования
 */
public class Moving extends Thread {
    private static final Logger log = LoggerFactory.getLogger(Moving.class);

    @Override public void run() {
        // Загрузка структуры здания BIM
        ClassLoader thisClassLoader = Moving.class.getClassLoader();
        BIMLoader<BIMExt> bimLoader = new BIMLoader<>(thisClassLoader.
                getResourceAsStream("Stand-v1.2.json"), BIMExt.class);

        log.info("Running thread with simulation moving");

        BIMExt bim = bimLoader.getBim();
        Traffic traffic = new Traffic(bim);
        // Максимальное кол-во проходов по циклу (Для избежания зацикливания)
        int acceptRepeat = 500;
        log.info("Set max cycle index {}", acceptRepeat);

        double timeModel = 0.0; // Текущее время моделирования эвакуации, c
        double time = 6; // Интервал моделирования эвакуации, c

        for (int i = 0; i < acceptRepeat; i++) {
            int balance = traffic.footTraffic(time);
            timeModel += time;

            log.info("In progress: number of people in Safety zone: {}, simulation time: {}",
                    bim.getSafetyZone().getNumOfPeople(), timeModel);

            if (balance != -1) {
                timeModel += balance * time;
                break;
            }

            try { sleep(500L); } catch (InterruptedException e) {e.printStackTrace();}
        }

        log.info("Successful finish simulation. Total: number of people in Safety zone: {}, simulation time: {}",
                bim.getSafetyZone().getNumOfPeople(), timeModel);
    }

}
