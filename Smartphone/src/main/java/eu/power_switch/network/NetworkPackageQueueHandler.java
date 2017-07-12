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

import android.content.Intent;
import android.support.design.widget.Snackbar;

import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.inject.Inject;

import dagger.android.DaggerIntentService;
import eu.power_switch.R;
import eu.power_switch.gui.StatusMessageHandler;
import timber.log.Timber;

/**
 * This Class is responsible for sending NetworkPackages that are queued up to be sent
 * <p/>
 * Created by Markus on 29.10.2015.
 */
public class NetworkPackageQueueHandler extends DaggerIntentService {

    public static final String KEY_NETWORK_PACKAGES = "networkPackages";
    public static final String KEY_CALLBACK         = "callback";

    @Inject
    NetworkHandler networkHandler;

    /**
     * Socket used to send NetworkPackages over UDP
     */
    private DatagramSocket socket;

    public NetworkPackageQueueHandler() {
        super(NetworkPackageQueueHandler.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // start working
        Timber.d("start working");

        try {
            ArrayList<NetworkPackage> networkPackages = (ArrayList<NetworkPackage>) intent.getSerializableExtra(KEY_NETWORK_PACKAGES);

            int queueSize = networkPackages.size();

            if (queueSize > 0) {
                processQueue(networkPackages);
            }

        } catch (Exception e) {
            Timber.e("Illegal intent extras");
        }


        Timber.d("exiting");
    }

    private void processQueue(ArrayList<NetworkPackage> networkPackages) {
        if (networkHandler.isNetworkConnected()) {
            StatusMessageHandler.showInfoMessage(getApplicationContext(), R.string.sending, Snackbar.LENGTH_INDEFINITE);

            NetworkPackage currentNetworkPackage;
            while (networkPackages.size() > 0) {

                currentNetworkPackage = networkPackages.get(0);
                try {
                    send(currentNetworkPackage);

                    int delay = 1000; // default delay
                    // calculate time to wait before sending next package
                    if (networkPackages.size() > 1) {
                        NetworkPackage nextNetworkPackage = networkPackages.get(1);
                        if (currentNetworkPackage.getHost()
                                .equals(nextNetworkPackage.getHost()) && currentNetworkPackage.getPort() == nextNetworkPackage.getPort()) {
                            // if same gateway, wait gateway-specific time
                            Timber.d("Waiting Gateway specific time (" + currentNetworkPackage.getTimeout() + "ms) " + "before sending next signal...");
                            delay = currentNetworkPackage.getTimeout();
                        }
                    }

                    // remove NetworkPackage from queue
                    networkPackages.remove(0);

                    Timber.d("Waiting for Gateway to finish sending Signal before sending next...");
                    Thread.sleep(delay);
                } catch (UnknownHostException e) {
                    removeQueueHead(networkPackages);

                    StatusMessageHandler.showInfoMessage(getApplicationContext(), R.string.unknown_host, Snackbar.LENGTH_LONG);
                    Timber.e("UDP Sender", e);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                        Timber.e("UDP Sender", e1);
                    }
                } catch (Exception e) {
                    removeQueueHead(networkPackages);

                    StatusMessageHandler.showErrorMessage(getApplicationContext(), e);
                    Timber.e("UDP Sender: Unknown error while sending message in background:", e);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                        Timber.e("UDP Sender", e1);
                    }
                } finally {
                    if (socket != null) {
                        socket.disconnect();
                        socket.close();
                    }
                }
            }

            // queue worked off
            StatusMessageHandler.showInfoMessage(getApplicationContext(), R.string.sent, Snackbar.LENGTH_SHORT);
        } else {
            clearQueue(networkPackages);

            StatusMessageHandler.showInfoMessage(getApplicationContext(), R.string.missing_network_connection, Snackbar.LENGTH_LONG);
        }
    }

    private void removeQueueHead(ArrayList<NetworkPackage> networkPackages) {
        // remove NetworkPackage from queue
        networkPackages.remove(0);
    }

    private void clearQueue(ArrayList<NetworkPackage> networkPackages) {
        // remove NetworkPackage from queue
        networkPackages.clear();
    }

    private void send(NetworkPackage networkPackage) throws Exception {
        if (networkPackage instanceof UdpNetworkPackage) {

            InetAddress host = InetAddress.getByName(networkPackage.getHost());
            int         port = networkPackage.getPort();

            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.connect(host, port);

            byte[] messageBuffer = networkPackage.getMessage()
                    .getBytes();
            DatagramPacket messagePacket = new DatagramPacket(messageBuffer, messageBuffer.length, host, port);
            socket.send(messagePacket);

            Timber.d("UDP Sender", "Host: " + host.getHostAddress() + ":" + port + " Message: \"" + new String(messageBuffer) + "\" sent.");

            socket.disconnect();
            socket.close();
//        } else networkPackage instanceof HttpNetworkPackage) {
//                URL url = new URL("http://" + networkPackage.getHost() + ":" + networkPackage.getPort() + "/" +
//                        networkPackage.getMessage());
//                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//                try {
//                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
//                    readStream(in, networkPackageTupel.getRight());
//                } finally {
//                    urlConnection.disconnect();
//                }
        }
    }

    private void readStream(InputStream inputStream, NetworkResponseCallback responseCallback) {
        String response;

        java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
        if (s.hasNext()) {
            response = s.next();
            Timber.d("HTTP Response", response);
            if (responseCallback != null) {
                responseCallback.receiveResponse("key", response);
            }
        } else {
            Timber.d("Scanner is empty");
            if (responseCallback != null) {
                responseCallback.receiveResponse("key", null);
            }
        }
    }

}
