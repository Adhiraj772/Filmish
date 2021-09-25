package com.example.filmish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
 private BottomNavigationView bottomNavigationView;
 private Fragment selectorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction().replace(R.id.parent_container,new FirstFragment()).commit();
        bottomNavigationView = findViewById(R.id.bottomView);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selected = null;
                switch (item.getItemId()){
                    case R.id.miHome:
                        selectorFragment = new FirstFragment();
                        break;
                    case R.id.miPerSearch:
                        selectorFragment = new SecondFragment();
                        break;
                    case R.id.miAdd:
                        selectorFragment = null;
                        startActivity(new Intent(MainActivity.this,AddActivity.class));
                        break;
                    case R.id.miTrack:
                        selectorFragment = new FourthFragment();
                        break;
                    case R.id.miSettings:
                        selectorFragment = new FifthFragment();
                        break;

                }
                getSupportFragmentManager().beginTransaction().replace(R.id.parent_container,selectorFragment).commit();

                return true;
            }




            });




        }

    }


