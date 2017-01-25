/*
 Copyright (C) 2016 Kolodkin Vladimir, Chirkov Boris

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
*/

package simulation;

import fds.DevcHelper;
import fds.ReadFDSOutput;
import json.extendetGeometry.BIMExt;
import json.extendetGeometry.BIMLoader;
import json.extendetGeometry.SensorExt;
import json.extendetGeometry.ZoneExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static json.extendetGeometry.SensorExt.T_SMOKE;
import static json.extendetGeometry.SensorExt.T_TEMPERATURE;

/**
 * Класс моделирования
 */
public class Moving extends Thread {
    private static final Logger log = LoggerFactory.getLogger(Moving.class);

    @Override public void run() {
        log.info("Running thread with simulation moving");

        // Загрузка структуры здания BIM
        ClassLoader thisClassLoader = Moving.class.getClassLoader();
        BIMLoader<BIMExt> bimLoader = new BIMLoader<>(thisClassLoader.
                getResourceAsStream("Stand-v1.2.json"), BIMExt.class);

        LinkedHashMap<Double, ArrayList<DevcHelper>> fdsData = ReadFDSOutput.readDevc("UdSU_devc.csv");

        BIMExt bim = bimLoader.getBim();
        Traffic traffic = new Traffic(bim);
        // Максимальное кол-во проходов по циклу (Для избежания зацикливания)
        int acceptRepeat = 500;
        log.info("Set max cycle index {}", acceptRepeat);

        double timeModel = 0.0; // Текущее время моделирования эвакуации, c
        double time; // Интервал моделирования эвакуации, c

        double previousFdsTime = 0.0;
        /*for (int i = 0; i < acceptRepeat; i++) {*/
        for (Map.Entry<Double, ArrayList<DevcHelper>> d : fdsData.entrySet()) {
            double fdsTime = d.getKey();

            for (ZoneExt ze : bim.getZones().values()) // по зонам
                for (SensorExt se : ze.getSensors()) // по сенсорам в зоне
                    for (DevcHelper dh : d.getValue()) // по значениям
                        if (se.getId().equalsIgnoreCase(dh.getId())) { // нашли сенсор для которого есть данные
                            if (se.isSmoke() && dh.getType() == T_SMOKE) {
                                se.setVisible(dh.getValue());
                                break;
                            }
                            if (se.isTemperature() && dh.getType() == T_TEMPERATURE) {
                                se.setTemperature(dh.getValue());
                                break;
                            }
                        }

            time = fdsTime - previousFdsTime;
            previousFdsTime = fdsTime;
            timeModel += time;

            int balance = traffic.footTraffic(time);

            log.info("In progress: number of people in Safety zone: {}, simulation time: {}",
                    bim.getSafetyZone().getNumOfPeople(), timeModel);

            if (balance != -1) {
                log.debug("fdsTome: {}", fdsTime);
                //timeModel += balance * fdsTime;
                break;
            }

            /*try { sleep(500L); } catch (InterruptedException e) {e.printStackTrace();}*/
        }

        log.info("Successful finish simulation. Total: number of people in Safety zone: {}, simulation time: {}",
                bim.getSafetyZone().getNumOfPeople(), timeModel);
    }

}
