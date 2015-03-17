package com.miniris.jupercontroller;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class ContrllerMainActivity extends ActionBarActivity {

    private BluetoothService mBluetoothService;
    private byte[] currentMotorState = {0,0,0,0};

    public void displayToast(int stringId) {
        Toast mToast = Toast.makeText(this,stringId, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public void displayToast(String msg) {
        Toast mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public void send_message(String message) {
        byte[] buff = message.getBytes();
        mBluetoothService.write(buff);
    }

    public void send_start() {
        send_message("STR");
    }

    public void send_end() {
        send_message("END");
    }

    public void send_motor(byte[] currentMotorState) {
        send_message(String.format("MOR%02d%02d%02d%02d", currentMotorState[0], currentMotorState[1],
                currentMotorState[2], currentMotorState[3]));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contrller_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contrller_main, menu);
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

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case Common.REQUEST_BLUETOOTH_ENABLE:
                if (resultCode == RESULT_CANCELED){
                    displayToast(R.string.disable_bluetooth);
                    finish();
                } else {
                    startBluetoothServer();
                }
        }
    }

    public void startBluetoothServer() {
        try {
            mBluetoothService = new BluetoothService(mHandler);
            //send_start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void displayMotorState(byte[] motorState)
    {
        currentMotorState = motorState;
        TextView m1 = (TextView) findViewById(R.id.M1_state);
        m1.setText(currentMotorState[0]);
        TextView m2 = (TextView) findViewById(R.id.M2_state);
        m2.setText(currentMotorState[1]);
        TextView m3 = (TextView) findViewById(R.id.M3_state);
        m3.setText(currentMotorState[2]);
        TextView m4 = (TextView) findViewById(R.id.M4_state);
        m4.setText(currentMotorState[3]);
    }

    final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Common.MESSAGE_READ:
                    switch (msg.arg1) {
                        case Common.COMMAND_STATE:
                            byte[] motorState = (byte[]) msg.obj;
                            displayMotorState(motorState);
                        default:
                            displayToast("Unknown command : " + msg.arg1);
                            break;
                    }
                    break;
                case Common.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    displayToast("=> " + writeMessage);
                    break;
            }
        }
    };

    public void startController(View view){
        // Set up bluetooth
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            displayToast(R.string.unsupport_bluetooth);
            finish();
        }else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, Common.REQUEST_BLUETOOTH_ENABLE);
        } else {
            startBluetoothServer();
        }
    }

    public void endController(View view){
        send_end();
        mBluetoothService.stop();
    }

    public void goingUp(View view){
        for(int i=0; i < currentMotorState.length; i++){
            if (currentMotorState[i] < Common.MAX_MOTOR_VALUE)
            {
                currentMotorState[i]++;
            }
        }
        send_motor(currentMotorState);
    }

    public void goingDown(View view){
        for(int i=0; i < currentMotorState.length; i++){
            if (currentMotorState[i] > Common.MIN_MOTOR_VALUE)
            {
                currentMotorState[i]--;
            }
        }
        send_motor(currentMotorState);
    }

    public void moveForward(View view){
        currentMotorState[2] += 5;
        currentMotorState[3] += 5;
    }
}