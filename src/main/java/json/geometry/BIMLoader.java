/******************************************************************************
 Copyright (C) 2016 Chirkov Boris <b.v.chirkov@udsu.ru>

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
 ------------------------------------------------------------------------------

 This code is in BETA; some features are incomplete and the code
 could be written better.
 *****************************************************************************/

package json.geometry;

import com.google.gson.Gson;
import json.errors.ExitCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Калсс загрузки и десериализации ПИМ здания.
 * <p>
 * Created by boris on 13.12.16.
 *
 * @param <B> класс, описывающий структуру здания. <br>
 */
public class BIMLoader<B extends BIM> {

    private static final Logger log = LoggerFactory.getLogger(BIMLoader.class);

    private B bim; // Класс для сериализации
    private String res = ""; // Содержимое файла *.json

    /**
     * Конструктор загрузчика пространственно-информационной модели здания.
     *
     * @param is    поток для чтения
     * @param clazz класс, описывающий структуру здания. Обязательно должен
     *              быть расширением {@link BIM}
     */
    public BIMLoader(final InputStream is, final Class<B> clazz) {
        final String className = clazz.getName();

        try {
            bim = clazz.newInstance(); // Инициализация нового объекта
            log.info("Created instance for class {}", clazz.getName());
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("Fail! Can not create new instance of class {}", className, e);
        }

        log.info("Read json data from stream");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
            String s;
            while ((s = in.readLine()) != null) res += s;

            checkResults(res);

            bim = new Gson().fromJson(res, clazz); // Парсинг *.json
            log.info("Successful parse json to {} structure", className);
        } catch (final IOException e) {
            log.error("Fail: parse json to {} structure. Any problems: ", className, e);
        }
    }

    /**
     * Проверка на наличие содержимого в файле
     *
     * @param res строка с содержанием *.json файла
     */
    private static void checkResults(String res) {
        if (res.isEmpty()) {
            // Если файл пустой, то завершаем программу с
            // кодом ошибки 1, и выводим сообщение об ошибке
            log.error("File *.json is empty", new Error());
            System.exit(ExitCode.FILE_EMPTY);
        }

        log.info("Successful read json");
    }

    /**
     * @return Структуру здания в формате BIM
     */
    public B getBim() {
        return bim;
    }

    /**
     * @return Текстовый вариант содержимого файла *.json
     */
    public String getRawJson() {
        return res;
    }
}
