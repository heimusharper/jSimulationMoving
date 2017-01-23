/*
 * Copyright (C) 2017 Chirkov Boris <b.v.chirkov@udsu.ru>
 *
 * Project website:       http://eesystem.ru
 * Organization website:  http://rintd.ru
 *
 * --------------------- DO NOT REMOVE THIS NOTICE -----------------------------
 * ReadCsvFds is part of jSimulationMoving.
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

package fds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;

import static fds.DevcHelper.*;

/**
 * Чтение выходного файла FDS *_devc.csv
 * <p>
 * Created by boris on 19.01.17.
 */
public class ReadFDSOutput {

    private static final Logger log = LoggerFactory.getLogger(ReadFDSOutput.class);

    /**
     * Чтение результатов моделирования. Результаты представлены в формате csv, где разделителем служит запятая.
     * Первые два индекса именования девайсов должны обозначать тип девайса. Типы задаются в классе {@link DevcHelper}
     *
     * @param fileName имя файла. Файл должен лежать в каталоге ресуросов.
     * @return {@link LinkedHashMap}, где в качестве ключа - время (значения первой колонки), значения - список девайсов
     */
    public static LinkedHashMap<Double, ArrayList<DevcHelper>> readDevc(String fileName) {
        int column = 0;
        int line = 0;
        double time = 0;
        LinkedHashMap<Double, ArrayList<DevcHelper>> empMap = new LinkedHashMap<>();
        ArrayList<Integer> types = new ArrayList<>(); // Список типов девайсов
        ArrayList<String> ids = new ArrayList<>(); // Список идентификаторов помещений

        // open file input stream
        try (InputStream is = ReadFDSOutput.class.getClassLoader().getResourceAsStream(fileName);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            // read file line by line
            for (String l = reader.readLine(); l != null; l = reader.readLine(), line++, time = 0, column = 0) {
                /* Список показателей датчиков строки в момент времени time */
                ArrayList<DevcHelper> empList = new ArrayList<>();

                col:
                for (Scanner scanner = new Scanner(l).useDelimiter(","); scanner.hasNext(); column++) {
                    String data = scanner.next().replaceAll("\"", ""); // Обрасываем лишние ковычки
                    switch (line) {
                    case 0: // пропускаем первую строку
                        break col;
                    case 1:
                        // Смотрим сколько столбцов разного типа: температура и видимость
                        // Группируем номера столбцов по типам
                        types.add(identifyType(data.contains("Time") ? data : data.substring(0, 2)));
                        if (column > 0) try {
                            if (data.length() > 3) ids.add(data.substring(3));
                            else throw new Error("Length string < 3. Source: " + data);
                        } catch (StringIndexOutOfBoundsException e) {
                            log.error("Source {} ", data, e);
                        }
                        break;
                    default: // Сбор данных
                        if (types.get(column) == UNKNOWN) throw new Exception(
                                String.format("Unknown column type. Column = %d,  Type = %d", column,
                                        types.get(column)));

                        double value = Double.parseDouble(data);
                        if (column == TIME) { // Берем время из первой колонки
                            time = value;
                            break;
                        }

                        DevcHelper emp = new DevcHelper();
                        emp.setType(types.get(column));
                        emp.setId(ids.get(column - 1));
                        emp.setValue(value);
                        emp.setTime(time);
                        empList.add(emp);
                    }
                }
                if (line >= 2) empMap.put(time, empList);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("Complete read FDS output file {}", fileName);
        return empMap;
    }
}
