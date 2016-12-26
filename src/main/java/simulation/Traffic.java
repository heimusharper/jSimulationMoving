/******************************************************************************
 Copyright (C) 2016 Kolodkin Vladimir <kolodkin@rintd.ru>

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

import json.extendetGeometry.*;
import json.geometry.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class Traffic {
    private final static Logger log = LoggerFactory.getLogger(Traffic.class);

    /**
     * Площадь горизонтальной проекции человека, м^2 (Коэфф.,
     * определяющий максимальную плотность в ячейке)
     */
    private final double FP             = 0.113;
    /**
     * Коэффициент проходимости зоны. Если значение проходимости зоны ниже этого
     * значения, зона не проходима
     */
    private final double CR_PASSABILITY = 0.1;
    /**
     * Максимальная скорость движения людского потока, м/мин
     */
    private final double                   V_MAX;
    private final float                    TAY;
    private final float                    TIME;
    /**
     * Количество эвакуационных выходов
     */
    private final int                      NUM_OF_EXITS;
    /**
     * Максимальная плотность людского потока
     */
    private final double                   D_MAX;
    /**
     * Безопасная зона
     */
    private       SafetyZone               safetyZone;
    /**
     * Индексированный списко зон, где инддекс - uuid зона
     */
    private       HashMap<String, ZoneExt> zones;
    /**
     * Список проемов в здании
     */
    private       List<TransitionExt>      transitions;
    /**
     * Список эвакуационных выходов
     */
    private       List<TransitionExt>      exits;
    /**
     * Количество людей в здании. Если поставить число <=0, то распределение
     * людей берется из ПИМ здания.
     */
    private       double                   numOfPeople;

    public Traffic(BIMExt bim, float tay, float time) {
        this.safetyZone = bim.getSafetyZone();
        this.zones = bim.getZones();
        this.transitions = bim.getTransitions();
        this.exits = bim.getExitsTransition();
        this.numOfPeople = bim.getNumOfPeople();
        this.TAY = tay;
        this.TIME = time;
        this.NUM_OF_EXITS = bim.getNumOfExits();
        this.D_MAX = 5.0;
        this.V_MAX = 100.0;
    }

    /**
     * @param l     ширина проема, м
     * @param dElem плотность в элементе
     *
     * @return Скорость потока в проеме в зависимости от плотности, м/мин
     */
    private static double vElem(final double l, final double dElem) {
        double v0 = 100; // м/мин
        double d0 = 0.65;
        double a = 0.295;

        double v0k;
        if (dElem >= 9) v0k = 10 * (2.5 + 3.75 * l) / d0;    //   07.12.2016
        else if (dElem > d0) {
            double m = dElem >= 5 ? 1.25 - 0.05 * dElem : 1;
            v0k = v0 * (1.0 - a * Math.log(dElem / d0)) * m;
        } else v0k = v0;

        return v0k;
    }

    /**
     * @param dElem плотность в элементе
     *
     * @return Скорость потока по горизонтальному пути, м/мин
     */
    private static double vElem(final double dElem) {
        double v0 = 100; // м/мин
        double d0 = 0.51;
        double a = 0.295;

        return dElem > d0 ? v0 * (1.0 - a * Math.log(dElem / d0)) : v0;
    }

    /**
     * Моделирование Людских потоков по зданию 18/08/2015, 22.12.2016
     */
    public void footTraffic() {
        // Оценка точности представления координат
        final double dxyz = 0.1 * TAY * V_MAX;
        // Списки на выход (в количестве numExit);
        final ArrayList<ArrayList<ZoneExt>> zoneOut = new ArrayList<>(
                NUM_OF_EXITS);
        for (int i = 0; i < NUM_OF_EXITS; i++)
            zoneOut.add(new ArrayList<>());

        // номер обрабатываемого элемента списка выхода ii
        final int[] k_out = new int[NUM_OF_EXITS];
        // номер последнего элемента списка выхода ii
        final int[] finish_out = new int[NUM_OF_EXITS];
        // номера очередей ii с обрабатываемыми элементами, отсортированными
        // по возрастанию плотности
        final int[] iiWork = new int[NUM_OF_EXITS];
        // плотность (раб. массив для сортировки по плотности)
        final double[] dZone0 = new double[NUM_OF_EXITS];
        // признак выхода из главного цикла по отсутствию людей (do)
        final boolean[] outstep = new boolean[NUM_OF_EXITS];

        final Supplier<IntStream> loop = () -> IntStream.range(0, NUM_OF_EXITS);

        // Формирование и обработка (одновременная) списков к каждому выходу
        // Цикл по времени процесса
        for (int kkktay = 1; kkktay <= TIME; kkktay++) {
            boolean xyz = false;
            loop.get().forEach(i -> outstep[i] = true);

            // Обработка первого элемента
            SafetyZone safetyZone = this.safetyZone; // Зона безопасности
            loop.get().parallel().forEach(i -> { // 01 Loop of output
                // i - номер эвакуационного выхода
                zoneOut.get(i).clear();
                // Идентификатор портала на улицу
                TransitionExt exit = exits.get(i);
                // Ширина проема на улицу
                double widthTransition = exit.getWidth();

                // Определяем зону, которая находится в здании и зону, которая
                // граничит с эвакуационным выходом
                String uuidZone = exit.getZoneAId() == null ?
                        exit.getZoneBId() :
                        exit.getZoneAId();

                // ---- Входим в здание ---
                // Обработка первой (проходимой) зоны (рядом с улицей)
                ZoneExt _zone = zones.get(uuidZone);
                int zoneType = _zone.getType();
                double dPeopleZone = _zone.getNumOfPeople();
                double sZone = _zone.getArea();
                double vZone = Double.NaN; // Скорость движения в зоне

                // Определяем плотность людей в зоне, чел/м2
                double dZone = dPeopleZone / sZone;

                /* Определяем как выходим из здания - по лестнице или по
                прямой */
                switch (zoneType) {
                case Zone.FLOOR:
                    vZone = vElem(dZone);
                    break;
                case Zone.STAIRS:
                    double hElem = _zone.getMinZ();
                    /* У безопасной зоны нет геометрических параметров, но
                    есть уровень, на котором она находится относительно
                    каждого из выходов */
                    double hElem0 = safetyZone.getMinZ(i);
                    double dh = hElem - hElem0; // Разница высот зон
                    if (Math.abs(dh) >= dxyz) {
                        // Определяем направление движения по лестнице
                        int direction = (hElem > hElem0) ?
                                Direction.DOWN :
                                Direction.UP;
                        _zone.setDirection(direction);
                        vZone = vElemZ(direction, dZone);
                    } else vZone = vElem(dZone);
                    break;
                default:
                    log.error("Неопределенный тип зоны");
                    break;
                }

                // Скорость движения в дверях на выходе из здания
                double vTransition = vElem(widthTransition, dZone);
                // Скорость на выходе из здания
                double vAtExit = Math.min(vZone, vTransition);
                // ! Не знаю что это ---->
                double d1 = widthTransition * vAtExit * TAY / sZone; // <-----
                double d2 = (d1 > 1) ? 1 : d1;

                // Изменение численности людей в помещении рядом с выходом
                double dPeople = d2 * dPeopleZone;
                double delta = _zone.getNumOfPeople() - dPeople;
                double ddPeople = (delta > 0) ? dPeople : dPeopleZone;

                // Увеличение людей в безопосной зоне
                safetyZone.addPeople(ddPeople);
                // Уменьшение людей в зоне здания
                _zone.removePeople(ddPeople);
                // Увеличение счетчика людей, прошедших через дверь ii
                exit.addPassingPeople(ddPeople);
                // Признак обработки двери. Увеличение TransitionExt#nTay на
                // единицу
                exit.nTayIncrease();
                // Выход через дверь ii на улицу. Присвоили выходу номер
                exit.setNumberExit(i);
                // Признак обработки элемента здания
                // Метка, которая говорит, что это помещение уже обработано
                // i-той дверью
                _zone.setNTay(exit.getNTay());
                // Помещение освобождается через выход ii
                _zone.setNumberExit(i);
                // Потенциал времени в первом помещении. Время достижения
                // эвакуационного выхода из зоны
                _zone.setTimeToReachExit(
                        (vAtExit > 0) ? Math.sqrt(sZone) / vAtExit : 0.0);

                // Добавляем ближайшую к выходу зону в соответствующий список
                // "на выход"
                zoneOut.get(i).add(_zone); // Increase List
                k_out[i] = 0;
                finish_out[i] = 0;
                // finis of passable processing zones
            }); // 01 Cycle of output // Finis of the cycle of output

        }
    }

    /**
     * @param direct направление движения (direct = 3 - вверх ({@link
     *               Direction#UP}), = -3 - вниз ({@link Direction#DOWN})
     * @param dElem  плотность в элементе
     *
     * @return Скорость потока при движении по лестнице в зависимости от
     * плотности, м/мин
     */
    private double vElemZ(final int direct, final double dElem) {
        double d0 = 0, v0 = 0, a = 0;

        switch (direct) {
        case Direction.UP:
            d0 = 0.67;
            v0 = 50;
            a = 0.305;
            break;
        case Direction.DOWN:
            d0 = 0.89;
            v0 = 80;
            a = 0.4;
            break;
        default:
            log.error("Fail! Direction unknown - '{}'", direct);
            break;
        }

        return dElem > d0 ? v0 * (1.0 - a * Math.log(dElem / d0)) : v0;
    }

}
