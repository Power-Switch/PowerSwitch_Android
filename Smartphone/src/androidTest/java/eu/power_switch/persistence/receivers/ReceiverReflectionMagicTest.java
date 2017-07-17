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

package eu.power_switch.persistence.receivers;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;

import eu.power_switch.obj.receiver.DipReceiver;
import eu.power_switch.obj.receiver.MasterSlaveReceiver;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.obj.receiver.UniversalReceiver;
import eu.power_switch.persistence.DatabaseTest;
import eu.power_switch.persistence.data.sqlite.handler.ReceiverReflectionMagic;

public class ReceiverReflectionMagicTest extends DatabaseTest {

    ReceiverReflectionMagic receiverReflectionMagic;

    @Before
    public void before() {
        receiverReflectionMagic = new ReceiverReflectionMagic();
        receiverReflectionMagic.setContext(getContext());
    }

    @Test
    public void testFromDatabase() throws Exception {
        fail("Test not implemented.");
    }

    @Test
    public void testGetConstructor() throws Exception {
        fail("Test not implemented.");
    }

    @Test
    public void testGetTypeMasterSlave() throws Exception {
        Receiver.Type type = receiverReflectionMagic.getType("eu.power_switch.obj.receiver.device.intertechno.CMR1000");
        assertEquals(Receiver.Type.MASTER_SLAVE, type);
    }

    @Test
    public void testGetTypeDips() throws Exception {
        Receiver.Type type = receiverReflectionMagic.getType("eu.power_switch.obj.receiver.device.brennenstuhl.RCS1000NComfort");
        assertEquals(Receiver.Type.DIPS, type);
    }

    @Test
    public void testGetTypeAutoPair() throws Exception {
        Receiver.Type type = receiverReflectionMagic.getType("eu.power_switch.obj.receiver.device.brennenstuhl.RC3600");
        assertEquals(Receiver.Type.AUTOPAIR, type);
    }

    @Test
    public void testGetTypeUniversal() throws Exception {
        Receiver.Type type = receiverReflectionMagic.getType("eu.power_switch.obj.receiver.UniversalReceiver");
        assertEquals(Receiver.Type.UNIVERSAL, type);
    }

    @Test
    public void testGetDummyMasterSlave() throws Exception {
        Receiver dummy = receiverReflectionMagic.getDummy("eu.power_switch.obj.receiver.device.intertechno" +
                ".CMR1000");
        MasterSlaveReceiver dummyMasterSlave = (MasterSlaveReceiver) dummy;

        assertEquals((long) 0, (long) dummy.getId());
        assertEquals("dummy", dummy.getName());
        assertEquals('A', dummyMasterSlave.getMaster());
        assertEquals(0, dummyMasterSlave.getSlave());
        assertEquals(null, dummy.getRoomId());
    }

    @Test
    public void testGetDummyDips() throws Exception {
        Receiver    dummy    = receiverReflectionMagic.getDummy("eu.power_switch.obj.receiver.device.brennenstuhl.RCS1000NComfort");
        DipReceiver dummyDip = (DipReceiver) dummy;

        assertEquals((long) 0, (long) dummy.getId());
        assertEquals("dummy", dummy.getName());
        assertEquals(LinkedList.class, dummyDip.getDips().getClass());
        assertEquals(null, dummy.getRoomId());
    }

    @Test
    public void testGetDummyAutoPair() throws Exception {
        Receiver dummy = receiverReflectionMagic.getDummy("eu.power_switch.obj.receiver.device.brennenstuhl.RC3600");
        assertEquals((long) 0, (long) dummy.getId());
        assertEquals("dummy", dummy.getName());
        assertEquals(null, dummy.getRoomId());
    }

    @Test
    public void testGetDummyUniversal() throws Exception {
        Receiver          dummy          = receiverReflectionMagic.getDummy("eu.power_switch.obj.receiver.UniversalReceiver");
        UniversalReceiver dummyUniversal = (UniversalReceiver) dummy;
        assertEquals((long) 0, (long) dummy.getId());
        assertEquals("dummy", dummy.getName());
        assertEquals(LinkedList.class, dummyUniversal.getButtons().getClass());
        assertEquals(null, dummy.getRoomId());
    }
}