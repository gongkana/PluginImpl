package com.nantian.pluginImpl;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.nantian.ad.PlayerFragment;
import com.nantian.sign.SignNameDialog;
import com.nantian.utils.Setting;
import com.van.paperless.R;




public class MainActivity extends Activity {
    private Handler hander;
    private EditText edit;
    private  PluginHandler pluginHandler;
    private SignNameDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Setting.instance().setContext(this);
    
        setContentView(R.layout.activity_main);
        PlayerFragment fragment = new PlayerFragment();
        getFragmentManager().beginTransaction().replace(R.id.frame, fragment).commit();
        //dialog = new SignNameDialog(this,300,300)

    }
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	super.onBackPressed();

    }
    
    
	public String ReadFile(String Path){
		BufferedReader reader = null;
		String laststr = "";
		try{
		FileInputStream fileInputStream = new FileInputStream(Path);
		InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
		reader = new BufferedReader(inputStreamReader);
		String tempString = null;
		while((tempString = reader.readLine()) != null){
		laststr += tempString;
		}
		reader.close();
		}catch(IOException e){
		e.printStackTrace();
		}finally{
		if(reader != null){
		try {
		reader.close();
		} catch (IOException e) {
		e.printStackTrace();
		}
		}
		}
		return laststr;
		}
}
