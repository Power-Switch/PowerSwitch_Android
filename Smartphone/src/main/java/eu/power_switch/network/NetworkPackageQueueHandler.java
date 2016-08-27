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
import android.support.design.widget.Snackbar;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import eu.power_switch.R;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.shared.Tupel;
import eu.power_switch.shared.log.Log;

/**
 * This Class is responsible for sending NetworkPackages that are queued up to be sent
 * <p/>
 * Created by Markus on 29.10.2015.
 */
public class NetworkPackageQueueHandler extends AsyncTask<Void, Void, Void> {

    /**
     * Lock Object used to lock thread on empty queue and wakeup again on notify
     */
    public static final Object lock = new Object();

    /**
     * Context
     */
    private Context context;

    /**
     * Socket used to send NetworkPackages over UDP
     */
    private DatagramSocket socket;

    public NetworkPackageQueueHandler(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        while (true) {
            // start working

            Log.d(this, "start working");

            int queueSize;
            synchronized (NetworkHandler.networkPackagesQueue) {
                queueSize = NetworkHandler.networkPackagesQueue.size();
            }

            if (queueSize > 0) {
                processQueue();
            }

            // queue is empty
            Log.d(this, "queue is empty, wait for notify...");

            // Put Thread asleep and wait for wakeup from NetworkHandler
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
                Log.d(this, "waking up");
            }
        }
    }

    private void processQueue() {
        if (NetworkHandler.isNetworkConnected()) {
            StatusMessageHandler.showInfoMessage(context, R.string.sending, Snackbar.LENGTH_INDEFINITE);

            Tupel<NetworkPackage, NetworkResponseCallback> currentNetworkPackageTupel;
            while (NetworkHandler.networkPackagesQueue.size() > 0) {

                synchronized (NetworkHandler.networkPackagesQueue) {
                    currentNetworkPackageTupel = NetworkHandler.networkPackagesQueue.get(0);
                }
                try {
                    send(currentNetworkPackageTupel);

                    int delay = 1000; // default delay
                    synchronized (NetworkHandler.networkPackagesQueue) {
                        // calculate time to wait before sending next package
                        if (NetworkHandler.networkPackagesQueue.size() > 1) {
                            Tupel<NetworkPackage, NetworkResponseCallback> nextNetworkPackageTupel = NetworkHandler.networkPackagesQueue.get(1);
                            if (currentNetworkPackageTupel.getLeft().getHost().equals(nextNetworkPackageTupel.getLeft().getHost()) &&
                                    currentNetworkPackageTupel.getLeft().getPort() == nextNetworkPackageTupel.getLeft().getPort()) {
                                // if same gateway, wait gateway-specific time
                                Log.d("Waiting Gateway specific time (" + currentNetworkPackageTupel.getLeft().getTimeout() + "ms) " +
                                        "before sending next signal...");
                                delay = currentNetworkPackageTupel.getLeft().getTimeout();
                            }
                        }

                        // remove NetworkPackage from queue
                        NetworkHandler.networkPackagesQueue.remove(0);
                    }

                    Log.d("Waiting for Gateway to finish sending Signal before sending next...");
                    Thread.sleep(delay);
                } catch (UnknownHostException e) {
                    removeQueueHead();

                    StatusMessageHandler.showInfoMessage(context, R.string.unknown_host, Snackbar.LENGTH_LONG);
                    Log.e("UDP Sender", e);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                        Log.e("UDP Sender", e1);
                    }
                } catch (Exception e) {
                    removeQueueHead();

                    StatusMessageHandler.showErrorMessage(context, e);
                    Log.e("UDP Sender: Unknown error while sending message in background:", e);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                        Log.e("UDP Sender", e1);
                    }
                } finally {
                    if (socket != null) {
                        socket.disconnect();
                        socket.close();
                    }
                }
            }

            // queue worked off
            StatusMessageHandler.showInfoMessage(context, R.string.sent, Snackbar.LENGTH_SHORT);
        } else {
            clearQueue();

            StatusMessageHandler.showInfoMessage(context, R.string.missing_network_connection, Snackbar.LENGTH_LONG);
        }
    }

    private void removeQueueHead() {
        synchronized (NetworkHandler.networkPackagesQueue) {
            // remove NetworkPackage from queue
            NetworkHandler.networkPackagesQueue.remove(0);
        }
    }

    private void clearQueue() {
        synchronized (NetworkHandler.networkPackagesQueue) {
            // remove NetworkPackage from queue
            NetworkHandler.networkPackagesQueue.clear();
        }
    }

    private void send(Tupel<NetworkPackage, NetworkResponseCallback> networkPackageTupel) throws Exception {
        NetworkPackage networkPackage = networkPackageTupel.getLeft();
        switch (networkPackage.getCommunicationType()) {
            case UDP:
                InetAddress host = InetAddress.getByName(networkPackage.getHost());
                int port = networkPackage.getPort();

                socket = new DatagramSocket(null);
                socket.setReuseAddress(true);
                socket.connect(host, port);

                byte[] messageBuffer = networkPackage.getMessage().getBytes();
                DatagramPacket messagePacket = new DatagramPacket(messageBuffer, messageBuffer.length, host, port);
                socket.send(messagePacket);

                Log.d("UDP Sender", "Host: " + host.getHostAddress() + ":" + port
                        + " Message: \"" + new String(messageBuffer) + "\" sent.");

                socket.disconnect();
                socket.close();
                break;
            case HTTP:
                URL url = new URL("http://" + networkPackage.getHost() + ":" + networkPackage.getPort() + "/" +
                        networkPackage.getMessage());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    readStream(in, networkPackageTupel.getRight());
                } finally {
                    urlConnection.disconnect();
                }
                break;
        }
    }

    private void readStream(InputStream inputStream, NetworkResponseCallback responseCallback) {
        String response;

        java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
        if (s.hasNext()) {
            response = s.next();
            Log.d("HTTP Response", response);
            if (responseCallback != null) {
                responseCallback.receiveResponse("key", response);
            }
        } else {
            Log.d("Scanner is empty");
            if (responseCallback != null) {
                responseCallback.receiveResponse("key", null);
            }
        }
    }
}
