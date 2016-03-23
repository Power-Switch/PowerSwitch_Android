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

package eu.power_switch.network;

import android.content.Context;
import android.os.AsyncTask;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.LinkedList;

import eu.power_switch.shared.log.Log;

/**
 * Background thread to search through local network for Gateways
 */
public class AutoGatewayDiscover extends AsyncTask<Void, Void, LinkedList<String>> {

    Context context;

    public AutoGatewayDiscover(Context context) {
        this.context = context;
    }

    @Override
    protected LinkedList<String> doInBackground(Void... params) {
        return doDiscovery();
    }

    public LinkedList<String> doDiscovery() {
        DatagramSocket socket = null;
        LinkedList<String> receivedMessages = new LinkedList<>();

        try {
            InetAddress ip = InetAddress.getByName("255.255.255.255");
            int port = 49880;
            // create new UDP Socket
            socket = new DatagramSocket(port);
            // make broadcast
            socket.setBroadcast(true);
            // prepare Data to send
            byte[] buffer = "SEARCH HCGW".getBytes();

            // create UDP Packet with data & destination(url+localPort)
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, port);

            // send packet
            socket.send(packet);
            socket.disconnect();
            socket.close();

            // Create new UDP-Socket
            socket = new DatagramSocket(port);
            /* By magic we know, how much data will be waiting for us */
            buffer = new byte[128];
            // Prepare a UDP-Packet that can contain the data we want to
            // receive
            packet = new DatagramPacket(buffer, buffer.length);

            // Set time to wait for input to 500 milliseconds
            socket.setSoTimeout(2000);
            String message;
            // SocketTimeoutException will stop this loop
            while (true) {
                // Receive the UDP-Packet
                socket.receive(packet);
                message = new String(buffer, 0, packet.getLength());

                String hostAddress = packet.getAddress().getHostAddress();

                try {
                    // Das ITGW schickt eine falsche IP mit, daher tausche ich sie aus, wenn sie nicht der HostIP
                    // entspricht

                    // read IP address version
                    int start = message.indexOf("IP:") + 3;
                    int end = message.indexOf(";;");
                    String IP = message.substring(start, end);
                    if (!IP.equals(hostAddress)) {
                        message = message.replace(IP, hostAddress);
                    }

                } catch (Exception e) {
                    Log.e("malformed string, couldnt check IP", e);
                }

                receivedMessages.add(message);
                // Reset the length of the packet before reusing it.
                packet.setLength(buffer.length);
            }

        } catch (SocketTimeoutException e) {
            Log.d(this, "AutoDetect Timeout Reached");
        } catch (Exception e) {
            Log.e("AutoGatewayDiscover", e);
        } finally {

            for (String message : receivedMessages) {
                Log.d("AutoGatewayDiscover", "Received: " + message);
            }

            if (socket != null) {
                socket.disconnect();
                socket.close();
            }
        }

        return receivedMessages;
    }
}
