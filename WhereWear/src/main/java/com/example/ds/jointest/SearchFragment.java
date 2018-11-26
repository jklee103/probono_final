package com.example.ds.jointest;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ds.jointest.utils.AudioWriterPCM;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.naver.speech.clientapi.SpeechConfig;
import com.naver.speech.clientapi.SpeechRecognitionException;
import com.naver.speech.clientapi.SpeechRecognitionListener;
import com.naver.speech.clientapi.SpeechRecognitionResult;
import com.naver.speech.clientapi.SpeechRecognizer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

/**
 * Created by DS on 2017-12-18.
 */

public class SearchFragment extends android.support.v4.app.Fragment {
    EditText et;
    ImageButton btnText;
    ViewGroup  rootview;
    private FirebaseAuth mAuth;
    String home="";
//    Button btn;
//    ConnectedTask mConnectedTask = null;
//    static BluetoothAdapter mBluetoothAdapter;
//    private String mConnectedDeviceName = null;
//    private ArrayAdapter<String> mConversationArrayAdapter;
//    static boolean isConnectionError = false;
//    private static final String TAG = "BluetoothClient";
//    private final int REQUEST_BLUETOOTH_ENABLE = 100;

    //네이버 클라이언트 id
    private static final String CLIENT_ID ="sHsDu_IcUaxJFz__PTgw";

    //clova
    private SearchFragment.RecognitionHandler handler;
    private NaverRecognizer naverRecognizer;
    //음성인식 결과값
    //private TextView txtResult;
    private ImageButton btnVoice;
    private String mResult;
    //audiowriter 클래스 생성(utils에 있음)
    private AudioWriterPCM writer;


    private void handleMessage(Message msg) {
        switch (msg.what) {
            case R.id.clientReady: // 음성인식 준비 가능
                et.setText("Connected");
                writer = new AudioWriterPCM(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NaverSpeechTest");
                writer.open("Test");
                break;
            case R.id.audioRecording:
                writer.write((short[]) msg.obj);
                break;
            case R.id.partialResult:
                mResult = (String) (msg.obj);
                et.setText(mResult);
                break;
            case R.id.finalResult: // 최종 인식 결과
                SpeechRecognitionResult speechRecognitionResult = (SpeechRecognitionResult) msg.obj;
                List<String> results = speechRecognitionResult.getResults();
                StringBuilder strBuf = new StringBuilder();
                for(String result : results) {
                    strBuf.append(result);
                    strBuf.append("\n");
                }

                mResult = results.get(0);
                et.setText(mResult);

                break;
            case R.id.recognitionError:
                if (writer != null) {
                    writer.close();
                }
                mResult = "Error code : " + msg.obj.toString();
                et.setText(mResult);
                //btn_start.setText(R.string.str_start);
                btnVoice.setEnabled(true);
                break;
            case R.id.clientInactive:
                if (writer != null) {
                    writer.close();
                }
                 //btnStart.setText(R.string.str_start);
                btnVoice.setEnabled(true);
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        naverRecognizer.getSpeechRecognizer().initialize();
    }

    @Override
    public void onStop() {
        super.onStop();
        naverRecognizer.getSpeechRecognizer().release();
    }

    @Override
    public void onResume() {
        super.onResume();
        mResult = "";
        et.setText("");
        //btn_start.setText(R.string.str_start);
        btnVoice.setEnabled(true);

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //xml과 연결
        rootview=(ViewGroup)inflater.inflate(R.layout.search,container,false);


        et=(EditText) rootview.findViewById(R.id.etResult);
        btnVoice=(ImageButton)rootview.findViewById(R.id.btnVoice);
       // btn=(Button)rootview.findViewById(R.id.blueTooth);
        handler = new SearchFragment.RecognitionHandler(this);
        naverRecognizer = new NaverRecognizer(this.getActivity(), handler, CLIENT_ID);
        btnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!naverRecognizer.getSpeechRecognizer().isRunning()) {
                    mResult = "";
                    et.setText("Connecting...");
                    //btnStart.setText(R.string.str_stop);
                    naverRecognizer.recognize();
                } else {
                    Log.d(TAG, "stop and wait Final Result");
                    btnVoice.setEnabled(false);
                    naverRecognizer.getSpeechRecognizer().stop();
                }

            }
        });
        btnText=(ImageButton)rootview.findViewById(R.id.btnText);
        btnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sendtext=et.getText().toString();

                //TMapPoint map=translatePoint("삼양로144길 19-7");
                // et.setText(map.getLongitude()+", "+map.getLatitude());
                Log.d("user","sendtext"+sendtext);
                if(sendtext.equals("집으로")){
                    sendtext="서울시 도봉구 쌍문동 422-25";
                    mAuth=FirebaseAuth.getInstance();
                    FirebaseUser user= mAuth.getCurrentUser();
                    Log.d("user","id: "+user.getUid());
                    if(user!=null){
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myref = database.getReference();
                        myref.child(mAuth.getUid()).child("address").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                home= (String) dataSnapshot.getValue();
                                Log.d("map", "datachange="+home);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }else{
                        Log.d("user","usernull");
                    }
                   // sendtext=home;
                }

                Log.d("map", "sendtext"+sendtext);
                Intent intent=new Intent(rootview.getContext(),MapActivity.class);
                intent.putExtra("address",sendtext);
                startActivity(intent);
            }
        });
