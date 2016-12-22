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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Main;
import json.extendetGeometry.BIMExt;
import json.extendetGeometry.RoomExt;
import json.extendetGeometry.TransitionExt;
import json.extendetGeometry.ZoneExt;

public class Moving {
    private static final Logger log = LoggerFactory.getLogger(Moving.class);
    private BIMExt              bim;

    public Moving(BIMExt bim) {
        this.bim = bim;
    }

    public void run() {
        ArrayList<ZoneExt> zones = new ArrayList<>();
        bim.getRooms().stream().forEach(
                r -> r.getZones().stream().forEach(zones::add));
        HashMap<String, ZoneExt> mapZones = new HashMap<>();

        zones.stream().forEach(z->mapZones.put(z.getId(), z));

        bim.getTransitions().stream().forEach(t -> {
            if (t.getZoneAId() != null) mapZones.get(t.getZoneAId()).addTransition(t);
            if (t.getZoneBId() != null) mapZones.get(t.getZoneBId()).addTransition(t);
        });

//        for (ZoneExt z : zones) {
//            System.out.println(z.getTransitions().size());
//        }

        // log.info("Начинаем распечатку переходов ");
        // for (TransitionExt r : bim.getTransitions() ){
        // System.out.println(r);
        // }

        final int acceptRepeat = 500; // Максимальное количество проходов по
                                      // циклу. Для избежания зацикливания.
        final long timeInterval = 10000; // Интервал моделирования, миллисек
                                         // (minimum 150ms)
        final float tay = getTay(); // Шаг моделирования
        // Высчитываем интервал моделирования в зависимости от timeInterval
        final int time = new BigDecimal(((timeInterval / 1000f) / 60f) / tay)
                .setScale(0, RoundingMode.UP).intValue();

        double timeModel = 0.0; // Время эвакуации, сек

        //Traffic traffic = new Traffic(zones, bim.getTransitions(), tay, time);

        for (int i = 0; i < acceptRepeat; i++) {
            // traffic.footTraffic();

            timeModel += tay * time * 60;
        }

    }

    /**
     * @return tay - шаг моделирования
     */
    private float getTay() {
        final float hxy = 0.5f; // характерный размер области, м
        final float ktay = 0.5f; // коэффициент (< 1) уменьшения шага по времени
                                 // для устойчивости расчетов
        final float vmax = 100f; // максимальная скорость эвакуации, м/мин
        final float tay = (hxy / vmax) * ktay; // мин - Шаг моделирования 100 -
                                               // максимальная скорость м/мин
        return tay;
    }
}
