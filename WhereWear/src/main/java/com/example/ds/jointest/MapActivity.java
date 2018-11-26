package com.example.ds.jointest;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.ParserConfigurationException;

public class MapActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";
    String pathText;
    String phonenum = "";
    String home="";

    LinearLayout container;
    //TextView tv1;
    String sec = "";

    TMapView tMapView;
    boolean flag = true;

    TextToSpeech tts;

    TMapPoint startPoint = new TMapPoint(37.653252, 127.016104);
    //TMapPoint startPoint = new TMapPoint(37.651609, 127.013917);
    TMapPoint destPoint;
    // TMapPoint destPoint;

    TMapData tmapdata;

    int directionFlag;
    Intent intent;
    TMapGpsManager tmapgps;

    LocationManager locationManager;
    Location loc = null;
    LocationListener locationListener;
    Location startloc=null;


    private final int REQUEST_BLUETOOTH_ENABLE = 100;

    TextView tv;
    ImageButton gotohome;
    List<String> direction;
    String directionText;
    TMapPoint nowP;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
//set locationlistener
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                loc = location;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        gotohome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                },1);
            }
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);

        // DB & SMS
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        mAuth=FirebaseAuth.getInstance();
        FirebaseUser user= mAuth.getCurrentUser();
        if(user != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myref = database.getReference();
            Log.d("map", "user id: "+mAuth.getUid());
            myref.child(mAuth.getUid()).child("phone").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    phonenum=(String) dataSnapshot.getValue();
                    Log.d("map", "datachange="+phonenum);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("map", "cancel");
                }
            });
        }

        pathText="경로안내를 시작하겠습니다. ";
        tMapView=new TMapView(this);
        tmapdata=new TMapData();
        tmapgps=new TMapGpsManager(this);
        tmapgps.setMinTime(1000);//현재 위치를 찾을 최소 시간 (밀리초)
        tmapgps.setMinDistance(5);//현재 위치를 갱신할 최소 거리

        container=(LinearLayout) findViewById(R.id.container);
        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.speak(pathText,TextToSpeech.QUEUE_FLUSH, null);
                try{
                    Thread.sleep(5000);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        //선언

        intent=getIntent();

        //목적지 좌표 설정
        destPoint=translatePoint(intent.getStringExtra("address"));
        //tv1=(TextView)findViewById(R.id.pathText);

        //initTMapView();

        //container.addView(tMapView);

        startmap();
        //mConnectionStatus = (TextView)findViewById(R.id.connection_status_textview);
        // mInputEditText = (EditText)findViewById(R.id.input_string_edittext);
        tv=(TextView)findViewById(R.id.message_listview);

        configgps();
        getNowPoint();
        tMapView.addMarkerItem("markeritem1",getMarker(nowP));

    }
    public void configgps(){
        tMapView.setOnLongClickListenerCallback(new TMapView.OnLongClickListenerCallback() {
            @Override
            public void onLongPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint) {
                if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.INTERNET
                        },1);
                    }
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
                TMapPoint convertpoint=new TMapPoint(loc.getLatitude(),loc.getLongitude());
                //String msg=getNowPoint().getLatitude()+","+getNowPoint().getLongitude();
                //String msg=getAddress(getNowPoint());
                //tmapgps.setProvider(tmapgps.GPS_PROVIDER);
                //getNowPoint();
                Log.d("mapppp", "gps: "+ convertpoint.getLongitude());
                String msg=convertpoint.getLatitude()+", "+convertpoint.getLongitude();
                msg="※긴급상황※ 내 위치: "+getAddress(convertpoint);
                Log.d("msgphone", phonenum);
                sendSMS(phonenum, msg);
                Log.d("msgg",msg);
            }
        });
    }
    //sms
    public void sendSMS(String phoneNumber, String message){
        PendingIntent sentPI=PendingIntent.getBroadcast(this,0,new Intent(SENT),0);
        PendingIntent deliverPI=PendingIntent.getBroadcast(this,0,new Intent(DELIVERED),0);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(),"메시지 전송 완료",Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        },new IntentFilter(SENT));
        SmsManager sms=SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber,null,message,sentPI,deliverPI);
    }


    public void startmap(){
        initTMapView();

        container.addView(tMapView);

        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.speak("경로안내를 시작합니다",TextToSpeech.QUEUE_FLUSH, null);

            }
        });





    }//마커 생성
    public TMapMarkerItem getMarker(TMapPoint point){
        TMapMarkerItem markerItem1 = new TMapMarkerItem();
        // 마커 아이콘
        Bitmap bitmap = BitmapFactory.decodeResource(getApplication().getResources(), R.drawable.markericon);

        markerItem1.setIcon(bitmap);
        markerItem1.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정

        markerItem1.setTMapPoint(point);

        markerItem1.setName("내위치");
        return markerItem1;

    }
    public void speechDirection(){
        for(String str:direction){

            directionText=str;
            // directionText=direction.get(i);
            setTextSpeech();
            //System.out.print("말할거"+str);
            //Log.d("a", str);


        }
    }
    //tmap view
    public void initTMapView(){


        //키값
        tMapView.setSKTMapApiKey("1d586f0b-7fa1-4e8b-b552-dd35d4788c45");

        tMapView.setCenterPoint(127.016160,37.651445);


        //경로 그리기
        tmapdata.findPathData(startPoint, destPoint, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine tMapPolyLine) {
                tMapPolyLine.setLineWidth(10);
                tMapView.addTMapPath(tMapPolyLine);
            }
        });

        int n=0;
        //경로 텍스트
        tmapdata.findPathDataAllType(TMapData.TMapPathType.PEDESTRIAN_PATH, startPoint, destPoint, new TMapData.FindPathDataAllListenerCallback() {

            @Override

            public void onFindPathDataAll(Document document) {
                //backThread thread=new backThread();

               Element root = document.getDocumentElement();

                NodeList nodeListPlacemark = root.getElementsByTagName("Placemark");

               for( int i=0; i<nodeListPlacemark.getLength(); i++ ) {

                    NodeList nodeListPlacemarkItem = nodeListPlacemark.item(i).getChildNodes();

                   for( int j=0; j<nodeListPlacemarkItem.getLength(); j++ ) {


                        if( nodeListPlacemarkItem.item(j).getNodeName().equals("description") ) {
                            if(nodeListPlacemarkItem.item(j).getTextContent().trim().toString().length()>0){

                                String str=nodeListPlacemarkItem.item(j).getTextContent().trim().toString();
                                pathText=str;
                                setTextSpeech();
////                                sec="";
////                                for(int k=0;k<pathText.length();k++){
////                                    if(isStringDouble(pathText.charAt(k)+"")){
////                                        if(sec.length()>0){
////                                            if(!isStringDouble(pathText.charAt(k-1)+"")){
////                                                sec="";
////                                            }
////                                        }
////                                        sec+=pathText.charAt(k);
////                                    }
////                                }
////                                directionText=direction.get(directionFlag);
////                                if(directionFlag<direction.size()){
////                                    directionFlag++;
////
////                                }
////                                setTextSpeech();


                           }
                        }
                    }
                }




            }

        });



    }


    //주소->좌표로 변환해 반환
    public TMapPoint translatePoint(String address){
        TMapPoint tmap=null;
        List<Address> list=null;

        Geocoder geocoder=new Geocoder(getApplication());
        try{
            list=geocoder.getFromLocationName(address,10);
        }catch (IOException e){
            e.printStackTrace();
        }
        if(list!=null){
            if(list.size()==0) {
                Toast.makeText(getApplicationContext(), "해당주소정보없음", Toast.LENGTH_LONG).show();
            }else{
                Address addr=list.get(0);
                double lat = addr.getLatitude();
                double lon = addr.getLongitude();

                tmap=new TMapPoint(lat,lon);
            }
        }

        return  tmap;
    }
    boolean isStringDouble(String str){
        try{
            Integer.parseInt(str);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    public void getNowPoint(){
        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                },1);
            }
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
        if(loc!=null) {
            TMapPoint convertpoint = new TMapPoint(loc.getLatitude(), loc.getLongitude());
            nowP = convertpoint;
        }
        else{
            Log.d("getnowpoint","loc null");
            nowP=startPoint;
        }
    }

    public String getAddress(TMapPoint now){
        String address = "";
        // try {
        //     address = tmapdata.convertGpsToAddress(now.getLatitude(),now.getLongitude());
        //      Log.d("address",address);
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
        ConvertTask convertTask= new ConvertTask();
        try {
            address= (String) convertTask.execute(now).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return address;
    }


    //text 수정 후 말해주기 tts사용
    public void setTextSpeech(){
        //String txt ;

        try{
            Thread.sleep(5000);
            //경로 textview에 갱신
            runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    tv.setText(pathText);
                    tts.speak(pathText,TextToSpeech.QUEUE_FLUSH, null);


                    //tv.setText(directionText);
                }
            });


            Thread.sleep(120000);

            //Thread.sleep(1000);
            //Thread.sleep(Integer.parseInt(sec)*5000);//1m당 1초 쉬기
        }catch (Exception ex){
            ex.printStackTrace();
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //activity 소멸하면 tts도 소멸해줘야 한다.

        tts.shutdown();
    }



}
