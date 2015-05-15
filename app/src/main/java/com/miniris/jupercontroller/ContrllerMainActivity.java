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
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class ContrllerMainActivity extends ActionBarActivity {

    private BluetoothService mBluetoothService;
    private int[] currentMotorState = {0,0,0,0};
    private int[] currentSensorState = {0,0,0};

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
    //    send_message("STR");
    }

    public void send_end() {
        send_message("00000");
    }

    public void send_motor(int[] currentMotorState) {
        send_message(String.format("MOR%02d%02d%02d%02d", currentMotorState[0], currentMotorState[1],
                currentMotorState[2], currentMotorState[3]));
    }

    public void send_command(String command, String swc, int currentMotorValue) {
        send_message(String.format("%s%s%02d", command, swc, currentMotorValue));
    }

    public void controlButtonInit() {
        View.OnTouchListener tl = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int motorNums = Integer.parseInt(v.getTag().toString());
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if ((motorNums & 8) == 8) { currentMotorState[0] += Common.MOTOR_VALUE_OFFSET; }
                        if ((motorNums & 4) == 4) { currentMotorState[1] += Common.MOTOR_VALUE_OFFSET; }
                        if ((motorNums & 2) == 2) { currentMotorState[2] += Common.MOTOR_VALUE_OFFSET; }
                        if ((motorNums & 1) == 1) { currentMotorState[3] += Common.MOTOR_VALUE_OFFSET; }
                        send_motor(currentMotorState);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if ((motorNums & 8) == 8) { currentMotorState[0] -= Common.MOTOR_VALUE_OFFSET; }
                        if ((motorNums & 4) == 4) { currentMotorState[1] -= Common.MOTOR_VALUE_OFFSET; }
                        if ((motorNums & 2) == 2) { currentMotorState[2] -= Common.MOTOR_VALUE_OFFSET; }
                        if ((motorNums & 1) == 1) { currentMotorState[3] -= Common.MOTOR_VALUE_OFFSET; }
                        send_motor(currentMotorState);
                        return true;
                }
                return false;
            }
        };
        ImageButton moveForwardButton = (ImageButton) findViewById(R.id.moveForward_button);
        moveForwardButton.setTag(3); // 3,4
        moveForwardButton.setOnTouchListener(tl);
        ImageButton moveBackwardButton = (ImageButton) findViewById(R.id.moveBackward_button);
        moveBackwardButton.setTag(12); // 1,2
        moveBackwardButton.setOnTouchListener(tl);
        ImageButton moveLeftButton = (ImageButton) findViewById(R.id.moveLeft_button);
        moveLeftButton.setTag(5); // 2,4
        moveLeftButton.setOnTouchListener(tl);
        ImageButton moveRightButton = (ImageButton) findViewById(R.id.moveRight_button);
        moveRightButton.setTag(10); // 1,3
        moveRightButton.setOnTouchListener(tl);
        ImageButton turnCWButton = (ImageButton) findViewById(R.id.turnCW_button);
        turnCWButton.setTag(9); // 1,4
        turnCWButton.setOnTouchListener(tl);
        ImageButton turnCCWButton = (ImageButton) findViewById(R.id.turnCCW_button);
        turnCCWButton.setTag(6); // 2,3
        turnCCWButton.setOnTouchListener(tl);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contrller_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        controlButtonInit();
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
            send_start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void displayMotorState(){
        //currentMotorState = motorState;
        TextView m1 = (TextView) findViewById(R.id.M1_state);
        m1.setText(Float.toString(currentMotorState[0] / 10));
        TextView m2 = (TextView) findViewById(R.id.M2_state);
        m2.setText(Float.toString(currentMotorState[1] / 10));
        TextView m3 = (TextView) findViewById(R.id.M3_state);
        m3.setText(Float.toString(currentMotorState[2] / 10));
        TextView m4 = (TextView) findViewById(R.id.M4_state);
        m4.setText(Float.toString(currentMotorState[3] / 10));
    }

    public void displaySnesorState(){
        TextView s1 = (TextView) findViewById(R.id.S1_state);
        s1.setText(Float.toString(currentSensorState[0] / 10));
        TextView s2 = (TextView) findViewById(R.id.S2_state);
        s2.setText(Float.toString(currentSensorState[1] / 10));
        TextView s3 = (TextView) findViewById(R.id.S3_state);
        s3.setText(Float.toString(currentSensorState[2] / 10));
    }

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
        displayMotorState();
    }

    //###########################################################
    //# Send Protocol
    //#
    //# 00|0|00 : End Communication
    //# 01|0|XX : Move Up - XX set value
    //# 02|0|XX : Move Down - XX set value
    //# 03|S|XX : Move Left - XX set angle, S On(1)/Off(0)
    //# 04|S|XX : Move Right - XX set angle, S On(1)/Off(0)
    //# 05|S|XX : Turn CW - XX set value, S On(1)/Off(0)
    //# 06|S|XX : Turn CCW - XX set value, S On(1)/Off(0)
    //#
    //# Receive Protocol
    //# 20|X|{ AA, BB, CC, DD} : Motor Status - X motor count, AA BB CC DD Value
    //# 21|X|{ SAA, SBB, SCC}  : Sensor Status - X Sensor count, AA BB CC Value, S sign (0:+, 1:-)
    //#
    //###########################################################

    final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Common.MESSAGE_READ:
                    switch (msg.arg1) {
                        case Common.COMMAND_MOTOR_STATE:
                            currentMotorState = (int[]) msg.obj;
                            displayMotorState();
                            break;
                        case Common.COMMAND_SENSOR_STATE:
                            displayToast((String)msg.obj);
                            currentSensorState = (int[]) msg.obj;
                            displaySnesorState();
                            break;
                        default:
                            displayToast("Unknown command : " + msg.arg1);
                            break;
                    }
                    break;
                case Common.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    //displayToast("=> " + writeMessage);
                    break;
            }
        }
    };

    public void endController(View view){
        send_end();
        mBluetoothService.stop();
    }

    public void goingUp(View view){
        send_command("01", "0", 1);
    }

    public void goingDown(View view){
        send_command("02", "0", 1);
    }
}
