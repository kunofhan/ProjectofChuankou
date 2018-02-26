package chuankou.cdd.com.projectofchuankou;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView scan_all;
    private TextView port_name;
    private TextView connect;
    private TextView send_mgs;
    private TextView send;
    private TextView receive_mgs;
    private SerialPortUtils portUtils;
    /**
     * 开始标志
     */
    private static final byte START_BYTE = 0x01;
    /**
     * 结束标志
     */
    private static final byte END_BYTE = 0x00;
    //数据

    byte[] result_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      /*byte app = (byte) 0x00;
      int i = (app < 0) ? 256 + app : app;
       System.out.println("byte:"+i);
       System.out.println("byte:"+app);*/
        scan_all = (TextView) findViewById(R.id.main_scan_all);
        port_name = (TextView) findViewById(R.id.main_port_name);
        connect = (TextView) findViewById(R.id.main_port_connect);
        send_mgs = (TextView) findViewById(R.id.main_edit_mgs);
        send = (TextView) findViewById(R.id.main_send);
        receive_mgs = (TextView) findViewById(R.id.main_receive);
        scan_all.setOnClickListener(this);
        connect.setOnClickListener(this);
        send.setOnClickListener(this);

        portUtils = new SerialPortUtils();
        portUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
            @Override
            public void onDataReceive(byte[] buffer, int size) {

                if (buffer[0] == START_BYTE || result_data == null) {
                    result_data = new byte[size - 1];
                    System.arraycopy(buffer, 1, result_data, 0, size - 1);
                } else {
                    byte[] data = new byte[size + result_data.length];
                    System.arraycopy(result_data, 0, data, 0, result_data.length);
                    System.arraycopy(buffer, 0, data, result_data.length, size);
                    result_data = new byte[data.length];
                    System.arraycopy(data, 0, result_data, 0, data.length);
                }
                if (result_data[result_data.length - 1] == END_BYTE) {
                    String receive = new String(result_data, 0, result_data.length - 1);
                    receive_mgs.setText(receive);
                    result_data = null;
                }
            }

            @Override
            public void onDataSend(boolean success) {
                if (success) {
                    Toast.makeText(MainActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
        SerialPort port = portUtils.openSerialPort();
        System.out.println("port" + port);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_scan_all:
                SerialPortFinder finder = new SerialPortFinder();
                String[] devices = finder.getAllDevices();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < devices.length; i++) {
                    sb.append(devices[i] + "    ");
                }
                port_name.setText(sb.toString());
                break;
            case R.id.main_port_connect:
                SerialPort port = portUtils.openSerialPort();
                System.out.println("port" + port);
                break;
            case R.id.main_send:
                portUtils.sendSerialPort(send_mgs.getText().toString().trim());
                break;
        }
    }
}
