/*
 * Copyright (C) 2017 Chirkov Boris <b.v.chirkov@udsu.ru>
 *
 * Project website:       http://eesystem.ru
 * Organization website:  http://rintd.ru
 *
 * --------------------- DO NOT REMOVE THIS NOTICE -----------------------------
 * Eventable is part of jSimulationMoving.
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

/**
 * Интерфейс работы с событиями
 * <p>
 * Created by boris on 16.01.17.
 */
public interface Eventable {
    /**
     * Регистрирует класс (вешает слушателя на шину), в котором есть
     * методы-обработчики событий
     *
     * @param object объект (класса)
     */
    default void register(Object object) {
        EBus.getInstance().register(object);
    }

    /**
     * Снимает с регистрации класс с методами-обработчиками событий
     *
     * @param object объект (класса)
     */
    default void unregister(Object object) {
        EBus.getInstance().unregister(object);
    }

    /**
     * Инициализация события. Сообщение о событии на шину
     *
     * @param event сообщение
     */
    default void post(Object event) {
        EBus.getInstance().post(event);
    }
}
