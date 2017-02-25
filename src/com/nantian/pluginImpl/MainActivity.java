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
        setContentView(R.layout.activity_main);
        //dialog = new SignNameDialog(this,300,300);
        hander = new Handler(){
            @Override
            public void dispatchMessage(Message msg) {
                super.dispatchMessage(msg);
               // dialog.show();
            }
        };
        Setting.instance().setContext(this);
        //dialog = new SignNameDialog(this, 150, 150, 500, 600);
       // hander.sendEmptyMessage(1);
        edit = (EditText) findViewById(R.id.editText);
        pluginHandler = new PluginHandler();
   
        pluginHandler.setContext(this);
        //dialog = new SignNameDialog(this,20,100,300,400);
        
      
        findViewById(R.id.action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
         
            	try {

            	JSONObject json = new JSONObject(edit.getText().toString());
            	final String tag = json.optString("cmd");
            	final String data = json.optString("data","{}");
                new Thread(){
                	public void run() {
                		try {
							pluginHandler.execute(tag,new JSONObject(data));
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                	};
                }.start();
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
    
            
            }
        });


    }
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	super.onBackPressed();
    	JSONObject json;
		try {
			json = new JSONObject("{\"cmd\":\"dismissSign\",\"fileName\":\"ImplPlugin.apk\"}");
	    	String tag = json.optString("cmd");
	    	String data = json.optString("data","{}");
	        
				pluginHandler.execute(tag,new JSONObject(data));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
