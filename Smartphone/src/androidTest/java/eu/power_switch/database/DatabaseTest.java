/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.database;

import eu.power_switch.ApplicationTest;
import eu.power_switch.database.handler.DatabaseHandler;

/**
 * Created by Markus on 21.08.2015.
 */
public class DatabaseTest extends ApplicationTest {

    protected void setUp() throws Exception {
        System.out.println(" Global setUp ");
        DatabaseHandler.init(getContext());
    }

//    @Test
//    public void testParallelWriteAccess() throws Exception {
//
//        Thread thread1 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for (int i = 0; i < 200; i++) {
//                    try {
//                        DatabaseHandler.addGateway(new ConnAir((long) 0, true, "Gateway", "Firmware", "localAddress[" + i + "]",
//                                49880 + i, "wanAddress[" + i + "]", 49880 + i, Collections.<String>emptySet()));
//                    } catch (Exception e) {
//                        Log4JLog.e(e);
//                    }
//                }
//            }
//        });
//
//        Thread thread2 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for (int i = 200; i < 400; i++) {
//                    try {
//                        DatabaseHandler.addGateway(new ConnAir((long) 0, true, "Gateway", "Firmware", "localAddress[" + i + "]",
//                                49880 + i, "wanAddress[" + i + "]", 49880 + i, Collections.<String>emptySet()));
//                        DatabaseHandler.getAllGateways();
//                        DatabaseHandler.getAllReceivers();
//                    } catch (Exception e) {
//                        Log4JLog.e(e);
//                    }
//
//                    try {
//                        Log4JLog.d(Arrays.toString(DatabaseHandler.getAllGateways().toArray()));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//
//        thread1.start();
//        thread2.start();
//        thread1.join();
//        thread2.join();
//    }
//
//    @Test
//    public void testPerformanceTest() throws Exception {
//        for (int i = 0; i < 200; i++) {
//            Log4JLog.d("apartment: " + i);
//            long id = DatabaseHandler.addApartment(new Apartment((long) 0, true, "Apartment[" + i + "]"));
//            for (int j = 0; j < 200; j++) {
//                long roomId = DatabaseHandler.addRoom(new Room((long) 0, id, "Room[" + j + "]", 0, false, new ArrayList<Gateway>()));
//                for (int k = 0; k < 20; k++) {
//                    DatabaseHandler.addReceiver(new CMR1000(getContext(), (long) 0, "Receiver[" + k + "]", 'A', 1, roomId, new ArrayList<Gateway>()));
//                }
//            }
//        }
//    }
}
