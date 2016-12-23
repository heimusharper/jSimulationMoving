/*******************************************************************************
 * Copyright (C) 2016 Chirkov Boris <b.v.chirkov@udsu.ru>
 *
 * Project website:       http://eesystem.ru
 * Organization website:  http://rintd.ru
 *
 * --------------------- DO NOT REMOVE THIS NOTICE -----------------------------
 * BIMExt is part of jSimulationMoving.
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
 ******************************************************************************/

package json.extendetGeometry;

import json.geometry.BIM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс, расширяющий базовый {@link BIM}.
 * Предназначен для полей, которые не входят в *.json файл с геометрией
 * <p>
 * Created by boris on 17.12.16.
 */
public class BIMExt extends BIM<RoomExt, TransitionExt> {
    private static final Logger log = LoggerFactory.getLogger(BIMExt.class);

    /**
     * Безопасная зона.
     * Выделена отдельно. Необходима для работы моделирования. Является
     * вершиной графа.
     */
    private SafetyZone safetyZone;

    /**
     * @return Экземпляр зоны, которая объявлена как безопасная. Если зона уже
     * создана, то она и вернется
     */
    public SafetyZone getSafetyZone() {
        // Не пересоздаем объект при каждом вызове
        // Обесчпечивает единую безопасную зону для всего проекта
        if (safetyZone != null) return safetyZone;
        safetyZone = new SafetyZone();

        /*
        * Собираем все двери, которые соединяют какое-то помещение и null
        * (то есть улицу) в список.
        */
        final List<?> transitions = getTransitions().stream()
                .filter(TransitionExt::hasEmptyZone)
                .collect(Collectors.toList());

        // Наполняем необходимой информацией
        safetyZone.setTransitions(transitions);
        safetyZone.setNumOfExits(transitions.size());

        log.info("Successful create Safety {}", safetyZone);
        return safetyZone;
    }

}
