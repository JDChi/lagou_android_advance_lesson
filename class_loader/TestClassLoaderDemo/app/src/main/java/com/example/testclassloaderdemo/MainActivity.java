package com.example.testclassloaderdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {
    private Button button;
    private ISay iSay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.bt_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final File jarFile = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "say_something_hotfix.jar");
                if (!jarFile.exists()) {
                    iSay = new SayException();
                    Toast.makeText(MainActivity.this, iSay.saySomething(), Toast.LENGTH_SHORT).show();
                } else {
                    DexClassLoader dexClassLoader = new DexClassLoader(jarFile.getAbsolutePath(), getExternalCacheDir().getAbsolutePath(), null, getClassLoader());
                    try {
                        Class clazz = dexClassLoader.loadClass("com.example.testclassloaderdemo.SayHotFix");
                        iSay = (ISay) clazz.newInstance();
                        Toast.makeText(MainActivity.this, iSay.saySomething(), Toast.LENGTH_SHORT).show();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
