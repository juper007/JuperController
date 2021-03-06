package com.miniris.jupercontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by v-mipark on 2/9/2015.
 */
public class BluetoothService {
    private BluetoothServerSocket mServerSocket;
    private BluetoothAdapter mBluetoothAdapter;
    private AcceptSocketThread mAcceptSocket;
    private CommunicationThread mCommunicationThread;
    private Handler mHandler;

    public BluetoothService(Handler handler) throws IOException {
        mHandler = handler;
        mAcceptSocket = new AcceptSocketThread();
        mAcceptSocket.start();
    }

    public void manageConnectedSocket(BluetoothSocket socket) {
        mCommunicationThread = new CommunicationThread(socket);
        mCommunicationThread.start();
    }

    public void write(byte[] buff) {
        mCommunicationThread.write(buff);
    }

    public void stop() {
        if (mCommunicationThread != null) {
            mCommunicationThread.cancel();
            mCommunicationThread = null;
        }

        if (mAcceptSocket != null) {
            mAcceptSocket.cancel();
            mAcceptSocket = null;
        }
    }

    private class AcceptSocketThread extends Thread {
        public AcceptSocketThread() {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothServerSocket tmp = null;

            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(Common.CONTROLLER_NAME, Common.MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mServerSocket = tmp;
        }

        public void run(){
            BluetoothSocket socket;
            while (true) {
                try {
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

                if (socket != null) {
                    try {
                        manageConnectedSocket(socket);
                        mServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void cancel() {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class CommunicationThread extends Thread {
        private OutputStream outStream;
        private InputStream inStream;
        private BluetoothSocket mmSocket;

        public CommunicationThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            mmSocket = socket;

            try {
                tmpOut = socket.getOutputStream();
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            outStream = tmpOut;
            inStream = tmpIn;
        }

        public void run() {
            byte[] cmd = new byte[2];

            while (true) {
                try {
                    inStream.read(cmd);
                    int cmdCode = Integer.parseInt(new String(cmd));
                    int count = 0;
                    byte[] sizeBuff;
                    byte[] countBuff = new byte[1];
                    switch (cmdCode) {
                        case Common.COMMAND_IMG:
                            sizeBuff = new byte[10];
                            inStream.read(sizeBuff);
                            int size = Integer.parseInt(new String(sizeBuff));
                            byte[] buff = new byte[size];
                            byte[] tmpBuff = new byte[1024];
                            int pos = 0;

                            while (pos < size) {
                                int readSize = inStream.read(tmpBuff);
                                System.arraycopy(tmpBuff, 0, buff, pos, readSize);
                                pos += readSize;
                            }
                            mHandler.obtainMessage(Common.MESSAGE_READ, Common.COMMAND_IMG, size, buff)
                                    .sendToTarget();
                            break;
                        case Common.COMMAND_MOTOR_STATE:
                            inStream.read(countBuff);
                            count = Integer.parseInt(new String(countBuff));

                            int[] motorBuff = new int[count];
                            for (int i=0; i < count; i++)
                            {
                                sizeBuff = new byte[3];
                                inStream.read(sizeBuff);
                                motorBuff[i] = Integer.parseInt(new String(sizeBuff));
                            }
                            mHandler.obtainMessage(Common.MESSAGE_READ, Common.COMMAND_MOTOR_STATE, motorBuff.length, motorBuff)
                                    .sendToTarget();
                            break;
                        case Common.COMMAND_SENSOR_STATE:
                            inStream.read(countBuff);
                            count = Integer.parseInt(new String(countBuff));

                            int[] sensorBuff = new int[count];
                            for (int i=0; i < count; i++)
                            {
                                byte[] signBuff = new byte[1];
                                inStream.read(signBuff);
                                int sign = Integer.parseInt(new String(signBuff));

                                sizeBuff = new byte[5];
                                inStream.read(sizeBuff);
                                sensorBuff[i] = Integer.parseInt(new String(sizeBuff));
                                if (sign == 1) { sensorBuff[i] = sensorBuff[i] * (-1); }
                            }
                            mHandler.obtainMessage(Common.MESSAGE_READ, Common.COMMAND_SENSOR_STATE, sensorBuff.length, sensorBuff)
                                    .sendToTarget();
                            break;

                        default:
                            mHandler.obtainMessage(Common.MESSAGE_READ, cmdCode, -1, -1)
                                    .sendToTarget();
                            break;
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                outStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(Common.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {

            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
