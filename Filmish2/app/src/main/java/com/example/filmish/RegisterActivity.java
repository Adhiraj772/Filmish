package com.example.filmish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText name;
    private EditText username;
    private EditText dob;
    private EditText gender;
    private EditText email;
    private EditText password;
    private Button register1;
    private TextView userLogin;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private TextView pro;
    boolean[] selectedProfession;
    ArrayList<Integer> profList = new ArrayList<>();
    String[] profArray = {"Writer","Editor","Cinematographer","Producer","Director","Actor","Voice Artist","Costume Designer","Makeup Artist","Production Accountant","Graphic Designer","Animator/VFX Artist","Stuntman","Script Editor","Sound Mixer","Props Builder/Provider","Distributor/Sales Agent","Equipment Provider"};

    ProgressDialog pd;
    DatePickerDialog.OnDateSetListener setListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name = findViewById(R.id.name);
        username = findViewById(R.id.username);
        dob = findViewById(R.id.dob);
        gender = findViewById(R.id.gender);
        email = findViewById(R.id.email);
        password  = findViewById(R.id.password);
        register1 = findViewById(R.id.register1);
        userLogin = findViewById(R.id.user_login);
        pro=findViewById(R.id.pro);
        selectedProfession = new boolean[profArray.length];

        pro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        RegisterActivity.this
                );
                builder.setTitle("I am a");
                builder.setCancelable(false);
                builder.setMultiChoiceItems(profArray, selectedProfession, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i, boolean b) {
                       if(b){
                           profList.add(i);
                           Collections.sort(profList);
                       }else{
                           profList.remove(i);
                       }
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        StringBuilder stringBuilder  = new StringBuilder();
                        for(int j =0;j<profList.size();j++){
                            stringBuilder.append(profArray[profList.get(j)]);
                            if (j!=profList.size()-1){
                             stringBuilder.append(",");


                            }
                        }
                        pro.setText(stringBuilder.toString());

                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        for(int j=0;j<selectedProfession.length;j++){
                            selectedProfession[j] = false;
                            profList.clear();
                            pro.setText("");
                        }
                    }
                });
                builder.show();



            }
        });






        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);
        userLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
            }
        });
        register1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtName = name.getText().toString();
                String txtUserName = username.getText().toString();
                String txtDob = dob.getText().toString();
                String txtGender = gender.getText().toString();
                String txtEmail = email.getText().toString();
                String txtPassword = password.getText().toString();
                String txtProf = pro.getText().toString();

                if (TextUtils.isEmpty(txtName) || TextUtils.isEmpty(txtUserName) || TextUtils.isEmpty(txtDob) || TextUtils.isEmpty(txtGender) || TextUtils.isEmpty(txtEmail) || TextUtils.isEmpty(txtPassword)){
                    Toast.makeText(RegisterActivity.this, "Empty Credentials", Toast.LENGTH_SHORT).show();
                }else if(txtPassword.length()<6){
                    Toast.makeText(RegisterActivity.this, "Password is too short", Toast.LENGTH_SHORT).show();
                } else{
                    registerUser(txtName,txtUserName,txtDob,txtGender,txtEmail,txtPassword,txtProf);
                }

            }
        });



        Calendar calendar = Calendar.getInstance();
        final int year=calendar.get(Calendar.YEAR);
        final int month= calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                  @Override
                  public void onDateSet(DatePicker view, int year, int month, int day) {
                      month = month +1;
                      String date = day+"/"+month+"/"+year;
                      dob.setText(date);
                  }
              },year,month,day);
              datePickerDialog.show();

            }
        });
    }

    private void registerUser(String name, String username, String dob, String gender, String email, String password,String pro) {
        pd.setMessage("Please wait");
        pd.show();
        mAuth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                HashMap<String,Object> map = new HashMap<>();
                map.put("name",name);
                map.put("username", username);
                map.put("gender", gender);
                map.put("email",email);
                map.put("bio","");
                map.put("image","");
                map.put("imageurl","default");
                map.put("date of birth", dob);
                map.put("Profession", pro);
                map.put("id",mAuth.getCurrentUser().getUid());

                mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            pd.dismiss();
                            Toast.makeText(RegisterActivity.this, "Update the profile for better experience", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            finish();
                        }
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(RegisterActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}