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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import json.extendetGeometry.BIMExt;
import json.extendetGeometry.Direction;
import json.extendetGeometry.SafetyZone;
import json.extendetGeometry.TransitionExt;
import json.extendetGeometry.ZoneExt;
import json.geometry.Zone;

public class Traffic {
    private final static Logger      log            = LoggerFactory
            .getLogger(Traffic.class);

    /**
     * Площадь горизонтальной проекции человека, м^2 (Коэфф.,
     * определяющий максимальную плотность в ячейке)= 0.1 - 0.125 м^2/чел.
     */
    private final double             FP             = 0.113;
    /**
     * Коэффициент проходимости зоны. Если значение проходимости зоны ниже этого
     * значения, зона не проходима
     */
    private final double             CR_PASSABILITY = 0.1;
    /**
     * Максимальная скорость движения людского потока, м/мин
     */
    private final double             V_MAX;
    // private final double TIME;
    /**
     * Количество эвакуационных выходов
     */
    private final int                NUM_OF_EXITS;
    /**
     * Максимальная плотность людского потока
     */
    private final double             D_MAX;
    /**
     * Безопасная зона
     */
    private SafetyZone               safetyZone;
    /**
     * Индексированный списко зон, где инддекс - uuid зона
     */
    private HashMap<String, ZoneExt> zones;
    /**
     * Список проемов в здании
     */
    private List<TransitionExt>      transitions;
    /**
     * Список эвакуационных выходов
     */
    private List<TransitionExt>      exits;
    /**
     * Количество людей в здании. Если поставить число <=0, то распределение
     * людей берется из ПИМ здания.
     */
    private double                   numOfPeople;

    public Traffic(BIMExt bim) {
        this.safetyZone = bim.getSafetyZone();
        this.zones = bim.getZones();
        this.transitions = bim.getTransitions();
        this.exits = bim.getExitsTransition();
        this.numOfPeople = bim.getNumOfPeople();
        this.NUM_OF_EXITS = bim.getNumOfExits();
        this.D_MAX = 5.0;
        this.V_MAX = 100.0; // м/мин
    }

