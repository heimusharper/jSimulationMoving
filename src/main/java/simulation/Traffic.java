/*
 Copyright (C) 2016 Kolodkin Vladimir <kolodkin@rintd.ru>   Version of 27.12.2016

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

import json.extendetGeometry.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class Traffic {
    private final static Logger log             = LoggerFactory.getLogger(Traffic.class);
    /**
     * Коэффициент проходимости зоны. Если значение проходимости зоны ниже этого
     * значения, зона не проходима
     */
    private final static double CR_PERMEABILITY = 0.1;
    /**
     * Площадь горизонтальной проекции человека, м^2 (Коэфф.,
     * определяющий максимальную плотность в ячейке)= 0.1 - 0.125 м^2/чел.
     */
    private final        double FP              = 0.113;
    /**
     * Максимальная скорость движения людского потока, м/мин
     */
    private final double                   V_MAX;
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
     * Список эвакуационных выходов
     */
    private       List<TransitionExt>      exits;
    /**
     * Количество людей в здании. Если поставить число <=0, то распределение
     * людей берется из ПИМ здания.
     */
    private       double                   numOfPeople;

    Traffic(BIMExt bim) {
        this.safetyZone = bim.getSafetyZone();
        this.zones = bim.getZones();
        this.exits = bim.getExitsTransition();
        this.numOfPeople = bim.getNumOfPeople();
        this.NUM_OF_EXITS = bim.getNumOfExits();
        this.D_MAX = 5.0;
        this.V_MAX = 100.0; // м/мин
    }

    /**
     * @param l     ширина проема, м
     * @param dElem плотность в элементе
     * @return Скорость потока в проеме в зависимости от плотности, м/мин
     */
    private static double vElem(final double l, final double dElem) {
        double v0 = 100; // м/мин
        double d0 = 0.65;
        double a = 0.295;
        double v0k;
        if (dElem >= 9) v0k = 10 * (2.5 + 3.75 * l) / d0; // 07.12.2016
        else if (dElem > d0) {
            double m = dElem >= 5 ? 1.25 - 0.05 * dElem : 1;
            v0k = v0 * (1.0 - a * Math.log(dElem / d0)) * m;
        } else v0k = v0;
        return v0k;
    }

    /**
     * @param dElem плотность в элементе
     * @return Скорость потока по горизонтальному пути, м/мин
     */
    private static double vElem(final double dElem) {
        double v0 = 100; // м/мин
        double d0 = 0.51;
        double a = 0.295;
        return dElem > d0 ? v0 * (1.0 - a * Math.log(dElem / d0)) : v0;
    }

    /**
     * Метод сортировки плотностей перед выходами
     *
     * @param dZone0 массив плотностей в зонах
     * @param iiWork масив очередей
     * @param b      параметр, который отвечает за обратную сортировку. Если
     *               true, то включается обратная сортировка, если параметр
     *               отсутствует, то сортировка по возрастанию
     */
    private void sortingDensity(final double[] dZone0, final int[] iiWork, boolean... b) {
        final float loadFactor = 1.247330950103979f;
        int step = dZone0.length;
        boolean isEnd = false;
        double z;
        int w;

        while (!isEnd) {
            isEnd = true;
            step /= loadFactor;
            if (step < 1) step = 1;

            for (int i = 0; i < dZone0.length - 1; i++) {
                int j;
                if ((j = i + step) < dZone0.length) {
                    boolean direction = b.length > 0 && b[0] ? dZone0[i] < dZone0[j] : dZone0[i] > dZone0[j];
                    if (direction) {
                        z = dZone0[i];
                        dZone0[i] = dZone0[j];
                        dZone0[j] = z;

                        w = iiWork[i];
                        iiWork[i] = iiWork[j];
                        iiWork[j] = w;

                        isEnd = false;
                    }
                } else isEnd = false;
            }
        }
    }

    /**
     * Моделирование Людских потоков по зданию 18.08.2015, 29.12.2016, 9.01.2017
     *
     * @param time временной интервал моделирования эвакуации в секундах
     */
    int footTraffic(double time) {
        // Списки на выход (в количестве numExit);
        final ArrayList<ArrayList<ZoneExt>> zoneOut = new ArrayList<>(NUM_OF_EXITS);
        for (int i = 0; i < NUM_OF_EXITS; i++)
            zoneOut.add(new ArrayList<>());

        // номер обрабатываемого элемента списка выхода ii
        final int[] k_out = new int[NUM_OF_EXITS];
        // номер последнего элемента списка выхода ii
        final int[] finish_out = new int[NUM_OF_EXITS];
        // номера очередей ii с обрабатываемыми элементами, отсортированными
        // по возрастанию плотности
        final int[] workQueue = new int[NUM_OF_EXITS];
        // плотность (раб. массив для сортировки по плотности)
        final double[] tmpArrForSort = new double[NUM_OF_EXITS];
        // признак выхода из главного цикла по отсутствию людей (do)
        final boolean[] outstep = new boolean[NUM_OF_EXITS];
        // Оценка точности представления координат, метры
        final int numberEvacCycle = getNumberEvacCycle(time, getTay());

        // Формирование и обработка (одновременная) списков к каждому выходу
        // Цикл по времени процесса
        for (int kkktay = 1; kkktay <= numberEvacCycle; kkktay++) { // kkktay=1
            for (int ii = 0; ii < NUM_OF_EXITS; ii++) outstep[ii] = true;

            /* Обход ближайших к эвакуационным выходам зон и перемещения из них людей в безопасную зону.
             * Создание очередей работы со зданием. */
            for (int ii = 0; ii < NUM_OF_EXITS; ii++) {
                zoneOut.get(ii).clear();
                // Обработка первой зоны (рядом с улицей)
                ZoneExt _zone = processingFirstZone(ii);
                // Добавляем ближайшую к выходу зону в соответствующий список "на выход"
                zoneOut.get(ii).add(_zone);
                k_out[ii] = finish_out[ii] = 0;
            }

            // Формирование последующих элементов в каждом списке к выходу
            boolean xyz;
            do { // 1 Проход по ячейкам и всасывание людей из ячеек, соседних с
                // наполняемой ячейкой на шаге tay
                for (int j = 0; j < NUM_OF_EXITS; j++) {
                    tmpArrForSort[j] = -1;
                    workQueue[j] = NUM_OF_EXITS - 1;
                    if (outstep[j]) {
                        ZoneExt z = zoneOut.get(j).get(k_out[j]);
                        tmpArrForSort[j] = z.getTimeToReachExit();
                        workQueue[j] = j;
                    }
                }
                // Сортировка очередей по времени достижения безопасной зоны
                sortingDensity(tmpArrForSort, workQueue, true); // true - сортировка по убыванию
                // Обход зон, следующих за ближайшей к эвакуационному выходу
                for (int k, iiTurn = 0; iiTurn < NUM_OF_EXITS; iiTurn++) { // == 001
                    int ii = workQueue[iiTurn];
                    boolean evacOutLimit = false;  //ограничитель эвакуации к выходу

                    checkWorkQueue(workQueue, tmpArrForSort, outstep[ii], iiTurn, ii);
                    ZoneExt receivingArea = zoneOut.get(ii).get(k_out[ii]); // Принимающая зона
                    do {                            // constDirec = true;       ==02
                        if (outstep[ii]) {          // outstep[ii] == true      ==2
                            // Обработка зоны, если она проходима
                            if (receivingArea.getPermeability() >= CR_PERMEABILITY) {  //  == 3
                                // Обходим все двери зоны
                                for (TransitionExt transit : receivingArea.getTransitionList()) {
                                    // Портал не подлежит обработке
                                    if (receivingArea.getNTay() <= transit.getNTay() || transit.hasNullZone()) continue;

                                    String idZone1 = transit.getZoneAId();// Ссылки от портала на зону A
                                    ZoneExt radiatingArea = zones.get((receivingArea.getId().equals(idZone1)) ?
                                            transit.getZoneBId() :
                                            idZone1);
                                    double numOfPeopleInZone = radiatingArea.getNumOfPeople();
                                    double sZone = radiatingArea.getArea();
                                    double vZone = vElem(radiatingArea, receivingArea, ii);
                                    double dZone = radiatingArea.getDensityOfPeople();
                                    double lTransition = transit.getWidth();
                                    double vTransition = vElem(lTransition, dZone);
                                    /*double vAtExit = receivingArea.getPermeability() * Math.min(vZone, vTransition);*/
                                    double vAtExit = Math.min(vZone, vTransition);
                                    double d1 = lTransition * vAtExit * getTay() / sZone;
                                    double d2 = (d1 >= 1) ? 1 : d1;
                                    double dPeople = d2 * numOfPeopleInZone; // Изменение кол.людей
                                    double delta = numOfPeopleInZone - dPeople; // Отдающая
                                    // Кол. людей, которые могут быть высосаны
                                    double ddPeople = (delta > 0) ? dPeople : numOfPeopleInZone;
                                    double capacityZone =
                                            D_MAX * receivingArea.getArea() - receivingArea.getNumOfPeople();
                                    double changePeople = (capacityZone > ddPeople) ? ddPeople : capacityZone;

                                    receivingArea.addPeople(changePeople);// Увелич. людей в принимающей
                                    radiatingArea.removePeople(changePeople);// Уменьшение людей
                                    transit.addPassingPeople(changePeople);  // Увел. людей через дверь
                                    // Потенциал времени в первом помещении
                                    double timeout = (vAtExit > 0) ?
                                            receivingArea.getTimeToReachExit() + Math.sqrt(sZone) / vAtExit :
                                            receivingArea.getTimeToReachExit();
                                    radiatingArea.setTimeToReachExit(timeout);
                                    transit.nTayIncrease(); // Признак  обработки двери
                                    transit.setNumberExit(ii);
                                    radiatingArea.setNTay(transit.getNTay()); // Признак обраб. зоны
                                    radiatingArea.setNumberExit(transit.getNumberExit());

                                    zoneOut.get(ii).add(radiatingArea);
                                    finish_out[ii]++;
                                } //  == 5 Обработка списка порталов
                            } // ==3
                            if (finish_out[ii] > k_out[ii]) {
                                k_out[ii]++;
                                evacOutLimit = true;
                            } else outstep[ii] = evacOutLimit = false;
                        } // outstep[ii] == true ==2

                        k = iiTurn + 1;
                    } while (evacOutLimit && k < NUM_OF_EXITS
                            && receivingArea.getTimeToReachExit() <= tmpArrForSort[k]); // 02 - finish
                } // Обработка очереди ii   // == 001
                xyz = false;
                for (int i = 0; i < NUM_OF_EXITS; i++)
                    if (outstep[i]) xyz = true;
            } while (xyz); // do 1

            // Выход из цикла моделирования
            if (isEnded()) return kkktay;
        }// kkktay=1
        return -1;
    }

    /**
     * @return true, если в здании нет людей
     */
    private boolean isEnded() {
        if (numOfPeople - safetyZone.getNumOfPeople() < 0.5) {
            zones.values().forEach(z -> z.setNumOfPeople(0.0)); // Чистка помещений
            safetyZone.setNumOfPeople(numOfPeople);
            return true;
        }
        return false;
    }

    /**
     * Проверка рабочей очереди на дефекты
     *
     * @param workQueue     массив очередей
     * @param tmpArrForSort вспомогательный массив времен достижения
     * @param b             значение outstep[ii]
     * @param iiTurn        номер двери, с которой ведется работа
     * @param ii            номер очереди
     */
    private void checkWorkQueue(int[] workQueue, double[] tmpArrForSort, boolean b, int iiTurn, int ii) {
        if (ii >= 0) return;
        log.error("Mistake Traffic ii= {}, outstep[ii]= {}, iiTurn= " + "{}, numExit= {}", ii, b, iiTurn, NUM_OF_EXITS);
        for (int i = 0; i < NUM_OF_EXITS; i++)
            log.error("workQueue[{}]={}, tmpArrForSort[ii]={}", i, workQueue[i], tmpArrForSort[i]);
    }

    private ZoneExt processingFirstZone(int ii) {
        // Идентификатор портала на улицу
        TransitionExt exit = exits.get(ii);

        // Определяем зону, которая находится в здании и зону, которая
        // граничит с эвакуационным выходом
        String uuidZone = null;
        if (exit.getZoneAId() == null) uuidZone = exit.getZoneBId();
        if (exit.getZoneBId() == null) uuidZone = exit.getZoneAId();
        // Ширина проема на улицу
        double widthTransition = exit.getWidth();
        ZoneExt _zone = zones.get(uuidZone);
        double dPeopleZone = _zone.getNumOfPeople();
        double sZone = _zone.getArea();
        double vZone = vElem(_zone, safetyZone, ii); // Скорость движения в зоне
        // Определяем плотность людей в зоне, рядом с выходом чел/м2
        double dZone = _zone.getDensityOfPeople();
        // Скорость движения в дверях на выходе из здания
        double vTransition = vElem(widthTransition, dZone);
        // Скорость на выходе из здания
        double vAtExit = Math.min(vZone, vTransition);
        // Доля вышедших  людей
        double d1 = widthTransition * vAtExit * getTay() / sZone;
        double d2 = (d1 > 1) ? 1 : d1;
        // Изменение численности людей в помещении рядом с выходом
        double dPeople = d2 * dPeopleZone;
        double delta = dPeopleZone - dPeople;
        double ddPeople = (delta > 0) ? dPeople : dPeopleZone;

        safetyZone.addPeople(ddPeople);// Увеличение количества людей в безопасной зоне
        _zone.removePeople(ddPeople);
        exit.addPassingPeople(ddPeople); // Увеличение счетчика людей, прошедших через дверь ii
        exit.nTayIncrease(); // Признак обработки двери. Увеличение TransitionExt#nTay на единицу
        exit.setNumberExit(ii); // Выход через дверь ii на улицу. Присвоили выходу номер
        _zone.setNTay(exit.getNTay()); // Метка, которая говорит, что это помещение уже обработано ii-той дверью
        _zone.setNumberExit(ii);// Помещение освобождается через выход ii
        // Потенциал времени в первом помещении. Время достижения эвакуационного выхода из зоны
        _zone.setTimeToReachExit((vAtExit > 0) ? Math.sqrt(sZone) / vAtExit : 0.0);

        return _zone;
    }

    /**
     * @return шаг моделирования процесса эвакуации,мин
     */
    private double getTay() {
        double hxy = 0.5; // характерный размер области, м
        double ktay = 0.5; // коэффициент (< 1) уменьшения шага по времени для устойчивости расчетов
        return (hxy / V_MAX) * ktay; // Шаг моделирования, мин
    }

    /**
     * @param time - временной интервал моделирования, секунды
     * @param tay  - временной шаг моделирования, минуты
     * @return количество проходов по циклу моделирования эвакуации
     */
    private int getNumberEvacCycle(double time, double tay) {
        if (time <= (tay * 60)) return 1;
        else return (int) (time / (tay * 60));
    }

    /**
     * @param direct направление движения (direct = 3 - вверх ({@link
     *               Direction#UP}), = -3 - вниз ({@link Direction#DOWN})
     * @param dElem  плотность в элементе
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

    /**
     * @param outZone зона, из которой высасываются люди
     * @param toZone  зона, в которую засасываются люди
     * @param numExit номер эвакуационного выхода от которого сейчас идет
     *                движение
     * @return Скорость людского потока в зоне
     */
    private double vElem(ZoneExt outZone, ZoneExt toZone, int numExit) {
        double vZone = Double.NaN;

        // Определяем как выходим из зоны в здании - по лестнице или по прямой
        switch (outZone.getType()) {
        case ZoneExt.FLOOR:
            vZone = vElem(outZone.getDensityOfPeople());
            break;
        case ZoneExt.STAIRS:
            // Оценка точности представления координат, метры
            double dxyz = 0.1 * getTay() * V_MAX;
            /* У безопасной зоны нет геометрических параметров, но
            есть уровень, на котором она находится относительно
            каждого из эвакуационных выходов */
            // Проверяем, является ли toZone экземпляром Безопасной зоны
            // если является, то используем другой метод определения высоты,
            // на которой расположена зона
            double dh = outZone.getMinZ() - ((toZone instanceof SafetyZone) ?
                    ((SafetyZone) toZone).getMinZ(numExit) :
                    toZone.getMinZ()); // Разница высот зон
            // Если перепад высот незначительный, то скорость движения
            // принимается как при типе зоны FLOOR
            if (Math.abs(dh) < dxyz) {
                vZone = vElem(outZone.getDensityOfPeople());
            } else {
                // Иначе определяем направление движения по лестнице
                int direction = (dh > 0) ? Direction.DOWN : Direction.UP;
                outZone.setDirection(direction);
                vZone = vElemZ(direction, outZone.getDensityOfPeople());
            }
            break;
        default:
            log.error("Unknown zone type: {}", outZone.getType());
            break;
        }

        return vZone;
    }
}
