package com.example.ds.jointest;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class JoinActivity extends AppCompatActivity {
    //서버 태그
    private static String TAG = "jointest_MainActivity";
    private EditText etEmail, etPW, etPWcheck,etAddress,etPhone;
    Button btnJoin;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        //get firebase auth

        etEmail=(EditText)findViewById(R.id.etEmail);
        etPW=(EditText)findViewById(R.id.etPW);
        etPWcheck=(EditText)findViewById(R.id.etPWcheck);
        etAddress=(EditText)findViewById(R.id.etAddress);
        etPhone=findViewById(R.id.etPhone);
        btnJoin=(Button)findViewById(R.id.btnJoin);
        mAuth=FirebaseAuth.getInstance();
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etEmail.getText().toString();
                String password = etPW.getText().toString();
                String pwcheck = etPWcheck.getText().toString();
                String address = etAddress.getText().toString();
                String phone = etPhone.getText().toString();
                //비밀번호 일치 확인
                if (password.equals(pwcheck)) {
                    //JoinActivity.InsertData task = new JoinActivity.InsertData();
                    //task.execute(email, password, address);
                    //위에껀 mysql코드
                    //아래 파이어베이스 가입
                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                                Log.d(TAG,"success");
                            else
                                Log.d(TAG,"fail");
                        }
                    });
                    //회원가입하면서 로그인
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                                Log.d(TAG, "suc");
                            else
                                Log.d(TAG, "fail");
                        }
                    });
                    //mAuth=FirebaseAuth.getInstance();
                    FirebaseUser user= mAuth.getCurrentUser();

                    if(user != null){
                        FirebaseDatabase database=FirebaseDatabase.getInstance();
                        DatabaseReference myref=database.getReference();
                        Log.d(TAG,"db-phone:"+phone);
                        Log.d(TAG,"db-uid:"+mAuth.getUid());
                        Log.d(TAG,"db-userid:"+user.getUid());
                        myref.child(mAuth.getUid()).child("phone").setValue(phone);
                        myref.child(mAuth.getUid()).child("address").setValue(address);
                    }else{
                        Log.d(TAG,"join fail");
                    }
                    etEmail.setText("");
                    etPW.setText("");
                    etPWcheck.setText("");
                    etAddress.setText("");
                    etPhone.setText("");
                    Toast.makeText(getApplicationContext(), "회원가입 완료", Toast.LENGTH_SHORT).show();
                    //startActivity(intent);}
                }
                //다르면 toast
                else{
                    Toast.makeText(getApplicationContext(), "비밀번호 불일치", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    class InsertData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(JoinActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {

            String name = (String)params[0];
            String tel = (String)params[1];
            String add = (String)params[2];
            //서버컴 ip 주소, 가입 php 파일
            String serverURL = "http://203.252.218.88/insert.php";
            String postParameters = "email=" + name + "&password=" + tel + "&address=" + add;


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                //httpURLConnection.setRequestProperty("content-type", "application/json");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString();


            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            }

        }
    }
}