    /**
     * Моделирование Людских потоков по зданию 18/08/2015, 29.12.2016,
     * 9.01.2017
     * 
     * @param time
     *            временной интервал моделирования эвакуации в секундах
     */
    public void footTraffic(double time) {

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
        
        // Оценка точности представления координат, метры 
        final int numberEvacCycle = getNumberEvacCycle(time, getTay());

        // Формирование и обработка (одновременная) списков к каждому выходу
        // Цикл по времени процесса
        for (int kkktay = 1; kkktay <= numberEvacCycle; kkktay++) { // kkktay=1
          boolean xyz = false;
          for (int ii = 0; ii < NUM_OF_EXITS; ii++)
              outstep[ii] = true;

          // Обработка первого элемента
          SafetyZone safetyZone = this.safetyZone; // Зона безопасности
          for (int ii = 0; ii < NUM_OF_EXITS; ii++) {     // 01
              zoneOut.get(ii).clear();
   
              // Идентификатор портала на улицу
              TransitionExt exit = exits.get(ii);
              // Ширина проема на улицу
              double widthTransition = exit.getWidth();

              // Определяем зону, которая находится в здании и зону, которая
              // граничит с эвакуационным выходом
              String uuidZone = null;
              if (exit.getZoneAId() == null) uuidZone = exit.getZoneBId();
              if (exit.getZoneBId() == null) uuidZone = exit.getZoneAId();

              // ---- Входим в здание ---
              // Обработка первой (проходимой) зоны (рядом с улицей)
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
             
              safetyZone.addPeople(ddPeople);// Увел. людей в безоп. зоне
              _zone.removePeople(ddPeople);  
              // Увеличение счетчика людей, прошедших через дверь ii
              exit.addPassingPeople(ddPeople);
              // Признак обработки двери. Увеличение TransitionExt#nTay на
              // единицу
              exit.nTayIncrease();
              // Выход через дверь ii на улицу. Присвоили выходу номер
              exit.setNumberExit(ii);
              // Признак обработки элемента здания
              // Метка, которая говорит, что это помещение уже обработано
              // ii-той дверью
              _zone.setNTay(exit.getNTay());
              // Помещение освобождается через выход ii
              _zone.setNumberExit(ii);
              // Потенциал времени в первом помещении. Время достижения
              // эвакуационного выхода из зоны
              _zone.setTimeToReachExit(
                        (vAtExit > 0) ? Math.sqrt(sZone) / vAtExit : 0.0);

              // Добавляем ближайшую к выходу зону в соответствующий список
              // "на выход"
              zoneOut.get(ii).add(_zone); // Increase List
              k_out[ii] = 0;
              finish_out[ii] = 0;
         } // 01 Cycle of output // Finish of the cycle of output
  
        // Формирование последующих элементов в каждом списке к выходу
        do { // 1 Проход по ячейкам и всасывание людей из ячеек, соседних с
             // наполняемой ячейкой на шаге tay
         for (int j = 0; j < NUM_OF_EXITS; j++) {
             dZone0[j] = Double.NaN;
             iiWork[j] = NUM_OF_EXITS - 1;
             if (outstep[j] == true) {
                final ZoneExt elem0 = zoneOut.get(j).get(k_out[j]);
                dZone0[j] = elem0.getTimeToReachExit();
                iiWork[j] = j;
             }
         } ;

        // Сортировка элементов firstElem по времени достижения зоны
        // безопасности 4.06.2015
        // Если нужна сортировка по убыванию, то нужно добавить третий
        // параметр в метод - REVERSE
        // sortingDensity(dZone0, iiWork, REVERSE) или
        // sortingDensity(dZone0, iiWork, true);
        sortingDensity(dZone0, iiWork);
        // Loop of output
        for (int iiTurn = 0; iiTurn < NUM_OF_EXITS; iiTurn++) { // == 001 
           final int ii = iiWork[iiTurn];
           if (ii < 0) {
               System.out.println();
               System.err.println(" !!!   Mistake Traffic - 261  ii=" + ii
                    + "  outstep[ii]= " + outstep[ii] + "  iiTurn ="
                    + iiTurn + "  numExit = " + NUM_OF_EXITS);
               for (int ii2 = 0; ii2 < NUM_OF_EXITS; ii2++)
                  System.out.println(" iiWork[" + ii2 + "]=" + iiWork[ii2]
                            + "  dElem0[ii]=" + dZone0[ii2]);
                  System.out.println();
              }

        boolean evacOutLimit = false;  //ограничитель эвакуации к выходу 
   do {                            // constDirec = true;       ==02  
     if (outstep[ii]) {    //    outstep[ii] == true   ==2
           // Выбираем элемент списка, в который будем "засасывать"
           // людей и проверяем на проходимость
       ZoneExt receivingArea = zoneOut.get(ii).get(k_out[ii]);
           // Обработка проходимой зоны                     
       if (receivingArea.getPermeability() >= CR_PASSABILITY) {  //  == 3
                          // Количество дверей элемента
         int numTransitions = receivingArea.getTransitionList().size(); 
                          // Обходим все двери зоны
         for (int iip = 0; iip < numTransitions; iip++) {  //  == 5
                          // Идентификатор одной из дверей зоны
          TransitionExt exit = transitions.get(iip); 
                          // Портал подлежит обработке
          if (receivingArea.getNTay() > exit.getNTay()) {   //   ==41
 //          final double lTransition = exit.getWidth(); // Ширина 
                           // Ссылки от портала 
           String idZone1 = exit.getZoneAId();// на помещение A   
           String idZone2 = exit.getZoneAId();// на помещение B
           String idZoneOId = receivingArea.getId();                     
           String idZone = (idZone1.equals(idZoneOId))? idZone2 : idZone1;
           ZoneExt radiatingArea = zones.get(idZone);
           
  //         double ddPeople = procZones(radiatArea, zone0,exit, dxyz, tay);
  //         private  double procZones(ZoneExt radiatingArea, ZoneExt receivingArea, 
  //                 TransitionExt exit, double dxyz, double tay)         
           double dPeopleZone = radiatingArea.getNumOfPeople(); // кол.людей
           double sZone = radiatingArea.getArea();              // Площадь
           double vZone = vElem(radiatingArea, receivingArea, ii);
           double dZone = radiatingArea.getDensityOfPeople();        // плотность, чел/м2  
           double lTransition = exit.getWidth();     // Ширина проема
           double vTransition = vElem(lTransition, dZone);
           double v = receivingArea.getPermeability()
                   * Math.min(vZone, vTransition); // Скорость на выходе
           double d1 = lTransition * v * getTay() / sZone;
           double d2 = (d1 >= 1) ? 1 : d1;
           double dPeopl = d2 * dPeopleZone; // Изменение кол.людей
           double delta = dPeopleZone - dPeopl; // Отдающая
               // ddPeople - кол. людей, которые могут быть высосаны  
           double ddPeople = (delta > 0) ? dPeopl: dPeopleZone; // изменение        

           double maxPeople = D_MAX*receivingArea.getArea(); 
           double numP = maxPeople-receivingArea.getNumOfPeople(); 
           double changePeople = (numP > ddPeople)? ddPeople : numP;
           
           receivingArea.addPeople(changePeople);// Увелич. людей в принимающей 
           radiatingArea.removePeople(changePeople);// Уменьшение людей 
           exit.addPassingPeople(changePeople);  // Увел. людей через дверь
           exit.nTayIncrease();                  // Признак  обработки двери
           exit.setNumberExit(ii);
           radiatingArea.setNTay(exit.getNTay());     // Признак обраб. элементa здания
           radiatingArea.setNumberExit(ii); // Помещ. освобож. через выход ii
                                    // Потенциал времени в первом помещении
           double timeout = (v > 0)? receivingArea.getTimeToReachExit()
              + Math.sqrt(sZone) / v: receivingArea.getTimeToReachExit();
              radiatingArea.setTimeToReachExit(timeout);

           zoneOut.get(ii).add(radiatingArea);
           finish_out[ii]++;
          } // ==41
         } //  == 5 Обработка списка порталов
        } // ==3
         if (finish_out[ii] > k_out[ii]) {
            k_out[ii]++;  evacOutLimit = true;
            } else {
                outstep[ii] = false;  evacOutLimit = false;  }
      } // outstep[ii] == true ==2
                        if (evacOutLimit) { // 5/06/2015
                            final int iikd = iiTurn + 1;
                            if (iikd >= NUM_OF_EXITS) evacOutLimit = false;
                            else {
                                if (zoneOut.get(ii).get(k_out[ii])
                                        .getTimeToReachExit() > dZone0[iikd]) {
                                    evacOutLimit = false;
                                } else {
                                    evacOutLimit = true;
                                }
                            }
                        }
   } while (evacOutLimit); // 02 - finis
            } // Обработка очереди ii           // == 001 
            xyz = false;
            for (int ii1 = 0; ii1 < NUM_OF_EXITS; ii1++)
                if (outstep[ii1] == true) xyz = true;
        } while (xyz); // do 1

        if (Math.abs(safetyZone.getNumOfPeople() - numOfPeople) < 0.5) {
            safetyZone.setNumberExit(-kkktay);
            // ????? return zones;
        } // Выход из цикла моделирования (В здании нет людей).
    }     // kkktay=1
    }


