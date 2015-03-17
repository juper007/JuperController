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
                    byte[] sizeBuff;
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
                        case Common.COMMAND_STATE:
                            int[] motorBuff = new int[4];
                            for (int i=0; i < 4; i++)
                            {
                                sizeBuff = new byte[2];
                                inStream.read(sizeBuff);
                                motorBuff[i] = Integer.parseInt(sizeBuff.toString());
                            }
                            mHandler.obtainMessage(Common.MESSAGE_READ, Common.COMMAND_STATE, motorBuff.length, motorBuff)
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
