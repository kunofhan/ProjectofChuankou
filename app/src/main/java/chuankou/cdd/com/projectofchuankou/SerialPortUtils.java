package chuankou.cdd.com.projectofchuankou;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import android_serialport_api.SerialPort;

/**
 * Created by hyk on 2018/2/2.
 */

public class SerialPortUtils {

    private final String TAG = "SerialPortUtils";
    private String path = "/dev/ttyS";
    private int baudrate = 9600;
    public boolean serialPortStatus = false; //是否打开串口标志
    public boolean threadStatus; //线程状态，为了安全终止线程

    public SerialPort serialPort = null;
    public InputStream inputStream = null;
    public OutputStream outputStream = null;
    private ReadThread readThread;


    /**
     * 打开串口
     *
     * @return serialPort串口对象
     */
    public SerialPort openSerialPort() {
        try {
            serialPort = new SerialPort(new File(path), baudrate, 0);
            this.serialPortStatus = true;
            threadStatus = false; //线程状态

            //获取打开的串口中的输入输出流，以便于串口数据的收发
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            if (readThread == null) {
                readThread = new ReadThread();
                readThread.start();
            }
            new ReadThread().start(); //开始线程监控是否有数据要接收
        } catch (Exception e) {
            Log.e(TAG, "openSerialPort: 打开串口异常：" + e.toString());
            return serialPort;
        }
        Log.d(TAG, "openSerialPort: 打开串口");
        return serialPort;
    }

    /**
     * 关闭串口
     */
    public void closeSerialPort() {
        try {
            inputStream.close();
            outputStream.close();

            this.serialPortStatus = false;
            this.threadStatus = true; //线程状态
            serialPort.close();
        } catch (IOException e) {
            Log.e(TAG, "closeSerialPort: 关闭串口异常：" + e.toString());
            return;
        }
        Log.d(TAG, "closeSerialPort: 关闭串口成功");
    }

    /**
     * 开始标志
     */
    private static final byte START_BYTE = 0x01;
    /**
     * 结束标志
     */
    private static final byte END_BYTE = 0x00;

    /**
     * 发送串口指令（字符串）
     *
     * @param data String数据指令
     */
    public void sendSerialPort(String data) {
        Log.d(TAG, "sendSerialPort: 发送数据");
        try {
            byte[] data1 = data.getBytes(Charset.forName("UTF-8")); //string转byte[]
//            this.data_ = new String(sendData); //byte[]转string
            if (data1.length > 0) {
                byte[] sendData = new byte[data1.length + 2];
                sendData[0] = START_BYTE;
                sendData[sendData.length - 1] = END_BYTE;
                System.arraycopy(data1, 0, sendData, 1, data1.length);
                outputStream.write(sendData);
                outputStream.write('\n');
                //outputStream.write('\r'+'\n');
                outputStream.flush();
                Log.d(TAG, "sendSerialPort: 串口数据发送成功");
                onDataReceiveListener.onDataSend(true);
            }
        } catch (IOException e) {
            Log.e(TAG, "sendSerialPort: 串口数据发送失败：" + e.toString());
            onDataReceiveListener.onDataSend(false);
        }
    }

    /**
     * 单开一线程，来读数据
     */
    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            //判断进程是否在运行，更安全的结束进程
            while (!threadStatus) {
                Log.d(TAG, "进入线程run");
                //64   1024
                byte[] buffer = new byte[64];
                int size; //读取数据的大小
                try {
                    size = inputStream.read(buffer);
                    if (size > 0) {
                        System.out.println("run: 接收到了数据大小：" + String.valueOf(size));
                        Log.d(TAG, "run: 接收到了数据大小：" + String.valueOf(size));
                        onDataReceiveListener.onDataReceive(buffer, size);
                    }
                } catch (IOException e) {
                    System.out.println("run: 数据读取异常：" + e.toString());
                    Log.e(TAG, "run: 数据读取异常：" + e.toString());
                }
            }
        }
    }

    //这是写了一监听器来监听接收数据
    private OnDataReceiveListener onDataReceiveListener = null;

    public static interface OnDataReceiveListener {
        public void onDataReceive(byte[] buffer, int size);

        public void onDataSend(boolean success);
    }

    public void setOnDataReceiveListener(OnDataReceiveListener dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }

}
