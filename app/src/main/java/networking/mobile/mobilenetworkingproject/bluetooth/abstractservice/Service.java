package networking.mobile.mobilenetworkingproject.bluetooth.abstractservice;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

/**
 * Created by Bhavnesh Gugnani on 4/16/2015.
 */
public interface Service {
    public void start();

    public void connect(BluetoothDevice device, byte[] out);

    public void connected(BluetoothSocket socket, BluetoothDevice device, byte[] out);

    public void stop();

    public void write(BluetoothDevice device, byte[] out) throws IOException;

    public void syncDataToNetwork(byte[] text);
}
