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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Класс моделирования
 */
public class Moving implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Moving.class);
    private BIMExt bim;

    public Moving(BIMExt bim) {
        this.bim = bim;
    }

    @Override
    public void run() {
        log.info("Running thread with simulation moving");
        // Максимальное кол-во проходов по циклу (Для избежания зацикливания)
        int acceptRepeat = 500;
        // Интервал моделирования, миллисек (minimum 150ms)
        // TODO Требует корректировки
        long timeInterval = 10000;
        float tay = getTay(); // Шаг моделирования
        // Высчитываем интервал моделирования в зависимости от timeInterval
        int time = new BigDecimal(((timeInterval / 1000f) / 60f) / tay)
                .setScale(0, RoundingMode.UP).intValue();

        double timeModel = 0.0; // Время эвакуации, мин

        Traffic traffic = new Traffic(bim, tay, time);

        // Главный цикл моделирования
        for (int i = 0; i < acceptRepeat; i++) {
            traffic.footTraffic();

            timeModel += tay * time * 60;
        }

        log.info("Finish simulation moving");
    }

    /**
     * @return tay - шаг моделирования
     */
    private float getTay() {
        float hxy = 0.5f; // характерный размер области, м
        float ktay = 0.5f; // коэффициент (< 1) уменьшения шага по времени
        // для устойчивости расчетов
        float vmax = 100f; // максимальная скорость эвакуации, м/мин

        // Шаг моделирования, мин
        return (hxy / vmax) * ktay;
    }
}