    // Цикл по времени процесса
 

    /**
     * Метод сортировки плотностей перед выходами
     * 
     * @param dZone0
     * @param iiWork
     * @param b
     *            - параметр, который отвечает за обратную сортировку. Если
     *            true, то включается обратная сортировка, если параметр
     *            отсутствует, то
     *            сортировка по возрастанию
     */
    private static void sortingDensity(final double[] dZone0,
            final int[] iiWork, boolean... b) {
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
                    boolean direction = b.length > 0 && b[0]
                            ? dZone0[i] < dZone0[j] : dZone0[i] > dZone0[j];
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

    static final boolean REVERSE = true;

    /**
     * @return шаг моделирования процесса эвакуации,мин
     */

    private double getTay() {
        double hxy = 0.5; // характерный размер области, м
        double ktay = 0.5; // коэффициент (< 1) уменьшения шага по времени
        // для устойчивости расчетов
        // Шаг моделирования, мин
        return (hxy / V_MAX) * ktay;
    }

    /**
     * @param time
     *            - временной интервал моделирования, секунды
     * @param tay
     *            - временной шаг моделирования, минуты
     * @return количество проходов по циклу моделирования эвакуации
     */
    private int getNumberEvacCycle(double time, double tay) {
        if (time <= (tay * 60)) return 1;
        else return (int) (time / (tay * 60));
    }

    /**
     * @param l             ширина проема, м
     * @param dElem         плотность в элементе
     * @return Скорость потока в проеме в зависимости от плотности, м/мин
     */
    private static double vElem(final double l, final double dElem) {
        double v0 = 100; // м/мин
        double d0 = 0.65;
        double a = 0.295;
        double v0k = 0;
        if (dElem >= 9) v0k = 10 * (2.5 + 3.75 * l) / d0; // 07.12.2016
        else if (dElem > d0) {
            double m = dElem >= 5 ? 1.25 - 0.05 * dElem : 1;
            v0k = v0 * (1.0 - a * Math.log(dElem / d0)) * m;
        } else v0k = v0;
        return v0k;
    }

    /**
     * @param dElem        плотность в элементе
     * @return Скорость потока по горизонтальному пути, м/мин
     */
    private static double vElem(final double dElem) {
        double v0 = 100; // м/мин
        double d0 = 0.51;
        double a = 0.295;
        return dElem > d0 ? v0 * (1.0 - a * Math.log(dElem / d0)) : v0;
    }

    /**
     * @param direct
     *            направление движения (direct = 3 - вверх ({@link
     *            Direction#UP}), = -3 - вниз ({@link Direction#DOWN})
     * @param dElem          плотность в элементе
     * @return Скорость потока при движении по лестнице в зависимости от
     *         плотности, м/мин
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

    /**   30.12.2016 - Колодкин
     * @param radiatingArea   - зона отдающая 
     * @param receivingArea   - зона принимающая  
     * @param exit            - переход между отдающей и принимающей зонами
     * @param dxyz            - точность представления координат
     * @param tay             - временной шаг, мин
     * @return 
     */
    @SuppressWarnings("unused")
    private  double procZones(ZoneExt radiatingArea,/* String idRadiatingArea,*/ ZoneExt receivingArea, 
                    TransitionExt exit, double dxyz, double tay) {
                                    // Отдающая зона
//        final ZoneExt radiatingArea = zones.get(idRadiatingArea);           
        final int zoneType = radiatingArea.getType();              // тип
        final double dPeopleZone = radiatingArea.getNumOfPeople(); // кол.людей
        final double sZone = radiatingArea.getArea();              // Площадь
        double vZone = Double.NaN;
        final double dZone = dPeopleZone/sZone;        // плотность, чел/м2  
        switch (zoneType) {
          case ZoneExt.FLOOR: vZone = vElem(dZone);  break;
          case ZoneExt.STAIRS:
              final double hElem  = radiatingArea.getMinZ();    // отдающая
              final double hElem0 = receivingArea.getMinZ();    // принимающая
              final double dh = hElem - hElem0;    // Разница высот зон
              if (Math.abs(dh) >= dxyz) {
              int direction = (hElem > hElem0)? Direction.DOWN
                                              : Direction.UP;
              receivingArea.setDirection(direction);
              vZone = vElemZ(direction, dZone); }
                    else vZone = vElem(dZone);
               break;
          default: log.info("Неопределенный (procZones) тип зоны");  break;
        }
        final double lTransition = exit.getWidth();     // Ширина проема
        double vTransition = vElem(lTransition, dZone);
        final double v = receivingArea.getPermeability()
                * Math.min(vZone, vTransition); // Скорость на выходе
        final double d1 = lTransition * v * tay / sZone;
        double d2 = (d1 >= 1) ? 1 : d1;
        final double dPeopl = d2 * dPeopleZone; // Изменение кол.людей
        final double delta = dPeopleZone - dPeopl; // Отдающая
        double ddPeople = (delta > 0) ? dPeopl: dPeopleZone; // изменение
        return ddPeople;
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
        
    
    /*      final double dmaxElem0 = D_MAX; // максимально-допустимая
    // плотность
    // в зоне,
// куда засасываются люди
// if(dmaxElem0 < 0)
// Evacuation.out_results.println(" Mistake
// Traffic - 308 dmaxElem0= " + dmaxElem0);   */

// max nubber people in area (куда  засасываем)
    
    
    
  /*      
        final double dmaxElem0 = D_MAX; // максимальная плот. в зоне 


                                 final double maxPeople = dmaxElem0
                                         * zone0.getArea(); // max nubber
                                                            // people in room
                                                            // elem0 (куда
                                                            // засасываем)
                                 final double numP = maxPeople
                                      // Вместимость элемента elem0 на данный момент времени
                                         - zone0.getNumOfPeople(); 
                                 final double changePeople = (numP > ddPeople)
                                         ? ddPeople : numP;
                              // Увеличение людей в безопасной зоне
                                 zone0.addPeople(changePeople);
                                 _zone.removePeople(changePeople); // Уменьшение
                                                                   // людей в
                                                                   // элементе
                                                                   // здания
                                 // Увеличение людей через дверь ii
                                 _transition.addPassingPeople(changePeople);
                                 _transition.nTayIncrease(); // Признак
                                                             // обработки
                                                             // двери
                                 _transition.setNumberExit(ii);

                                 final int indp = _transition.getNTay();
                                 _zone.setNTay(indp); // Признак обработки
                                                      // элементa здания
                                 _zone.setNumberExit(ii); // Помещение
                                                          // освобождается
                                                          // через выход ii

                                 // Потенциал времени в первом помещении
                                 final double timeout = (v > 0)
                                         ? zone0.getTimeToReachExit()
                                                 + Math.sqrt(sZone) / v
                                         : zone0.getTimeToReachExit();
                                 _zone.setTimeToReachExit(timeout);

     */   
       
}
