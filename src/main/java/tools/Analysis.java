/*
 * Copyright (C) 2017 Chirkov Boris <b.v.chirkov@udsu.ru>
 *
 * Project website:       http://eesystem.ru
 * Organization website:  http://rintd.ru
 *
 * --------------------- DO NOT REMOVE THIS NOTICE -----------------------------
 * Analysis is part of jSimulationMoving.
 *
 * jSimulationMoving is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jSimulationMoving is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jSimulationMoving. If not, see <http://www.gnu.org/licenses/>.
 * -----------------------------------------------------------------------------
 *
 * This code is in BETA; some features are incomplete and the code
 * could be written better.
 */

package tools;

import json.extendetGeometry.BIMExt;
import json.extendetGeometry.TransitionExt;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Класс, содержащий интсрументы для исследования модели
 *
 * Created by boris on 02.02.17.
 */
public class Analysis {

    private final String  fileName;
    private final boolean isFire;
    private final double  density;

    private BIMExt                             bim;
    private HashMap<String, TransitionExt>     transitionsMap;
    private HashMap<String, String>            uuidMap;
    private HashMap<String, ArrayList<String>> timeList;

    public Analysis(BIMExt bim, String fileName, boolean isFire, double density) {
        this.bim = bim;
        this.fileName = fileName;
        this.isFire = isFire;
        this.density = density;

        uuidMap = new HashMap<>();
        uuidMap.put("{224ef342-55df-4010-9e95-5950a57a084d}", "A");
        uuidMap.put("{cc336af5-e1ff-4db1-b911-9b9559449877}", "B");
        uuidMap.put("{d2e5bd86-6409-4e69-ab1c-2702891ac11a}", "C");
        uuidMap.put("{fb7da305-ddd3-4cb5-b0a4-2edb187f178c}", "D");
        uuidMap.put("{b80aea26-3251-4709-a73e-85bd44328c57}", "E");
        uuidMap.put("{9deead9b-205a-4114-94ef-ea30e7276c9b}", "F");
        uuidMap.put("{3cf17758-3417-4b83-bb7c-1076cb351c2d}", "G");

        timeList = new HashMap<>();
        transitionsMap = new HashMap<>();
        for (TransitionExt t : bim.getTransitions())
            for (String uuid : uuidMap.keySet())
                if (uuid.equals(t.getId())) {
                    transitionsMap.put(uuid, t);
                    ArrayList<String> sb = new ArrayList<>();
                    sb.add(0.0 + ";" + 0.0);
                    timeList.put(uuid, sb);
                }
    }

    public HashMap<String, String> getUuidMap() {
        return uuidMap;
    }

    public HashMap<String, ArrayList<String>> getTimeList() {
        return timeList;
    }

    public void counterPeopleThroughDoor(double time) {
        transitionsMap.forEach((k, v) -> {
            if (timeList.containsKey(k)) {
                ArrayList<String> sb = timeList.get(k);
                double nop = v.getNumOfPeoplePassing();
                sb.add(time + ";" + ((nop < 0.5) ? 0.0 : nop));
                timeList.replace(k, sb);
            }
        });
    }

    public HashMap<String, TransitionExt> getTransitionsMap() {
        return transitionsMap;
    }

    public void getBlockedZone(String uuid, double time) {
        bim.getZones().values().forEach(z -> {
            if (z.getId().equals(uuid) && z.isBlocked())
                System.out.println("Zone: " + uuid + "::" + "blocked time: " + time);
        });
    }

    public void saveResult(ArrayList<String> val, double nop, String note) {
        StringBuilder sb = new StringBuilder(fileName);
        String fName = sb.delete(fileName.length() - 4, fileName.length()).append(isFire ? "-f" : "-nf").append("-d")
                .append(density).append("-p").append(new BigDecimal(nop).setScale(1, RoundingMode.UP).doubleValue())
                .append("-").append(note).toString();
        try {
            PrintWriter writer = new PrintWriter("src/main/resources/out-170206/" + fName + ".csv", "UTF-8");
            for (String s : val) writer.println(s);
            writer.close();
        } catch (IOException ignored) {}
    }

    public <K, V> Map.Entry<K, V> getLast(LinkedHashMap<K, V> map) {
        Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();
        Map.Entry<K, V> result = null;
        while (iterator.hasNext()) {
            result = iterator.next();
        }
        return result;
    }

}


/*
{224ef342-55df-4010-9e95-5950a57a084d} - A
{cc336af5-e1ff-4db1-b911-9b9559449877} - B
{d2e5bd86-6409-4e69-ab1c-2702891ac11a} - C
{fb7da305-ddd3-4cb5-b0a4-2edb187f178c} - D
{b80aea26-3251-4709-a73e-85bd44328c57} - E
{9deead9b-205a-4114-94ef-ea30e7276c9b} - F
{3cf17758-3417-4b83-bb7c-1076cb351c2d} - G
*/
