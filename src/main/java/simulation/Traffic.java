/******************************************************************************
 Copyright (C) 2016 KolodkinVM <kolodkin@rintd.ru>
 
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

import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import json.extendetGeometry.Direction;
import json.extendetGeometry.TransitionExt;
import json.extendetGeometry.ZoneExt;

public class Traffic {
    private ArrayList<ZoneExt>       zones;
    private ArrayList<TransitionExt> transitions;
    private float                    tay;
    private float                    time;
    
    /**
     * Коэфф., определяющий макс. плотность в ячейке (площадь гориз. проекции человека, м2/чел)
     */
    private static final double FP = 0.113;
    
    /**
     * Коэффициент проходимости зоны. Если значение проходимости зоны ниже этого значения, зона не проходима
     */
    private static final double CR_PASSABILITY = 0.1;
    
    /**
     * Количество людей в здании. Если поставить число <=0, то распределение людей берется из ПИМ здания.
     */
    private static double NUMBER_OF_PEOPLE;

    public Traffic(ArrayList<ZoneExt> zones,
            ArrayList<TransitionExt> transitions, float tay, float time) {
        this.zones = zones;
        this.transitions = transitions;
        this.tay = tay;
        this.time = time;
        NUMBER_OF_PEOPLE = zones.stream().mapToDouble(ZoneExt::getNumOfPeople).sum();
    }
    
    private int getNumOfExit () {
        return 3;
    }

    /**
     * Моделирование Людских потоков по зданию 18/08/2015, 22.12.2016
     */
    public void footTraffic() {
        final double Vmax = 100.0; // Максимальная скорость (метров в минуту)
        final double dxyz = 0.1 * tay * Vmax; // Оценка точности представления координат
        final int numExit = getNumOfExit(); // Количество выходов из здания
        
        // Списки на выход (в количестве numExit);
        final ArrayList<ArrayList<ZoneExt>> zoneOut = new ArrayList<ArrayList<ZoneExt>>(numExit);
        for(int i = 0; i < numExit; i++)
            zoneOut.add(new ArrayList<ZoneExt>());
            
        final int k_out[] = new int[numExit]; // номер обрабатываемого элемента списка выхода ii
        final int finis_out[] = new int[numExit]; // номер последнего элемента списка выхода ii
        final int iiWork[] = new int[numExit]; // номера очередей ii с обрабатываемыми элементами, отсортированными по
                                               // возрастанию плотности
        final double dZone0[] = new double[numExit]; // плотность (раб. массив для сортировки по плотности)
        
        final boolean[] outstep = new boolean[numExit]; // признак выхода из главного цикла по отсутствию людей (do)
        
        final Supplier<IntStream> loop = () -> IntStream.range(0, numExit);
        
        
        // Формирование и обработка (одновременная) списков к каждому выходу
        for(int kkktay = 1; kkktay <= time; kkktay++) { // Цикл по времени процесса
            boolean xyz = false;
            loop.get().forEach(i -> outstep[i] = true);
            
            // Обработка первого элемента
            final ZoneExt xZone = getSafetyZone(); // Зона безопасности
            loop.get().parallel().forEach(i -> { // 01 Loop of output
                zoneOut.get(i).clear();
                final int idTransition = xZone.getTransition(i); // Идентификатор портала на улицу
                final TransitionExtended _transition = transitionsList.get(idTransition);
                final double lportal = _transition.getWidth(); // Ширина проема на улицу
                
                // Получаем Зоны, которые граничат с эвакуационным выходом
                final int idZone1 = _transition.getZoneAIdInt();
                final int idZone2 = _transition.getZoneBIdInt();
                
                // Определяем зону, которая находится в здании и граничит с эвакуационным выходом
                int idZone = -10000; // idZone -> Id помещения рядом с выходом на улицу
                if(idZone1 == 0)
                    idZone = idZone2;
                else if(idZone2 == 0) idZone = idZone1;
                
                // Обработка проходимой зоны, (рядом с улицей)
                final ZoneExtended _zone = zonesList.get(idZone);
                final ZoneType zoneType = _zone.getType();
                final double dPeopleZone = _zone.getNumOfPeople();
                final double sZone = _zone.getArea();
                double vZone = Double.NaN; // Скорость движения в зоне

                // Определяем плотность людей в зоне, чел/м2
                final double dZone = dPeopleZone / sZone;
                switch(zoneType) {
                case FLOOR:
                    vZone = velem(dZone);
                    break;
                case STAIRS:
                    final double hElem = _zone.getZLevel();
                    final double hElem0 = xZone.getZLevel();
                    final double dh = hElem - hElem0; // Разница высот зон
                    if(Math.abs(dh) >= dxyz) {
                        // Определяем направление движения по лестнице
                        Direction direction = (hElem > hElem0) ? Direction.DOWN : Direction.UP;
                        _zone.setDirect(direction);
                        vZone = velemz(direction, dZone);
                    } else vZone = velem(dZone);
                    break;
                default:
                    log.log(Level.SEVERE, "Неопределенный тип зоны");
                    break;
                }
                
                final double vTransition = velem(lportal, dZone); // Скорость движения в дверях
                final double v = Math.min(vZone, vTransition); // Скорость на выходе из здания
                final double d1 = lportal * v * tay / sZone; // ! Не знаю что это
                
                final double d2 = (d1 > 1) ? 1 : d1;
                
                final double dPeopl = d2 * dPeopleZone; // Изменение численности людей в помещении рядом с выходом
                final double delta = _zone.getNumOfPeople() - dPeopl;
                
                final double ddPeople = (delta > 0) ? dPeopl : dPeopleZone;
                
                xZone.addPeople(ddPeople); // Увеличение людей в безопосной зоне
                _zone.removePeople(ddPeople); // Уменьшение людей в зоне здания
                
                _transition.addPeopleTransition(ddPeople); // Увеличение людей через дверь ii
                _transition.incrementNtay(); // Признак обработки двери
                _transition.setNumberOutput(i); // Выход через дверь ii на улицу
                
                final int indp = _transition.getNtay();
                _zone.setNtay(indp); // Признак обработки элементa здания
                _zone.setNumberOutput(i); // Помещение освобождается через выход ii
                
                final double timeout = (v > 0) ? Math.sqrt(sZone) / v : 0.0; // Потенциал времени в первом помещении
                _zone.setTimeout(timeout);
                
                zoneOut.get(i).add(_zone); // Increase List
                k_out[i] = 0;
                finis_out[i] = 0;
                // finis of passable processing zones
            }); // 01 Cycle of output // Finis of the cycle of output            
            
        }
    }
    
    private ZoneExt getSafetyZone() {
        return null;
    }

    /**
     * Скорость в проеме, м/мин | 31/08/2013 11.08.2015
     * @param l - ширина проема, м
     * @param dElem - плотность в элементе
     * @return
     */
    private static double velem(final double l, final double dElem) {
        final double v0 = 100; // м/мин
        final double d0 = 0.65;
        final double a = 0.295;
        
        double v0k;
        if(dElem >= 9)
            v0k = 10 * (2.5 + 3.75 * l) / d0;    //   07.12.2016
        else if(dElem > d0) {
            final double m = dElem >= 5 ? 1.25 - 0.05 * dElem : 1;
            v0k = v0 * (1.0 - a * Math.log(dElem / d0)) * m;
        } else v0k = v0;
        
        return v0k;
    }
    
    /**
     * Скорость на лестнице, м/мин | 13.08.02 11/08/2015
     * @param direct - направление движения (direct = 3 - вверх, = -3 - вниз)
     * @param dElem - плотность в элементе
     * @return
     */
    private static double velemz(final int direct, final double dElem) {
        double d0 = 0, v0 = 0, a = 0;
        
        switch(direct) {
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
            System.err.println(" !!! Mistake Traffic-55  !!! direct=" + direct);
            break;
        }
        
        final double v0k = dElem > d0 ? v0 * (1.0 - a * Math.log(dElem / d0)) : v0;
        
        return v0k;
    }
    
    /**
     * Скорость м/мин ... - горизонтальный путь в здании 11/08/2015
     * @param dElem - плотность в элементе
     * @return
     */
    private static double velem(final double dElem) {
        final double v0 = 100; // м/мин
        final double d0 = 0.51;
        final double a = 0.295;
        
        final double v0k = dElem > d0 ? v0 * (1.0 - a * Math.log(dElem / d0)) : v0;
        
        return v0k;
    }
    
    private static double dmaxxyz() {
        return 5.0;
    }

}
