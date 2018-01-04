package com.example.rutherford.laserquest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements JoystickView.JoystickListener
{
    private OutputStream outputStream;
    private InputStream inStream;
    private String myDeviceName = "HC-05";
    private BluetoothAdapter blueAdapter;
    private int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        JoystickView joystick = new JoystickView(this);
        blueAdapter = BluetoothAdapter.getDefaultAdapter();
        blueAdapter.enable();

        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                attemptConnection();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onJoystickMoved(float xPercent, float yPercent, int id) {
        int x = (int) (xPercent * 128) + 128;
        int y = (int) (yPercent * 128) + 128;
        if (outputStream != null)
        {
            try
            {
                write((byte) x, (byte) y);
                btn.setEnabled(false);
            }
            catch (IOException e)
            {
                btn.setText("Connect");
                btn.setEnabled(true);
            }
        }

        switch (id)
        {
            case R.id.joystick:
                Log.d("Joystick", x + "," + y);
                break;
        }
    }

    private void attemptConnection()
    {

        if (blueAdapter.isEnabled())
        {
            Set<BluetoothDevice> bondedDevices = blueAdapter.getBondedDevices();
            if (bondedDevices.size() > 0)
            {
                int i, index = -1;
                Object[] devices = (Object[]) bondedDevices.toArray();
                for (i = 0; i < bondedDevices.size(); i++)
                {
                    BluetoothDevice device = (BluetoothDevice) devices[i];
                    String name = device.getName();
                    Log.d("device", name);
                    if (myDeviceName.equals(name))
                    {
                        index = i;
                    }
                }
                if (index != -1)
                {
                    BluetoothDevice device = (BluetoothDevice) devices[index];
                    try
                    {
                        connectDevice(device);
                        btn.setText("Connected");
                        btn.setEnabled(false);
                    }
                    catch (IOException e)
                    {
                        Log.e("Unbond", "Remove bond", e);
                        unpairDevice(device);
                    }
                }
                else
                {
                    btn.setText("Search");
                    IntentFilter filter = new IntentFilter();

                    filter.addAction(BluetoothDevice.ACTION_FOUND);
                    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

                    registerReceiver(myReceiver, filter);

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                    blueAdapter.startDiscovery();
                }
            }
        }
    }

    private void connectDevice(BluetoothDevice device) throws IOException
    {
        ParcelUuid[] uuids = device.getUuids();
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
        socket.connect();
        outputStream = socket.getOutputStream();
        inStream = socket.getInputStream();
    }

    private final BroadcastReceiver myReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                pairDevice(device);
            }
        }
    };

    private void pairDevice(BluetoothDevice device)
    {
        try
        {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
            btn.setText("Connect");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device)
    {
        try
        {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e)
        {
            Log.e("Unpair Error", "exception", e);
        }
    }

    public void write(byte x, byte y) throws IOException
    {
        byte[] towrite = {x, 32, y};
        outputStream.write(towrite);
    }
}