//        btn.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View view) {
//                String sendMessage="sending message";
//                sendMessage(sendMessage);
//
//            }
//        });

//        mConversationArrayAdapter = new ArrayAdapter<>( this,
//                android.R.layout.simple_list_item_1 );
        //mMessageListview.setAdapter(mConversationArrayAdapter);


//        Log.d( TAG, "Initalizing Bluetooth adapter...");
//
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (mBluetoothAdapter == null) {
//            showErrorDialog("This device is not implement Bluetooth.");
//           // return;
//        }
//
//        if (!mBluetoothAdapter.isEnabled()) {
//            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
//        }
//        else {
//            Log.d(TAG, "Initialisation successful.");
//
//            showPairedDevicesListDialog();
//        }
        return rootview;
    }//네이버 api handler
    static class RecognitionHandler extends Handler {
        private final WeakReference<SearchFragment> mActivity;
        RecognitionHandler(SearchFragment activity) {
            mActivity = new WeakReference<SearchFragment>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            SearchFragment activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//
//        if ( mConnectedTask != null ) {
//
//            mConnectedTask.cancel(true);
//        }
//    }
//
//
////    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {
//
//        private BluetoothSocket mBluetoothSocket = null;
//        private BluetoothDevice mBluetoothDevice = null;
//
//        ConnectTask(BluetoothDevice bluetoothDevice) {
//            mBluetoothDevice = bluetoothDevice;
//            mConnectedDeviceName = bluetoothDevice.getName();
//
//            //SPP
//            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
//
//            try {
//                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
//                Log.d( TAG, "create socket for "+mConnectedDeviceName);
//
//            } catch (IOException e) {
//                Log.e( TAG, "socket create failed " + e.getMessage());
//            }
//
//            //mConnectionStatus.setText("connecting...");
//        }
//
//
//        @Override
//        protected Boolean doInBackground(Void... params) {
//
//            // Always cancel discovery because it will slow down a connection
//            mBluetoothAdapter.cancelDiscovery();
//
//            // Make a connection to the BluetoothSocket
//            try {
//                // This is a blocking call and will only return on a
//                // successful connection or an exception
//                mBluetoothSocket.connect();
//            } catch (IOException e) {
//                // Close the socket
//                try {
//                    mBluetoothSocket.close();
//                } catch (IOException e2) {
//                    Log.e(TAG, "unable to close() " +
//                            " socket during connection failure", e2);
//                }
//
//                return false;
//            }
//
//            return true;
//        }
//
//
//        @Override
//        protected void onPostExecute(Boolean isSucess) {
//
//            if ( isSucess ) {
//                connected(mBluetoothSocket);
//            }
//            else{
//
//                isConnectionError = true;
//                Log.d( TAG,  "Unable to connect device");
//                showErrorDialog("Unable to connect device");
//            }
//        }
//    }
//
//
//    public void connected( BluetoothSocket socket ) {
//        mConnectedTask = new ConnectedTask(socket);
//        mConnectedTask.execute();
//    }
//
//
//
//    private class ConnectedTask extends AsyncTask<Void, String, Boolean> {
//
//        private InputStream mInputStream = null;
//        private OutputStream mOutputStream = null;
//        private BluetoothSocket mBluetoothSocket = null;
//
//        ConnectedTask(BluetoothSocket socket){
//
//            mBluetoothSocket = socket;
//            try {
//                mInputStream = mBluetoothSocket.getInputStream();
//                mOutputStream = mBluetoothSocket.getOutputStream();
//            } catch (IOException e) {
//                Log.e(TAG, "socket not created", e );
//            }
//
//            Log.d( TAG, "connected to "+mConnectedDeviceName);
//            //mConnectionStatus.setText( "connected to "+mConnectedDeviceName);
//        }
//
//
//        @Override
//        protected Boolean doInBackground(Void... params) {
//
//            byte [] readBuffer = new byte[1024];
//            int readBufferPosition = 0;
//
//
//            while (true) {
//
//                if ( isCancelled() ) return false;
//
//                try {
//
//                    int bytesAvailable = mInputStream.available();
//
//                    if(bytesAvailable > 0) {
//
//                        byte[] packetBytes = new byte[bytesAvailable];
//
//                        mInputStream.read(packetBytes);
//
//                        for(int i=0;i<bytesAvailable;i++) {
//
//                            byte b = packetBytes[i];
//                            if(b == '\n')
//                            {
//                                byte[] encodedBytes = new byte[readBufferPosition];
//                                System.arraycopy(readBuffer, 0, encodedBytes, 0,
//                                        encodedBytes.length);
//                                String recvMessage = new String(encodedBytes, "UTF-8");
//
//                                readBufferPosition = 0;
//
//                                //Toast.makeText(rootview.getContext(), recvMessage.toString(), Toast.LENGTH_SHORT).show();
//                                btn.setText(recvMessage);
//                                Log.d(TAG, "recv message: " + recvMessage);
//                                publishProgress(recvMessage);
//                            }
//                            else
//                            {
//                                readBuffer[readBufferPosition++] = b;
//                            }
//                        }
//                    }
//                } catch (IOException e) {
//
//                    Log.e(TAG, "disconnected", e);
//                    return false;
//                }
//            }
//
//        }
//
//        @Override
//        protected void onProgressUpdate(String... recvMessage) {
//            //Toast.makeText(rootview.getContext(), recvMessage.toString(), Toast.LENGTH_SHORT).show();
//
//            //mConversationArrayAdapter.insert(mConnectedDeviceName + ": " + recvMessage[0], 0);
//        }
//
//        @Override
//        protected void onPostExecute(Boolean isSucess) {
//            super.onPostExecute(isSucess);
//
//            if ( !isSucess ) {
//
//
//                closeSocket();
//                Log.d(TAG, "Device connection was lost");
//                isConnectionError = true;
//                showErrorDialog("Device connection was lost");
//            }
//        }
//
//        @Override
//        protected void onCancelled(Boolean aBoolean) {
//            super.onCancelled(aBoolean);
//
//            closeSocket();
//        }
//
//        void closeSocket(){
//
//            try {
//
//                mBluetoothSocket.close();
//                Log.d(TAG, "close socket()");
//
//            } catch (IOException e2) {
//
//                Log.e(TAG, "unable to close() " +
//                        " socket during connection failure", e2);
//            }
//        }
//
//        void write(String msg){
//
//            msg += "\n";
//
//            try {
//                mOutputStream.write(msg.getBytes());
//                mOutputStream.flush();
//            } catch (IOException e) {
//                Log.e(TAG, "Exception during send", e );
//            }
//
//            //mInputEditText.setText(" ");
//        }
//    }
//
//
//    public void showPairedDevicesListDialog()
//    {
//        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
//        final BluetoothDevice[] pairedDevices = devices.toArray(new BluetoothDevice[0]);
//
//        if ( pairedDevices.length == 0 ){
//            showQuitDialog( "No devices have been paired.\n"
//                    +"You must pair it with another device.");
//            return;
//        }
//
//        String[] items;
//        items = new String[pairedDevices.length];
//        for (int i=0;i<pairedDevices.length;i++) {
//            items[i] = pairedDevices[i].getName();
//        }
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
//        builder.setTitle("Select device");
//        builder.setCancelable(false);
//        builder.setItems(items, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//
//                ConnectTask task = new ConnectTask(pairedDevices[which]);
//                task.execute();
//            }
//        });
//        builder.create().show();
//    }
//
//
//
//    public void showErrorDialog(String message)
//    {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
//        builder.setTitle("Quit");
//        builder.setCancelable(false);
//        builder.setMessage(message);
//        builder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                if ( isConnectionError  ) {
//                    isConnectionError = false;
//                    //finish();
//                }
//            }
//        });
//        builder.create().show();
//    }
//
//
//    public void showQuitDialog(String message)
//    {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
//        builder.setTitle("Quit");
//        builder.setCancelable(false);
//        builder.setMessage(message);
//        builder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//               // finish();
//            }
//        });
//        builder.create().show();
//    }
//
//    void sendMessage(String msg){
//
//        if ( mConnectedTask != null ) {
//            mConnectedTask.write(msg);
//            Log.d(TAG, "send message: " + msg);
//            //mConversationArrayAdapter.insert("Me:  " + msg, 0);
//        }
//    }


//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        if(requestCode == REQUEST_BLUETOOTH_ENABLE){
//            if (resultCode == RESULT_OK){
//                //BlueTooth is now Enabled
//                showPairedDevicesListDialog();
//            }
//            if(resultCode == RESULT_CANCELED){
//                showQuitDialog( "You need to enable bluetooth");
//            }
//        }
//    }

}
class NaverRecognizer implements SpeechRecognitionListener {
    private final static String TAG = NaverRecognizer.class.getSimpleName();
    private Handler mHandler;
    private SpeechRecognizer mRecognizer;

    public NaverRecognizer(FragmentActivity context, Handler handler, String clientId) {
        this.mHandler = handler;
        try {
            mRecognizer = new SpeechRecognizer(context, clientId);
        } catch (SpeechRecognitionException e) {
            e.printStackTrace();
        }
        mRecognizer.setSpeechRecognitionListener(this);
    }

    public SpeechRecognizer getSpeechRecognizer() {
        return mRecognizer;
    }

    public void recognize() {
        try {
            mRecognizer.recognize(new SpeechConfig(SpeechConfig.LanguageType.KOREAN, SpeechConfig.EndPointDetectType.AUTO));
        } catch (SpeechRecognitionException e) {
            e.printStackTrace();
        }
    }

    @Override
    @WorkerThread
    public void onInactive() {
        Message msg = Message.obtain(mHandler, R.id.clientInactive);
        msg.sendToTarget();
    }

    @Override
    @WorkerThread
    public void onReady() {
        Message msg = Message.obtain(mHandler, R.id.clientReady);
        msg.sendToTarget();
    }

    @Override
    @WorkerThread
    public void onRecord(short[] speech) {
        Message msg = Message.obtain(mHandler, R.id.audioRecording, speech);
        msg.sendToTarget();
    }

    @Override
    @WorkerThread
    public void onPartialResult(String result) {
        Message msg = Message.obtain(mHandler, R.id.partialResult, result);
        msg.sendToTarget();
    }

    @Override
    @WorkerThread
    public void onEndPointDetected() {
        Log.d(TAG, "Event occurred : EndPointDetected");
    }

    @Override
    @WorkerThread
    public void onResult(SpeechRecognitionResult result) {
        Message msg = Message.obtain(mHandler, R.id.finalResult, result);
        msg.sendToTarget();
    }

    @Override
    @WorkerThread
    public void onError(int errorCode) {
        Message msg = Message.obtain(mHandler, R.id.recognitionError, errorCode);
        msg.sendToTarget();
    }

    @Override
    @WorkerThread
    public void onEndPointDetectTypeSelected(SpeechConfig.EndPointDetectType epdType) {
        Message msg = Message.obtain(mHandler, R.id.endPointDetectTypeSelected, epdType);
        msg.sendToTarget();
    }
}
