/*
 * Copyright (C) 2016 Chirkov Boris <b.v.chirkov@udsu.ru>
 *
 * Project website:       http://eesystem.ru
 * Organization website:  http://rintd.ru
 *
 * --------------------- DO NOT REMOVE THIS NOTICE -----------------------------
 * DBus is part of jSimulationMoving.
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

package bus;

import json.extendetGeometry.BIMExt;

/**
 * Общая шина данных. <br>
 * Created by boris on 20.12.16.
 */
public class DBus {

    /**
     * Сырой json
     */
    private static String rawJson;
    /**
     * Разобраный json
     */
    private static BIMExt bim;

    public static String getRawJson() {
        return DBus.rawJson;
    }

    public static void setRawJson(final String rawJson) {
        DBus.rawJson = rawJson;
    }

    public static BIMExt getBim() {
        return bim;
    }

    public static void setBim(BIMExt bim) {
        DBus.bim = bim;
    }
}
