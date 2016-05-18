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

package eu.power_switch.obj.device.intertechno;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.obj.ReceiverTest;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.device.intertechno.CMR300;

/**
 * Created by Markus on 08.08.2015.
 */
public class CMR300_Test extends ReceiverTest {

    private CMR300 receiver;

    @Test
    public void testCodeGenerationA1() throws Exception {
        receiver = new CMR300(getContext(), (long) 0, "Name", 'A', 1, (long) 0, new ArrayList<Gateway>());

        Method method = receiver.getClass().getDeclaredMethod("getSignal", argClassesGetSignal);
        method.setAccessible(true);

        Object[] argObjects = new Object[]{connAir, getContext().getString(R.string.on)};
        String generatedMessage = (String) method.invoke(receiver, argObjects);

        // ON
        String expectedMessage = "TXP:0,0,6,11125,89,25,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,12,4,4,12,12,4,4,12,12,4,1,140;";

        Assert.assertEquals(expectedMessage, generatedMessage);

        argObjects = new Object[]{connAir, getContext().getString(R.string.off)};
        generatedMessage = (String) method.invoke(receiver, argObjects);

        // OFF
        expectedMessage = "TXP:0,0,6,11125,89,25,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,12,4,4,12,12,4,4,12,4,12,1,140;";
        Assert.assertEquals(expectedMessage, generatedMessage);
    }

    @Test
    public void testCodeGenerationA5() throws Exception {
        receiver = new CMR300(getContext(), (long) 0, "Name", 'A', 5, (long) 0, new ArrayList<Gateway>());

        String methodName = "getSignal";
        Method method = receiver.getClass().getDeclaredMethod(methodName, argClassesGetSignal);
        method.setAccessible(true);
        Object[] argObjects = new Object[]{connAir, getContext().getString(R.string.on)};
        String generatedMessage = (String) method.invoke(receiver, argObjects);

        // ON
        String expectedMessage = "TXP:0,0,6,11125,89,25,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,12,4,4,12,4,12,4,12,4,12,4,12,12,4,4,12,12,4,4,12,12,4,1,140;";
        Assert.assertEquals(expectedMessage, generatedMessage);

        argObjects = new Object[]{connAir, getContext().getString(R.string.off)};
        generatedMessage = (String) method.invoke(receiver, argObjects);

        // OFF
        expectedMessage = "TXP:0,0,6,11125,89,25,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,12,4,4,12,4,12,4,12,4,12,4,12,12,4,4,12,12,4,4,12,4,12,1,140;";
        Assert.assertEquals(expectedMessage, generatedMessage);
    }

    @Test
    public void testCodeGenerationE1() throws Exception {
        receiver = new CMR300(getContext(), (long) 0, "Name", 'E', 1, (long) 0, new ArrayList<Gateway>());

        String methodName = "getSignal";
        Method method = receiver.getClass().getDeclaredMethod(methodName, argClassesGetSignal);
        method.setAccessible(true);
        Object[] argObjects = new Object[]{connAir, getContext().getString(R.string.on)};
        String generatedMessage = (String) method.invoke(receiver, argObjects);

        // ON
        String expectedMessage = "TXP:0,0,6,11125,89,25,4,12,4,12,4,12,4,12,4,12,12,4,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,12,4,4,12,12,4,4,12,12,4,1,140;";
        Assert.assertEquals(expectedMessage, generatedMessage);

        argObjects = new Object[]{connAir, getContext().getString(R.string.off)};
        generatedMessage = (String) method.invoke(receiver, argObjects);

        // OFF
        expectedMessage = "TXP:0,0,6,11125,89,25,4,12,4,12,4,12,4,12,4,12,12,4,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,4,12,12,4,4,12,12,4,4,12,4,12,1,140;";
        Assert.assertEquals(expectedMessage, generatedMessage);
    }

    @Test
    public void testCodeGenerationP16() throws Exception {
        receiver = new CMR300(getContext(), (long) 0, "Name", 'P', 16, (long) 0, new ArrayList<Gateway>());

        String methodName = "getSignal";
        Method method = receiver.getClass().getDeclaredMethod(methodName, argClassesGetSignal);
        method.setAccessible(true);
        Object[] argObjects = new Object[]{connAir, getContext().getString(R.string.on)};
        String generatedMessage = (String) method.invoke(receiver, argObjects);

        // ON
        String expectedMessage = "TXP:0,0,6,11125,89,25,4,12,12,4,4,12,12,4,4,12,12,4,4,12,12,4,4,12,12,4,4,12,12,4,4,12,12,4,4,12,12,4,4,12,4,12,4,12,12,4,4,12,12,4,4,12,12,4,1,140;";
        Assert.assertEquals(expectedMessage, generatedMessage);

        argObjects = new Object[]{connAir, getContext().getString(R.string.off)};
        generatedMessage = (String) method.invoke(receiver, argObjects);

        // OFF
        expectedMessage = "TXP:0,0,6,11125,89,25,4,12,12,4,4,12,12,4,4,12,12,4,4,12,12,4,4,12,12,4,4,12,12,4,4,12,12,4,4,12,12,4,4,12,4,12,4,12,12,4,4,12,12,4,4,12,4,12,1,140;";
        Assert.assertEquals(expectedMessage, generatedMessage);
    }

}
