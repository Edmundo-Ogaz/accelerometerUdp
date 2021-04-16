package com.example.accelerometerudp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class MainActivity extends Activity implements SensorEventListener {

    private TextView xText, yText, zText, direction;

    private Sensor mySensor;
    private SensorManager SM;

    private String action = "";

    private String ip = "192.168.4.1";
    private int port = 8888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create our Sensor Manager
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);

        // Accelerometer Sensor
        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Register sensor Listener
        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Assign TextView
        xText = (TextView)findViewById(R.id.xText);
        yText = (TextView)findViewById(R.id.yText);
        zText = (TextView)findViewById(R.id.zText);
        direction = (TextView)findViewById(R.id.direction);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not in use
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];

        xText.setText("X: " + x);
        yText.setText("Y: " + y);
        zText.setText("Z: " + z);

        Log.i("onSensorChanged","x: " + x + " y: " + y + " z: " + z + " action: " + action);

        if ( x < -1.5 )
        {
            int speed = (int)(400 - ( 120 * x ));
            String command = "R-" + speed;
            new Thread(new ClientSend(command)).start();
            Log.i("onSensorChanged", "goRigth " + command);
            action = "goRigth " + command;
        }
        else if ( x > 1.5 )
        {
            int speed = (int)(400 + ( 120 * x ));
            String command = "L-" + speed;
            new Thread(new ClientSend(command)).start();
            Log.i("onSensorChanged", "goLeft " + command);
            action = "goLeft " + command;
        }
        else if ( y < 6.5 && y > 1.0 )
        {
            int speed = (int)(1023 - ( 100 * y ));
            String command = "F-" + speed;
            new Thread(new ClientSend(command)).start();
            Log.i("onSensorChanged", "goAhead " + command);
            action = "goAhead " + command;
        }
        else if ( y > 8.5 )
        {
            int speed = (int)(400 + ( 60 * ((y - 8.5) * 10)));
            String command = "B-" + speed;
            new Thread(new ClientSend(command)).start();
            Log.i("onSensorChanged","goBack " + command);
            action = "goBack " + command;
        }
        else if ( action != "goStop" )
        {
            action = "goStop";
            new Thread(new ClientSend("S")).start();
            Log.i("onSensorChanged","goStop");
        }

        direction.setText("direction: " + action);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class ClientSend implements Runnable {

        private String command;

        ClientSend(String command) {
            this.command = command;
        }
        @Override
        public void run() {
            try {
                DatagramSocket udpSocket = new DatagramSocket();
                InetAddress serverAddr = InetAddress.getByName(ip);
                byte[] buf = (command).getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length,serverAddr, port);
                udpSocket.send(packet);
            } catch (SocketException e) {
                Log.e("Udp:", "Socket Error:", e);
            } catch (IOException e) {
                Log.e("Udp Send:", "IO Error:", e);
            }
        }
    }
}