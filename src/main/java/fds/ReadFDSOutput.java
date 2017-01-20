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

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import static fds.DevcHelper.*;

/**
 * Чтение выходного файла FDS *_devc.csv
 * <p>
 * Created by boris on 19.01.17.
 */
public class ReadFDSOutput {

    private static final Logger log = LoggerFactory.getLogger(ReadFDSOutput.class);

    public static HashMap<Double, ArrayList<DevcHelper>> readDevc(String fileName) throws FileNotFoundException {
        // open file input stream
        InputStream is = ReadFDSOutput.class.getClassLoader().getResourceAsStream(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        // read file line by line
        String l;
        Scanner scanner;
        int column = 0;
        int line = 0;
        HashMap<Double, ArrayList<DevcHelper>> empMap = new HashMap<>();
        ArrayList<Integer> types = new ArrayList<>();
        ArrayList<String> ids = new ArrayList<>();

        try {
            while ((l = reader.readLine()) != null) { // Получаем строку из файла
                scanner = new Scanner(l);
                scanner.useDelimiter(",");
                ArrayList<DevcHelper> empList = new ArrayList<>();
                double time = 0;

                col:
                while (scanner.hasNext()) {
                    String data = scanner.next();
                    data = data.replaceAll("\"", ""); // Обрасываем лишние ковычки
                    switch (line) {
                    case 0:
                        log.info("Пропустили строку с единицами измерения");
                        break col;
                    case 1:
                        // Смотрим сколько столбцов разного типа: температура и видимость
                        // Группируем номера столбцов по типам
                        types.add(identifyType(data.contains("Time") ? data : data.substring(0, 2)));
                        if (column > 0) {
                            try {
                                ids.add(data.substring(3));
                            } catch (StringIndexOutOfBoundsException e) {
                                log.error("Source {} ", data, e);
                            }
                        }
                        break;
                    default: // Сбор данных
                        if (types.get(column) == UNKNOWN) {
                            throw new Exception(String.format("Unknown column type. Column = %d,  Type = %d", column,
                                    types.get(column)));
                        }

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
                    column++;
                }

                if (line >= 2) empMap.put(time, empList);
                column = 0;
                line++;
            }
            //close reader
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("Complete read FDS output file {}", fileName);
        return empMap;
    }
}
