/******************************************************************************
 Copyright (C) 2016 Galiullin Marat

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

/**
 * Класс используется для десериализации стандарта BECS.json
 *
 * @author mag
 */
public class BuildingPIM {

    /**
     * Общее название здания
     */
    private String name;

    /**
     * Адресные данные
     */
    private Address address;

    /**
     * Массив помещений в здании
     */
    private Room[] rooms;

    /**
     * Массив переходов/дверей/проемов в здании
     */
    private Transition[] transitions;

    // --------------------------------------------------

    public void jsonToString() {
        System.out.println("  Идентификатор здания		 		" + name);
        for (Room room : rooms)
            System.out.println(" rooms[kk].name    " + room.getName());
    }

    @Override
    public String toString() {
        return ("Name:\t" + name + "\n") +
                address + "\n" +
                "Number of rooms:\t" + rooms.length + "\n" +
                "Number of transitions:\t" + transitions.length + "\n";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Room[] getRooms() {
        return rooms;
    }

    public void setRooms(Room[] rooms) {
        this.rooms = rooms;
    }

    public Room getRooms(int i) {
        return rooms[i];
    }

    public Transition[] getTransitions() {
        return transitions;
    }

    public void setTransitions(Transition[] transitions) {
        this.transitions = transitions;
    }

    public Transition getTransitions(int i) {
        return transitions[i];
    }

    /**
     * Внутренний класс для указания адреса - Address.
     */
    private class Address {

        /**
         * Улица, дом.
         */
        private String street;
        /**
         * Город.
         */
        private String city;
        /**
         * Дополнительная информация.
         */
        private String addInfo;

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getAddInfo() {
            return addInfo;
        }

        public void setAddInfo(String addInfo) {
            this.addInfo = addInfo;
        }

        @Override
        public String toString() {
            return "Street:\t" + street + "\n" + "City:\t" + city + "\n" + "AddInfo:\t" + addInfo;
        }
    }
}