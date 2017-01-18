/*******************************************************************************
 * Copyright (C) 2016 Chirkov Boris <b.v.chirkov@udsu.ru>
 *
 * Project website:       http://eesystem.ru
 * Organization website:  http://rintd.ru
 *
 * --------------------- DO NOT REMOVE THIS NOTICE -----------------------------
 * EBus is part of jSimulationMoving.
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

package bus;

import com.google.common.eventbus.EventBus;
import org.slf4j.LoggerFactory;

/**
 * Шина событиый {@link EventBus}
 * <p>
 * Initialization-on-demand holder idiom
 * <p>
 * Created by boris on 15.12.16.
 */
public class EBus {

    /**
     * @return Экземпляр шины {@link EventBus}
     */
    static EventBus getInstance() {
        return EBusHelper.BUS_INSTANCE;
    }

    private static class EBusHelper {
        private static final EventBus BUS_INSTANCE;

        static {
            BUS_INSTANCE = new EventBus(EBusHelper.class.getName());
            LoggerFactory.getLogger(EBusHelper.class).info("Create instance bus events");
        }
    }

}
