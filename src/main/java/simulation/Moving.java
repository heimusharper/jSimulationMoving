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
import tools.Analysis;
import tools.Plotting;

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

    private String  fileName;
    private double  density;
    private boolean isFire;
    private Plotting plot;

    public Moving(String fileName, double density, boolean isFire, Plotting plot) {
        this.fileName = fileName;
        this.density = density;
        this.isFire = isFire;
        this.plot = plot;
    }

    public Moving() {}

    @Override public void run() {
        log.info("Running thread with simulation moving");

        // Загрузка структуры здания BIM
        ClassLoader thisClassLoader = Moving.class.getClassLoader();
        BIMLoader<BIMExt> bimLoader = new BIMLoader<>(thisClassLoader.
                getResourceAsStream("segment-6k-v2.3.json"), BIMExt.class);

        LinkedHashMap<Double, ArrayList<DevcHelper>> fdsData = ReadFDSOutput.readDevc("scenarios/" + fileName);

        BIMExt bim = bimLoader.getBim();

        // START ------------- ANALYSIS VARS -------------------
        {
            // Распределение людей по помещениям. Распределяются только в те, где уже были люди
            bim.getZones().values().forEach(z -> {
                if (z.getNumOfPeople() > 0) z.setAllocationPeople(density);
            });
            // Распечатка количества людей по этажам
            bim.getNumOfPeopleOnLayers().forEach((l, p) -> log.debug("On level {} is {} people ", l, p));
        }
        Analysis analysis = new Analysis(bim, fileName, isFire, density, plot.getWorkDir().getAbsolutePath());
        ArrayList<String> blockedZones = new ArrayList<>();
        ArrayList<String> dynamicsOfGeneralEvac = new ArrayList<>(); // Динамика вышедших на улицу
        dynamicsOfGeneralEvac.add(String.valueOf(0.0) + ";" + String.valueOf(bim.getSafetyZone().getNumOfPeople()));
        // END ------------- ANALYSIS VARS -------------------

        Traffic traffic = new Traffic(bim);

        // Количество людей в здании, до эвакуации
        double nop = bim.getNumOfPeople();
        log.info("Number of people in Building: {}", nop);
        // Максимальное кол-во проходов по циклу (Для избежания зацикливания)
        // int acceptRepeat = 500;
        double timeModel = 0.0; // Текущее время моделирования эвакуации, c
        double time; // Интервал моделирования эвакуации, c
        double previousFdsTime = 0.0;

        /*for (int i = 0; i < acceptRepeat; i++) {*/
        for (Map.Entry<Double, ArrayList<DevcHelper>> d : fdsData.entrySet()) {
            double fdsTime = d.getKey();
            if (fdsTime == 0.0) continue;

            if (isFire) for (ZoneExt ze : bim.getZones().values()) {// по зонам
                for (SensorExt se : ze.getSensors()) // по сенсорам в зоне
                    for (DevcHelper dh : d.getValue()) // по значениям
                        if (se.getId().substring(3).equalsIgnoreCase(dh.getId())) { // сенсор для которого есть данные
                            if (se.isSmoke() && dh.getType() == T_SMOKE) {
                                se.setVisible(dh.getValue());
                                break;
                            }
                            if (se.isTemperature() && dh.getType() == T_TEMPERATURE) {
                                se.setTemperature(dh.getValue());
                                break;
                            }
                        }
                if (ze.isBlocked() && !blockedZones.contains(ze.getId())) {
                    blockedZones.add(ze.getId());
                    plot.addTimeBlock(timeModel);
                    log.debug("Zone {} blocked at time {}", ze.getId(), timeModel);
                }
            }

            time = fdsTime - previousFdsTime;
            previousFdsTime = fdsTime;
            timeModel += time;

            int balance = traffic.footTraffic(time);

            // START ------------- ANALYSIS -------------------
            // Подсчет людей проходящих через заданные проемы. Проемы задаются в классе Analysis
            analysis.counterPeopleThroughDoor(timeModel);
            // Фиксация динамики выхода люде из здания
            dynamicsOfGeneralEvac.add(timeModel + ";" + bim.getSafetyZone().getNumOfPeople());
            // END ------------- ANALYSIS -------------------

            /*log.info("In progress: number of people in Safety zone: {}, simulation time: {}",
                    bim.getSafetyZone().getNumOfPeople(), timeModel);*/

            if (balance != -1) {
                log.debug("fdsTime: {}", fdsTime);
                break;
            }

            /*try { sleep(500L); } catch (InterruptedException e) {e.printStackTrace();}*/
        }

        log.info("Successful finish simulation. Total: number of people in Safety zone: {} of {}, simulation time: {}",
                bim.getSafetyZone().getNumOfPeople(), nop, timeModel);
        // Запись результатов
        // Динамика выхода людей из здания
        /*analysis.saveResult(dynamicsOfGeneralEvac, nop, "g");/**/ // G - general. Динамика выхода из здания
        // Динамика движения через обозначенные проемы
        analysis.getTimeList().forEach((k, v) -> {
            if (analysis.getUuidMap().containsKey(k)) analysis.saveResult(v, nop, "t" + analysis.getUuidMap().get(k));
        });/**/

        plot.setEndModelling();
    }
}
