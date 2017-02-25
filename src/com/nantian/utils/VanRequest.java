package com.nantian.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;


public class VanRequest {
	
	private String baseUrl;
	
	public VanRequest(String ip, int port) {
		baseUrl = "http://" + ip + ":" + port;
	}
	
	public String execute(String method, String cmd) throws Exception {
		
		String url = baseUrl + method + "?cmd=" + cmd;

		RequestThread thread = new RequestThread(url);
		
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			// e.printStackTrace();
		}
		
		String result = thread.getResponse();
		
		return result;
	}
	
	public class RequestThread extends Thread {
		
		private String url;
		private String ret;
		
		public RequestThread(String url) {
			this.url  = url;
		}

		@Override
		public void run() {
			super.run();
			
			try {
				ret = doRequest();
			} catch (Exception e) {
			}
		}
		
		private String doRequest() throws Exception {
			
			HttpGet request = new HttpGet(url);
			
			HttpParams params = new BasicHttpParams();
		    HttpConnectionParams.setConnectionTimeout(params, 1000);
		    HttpConnectionParams.setSoTimeout(params, 3000);
		    HttpProtocolParams.setUseExpectContinue(params, false);
			
		    DefaultHttpClient httpClient = new DefaultHttpClient(params);
		    httpClient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
			HttpResponse response = null;

			response = httpClient.execute(request);
			
			int statusCode = response.getStatusLine().getStatusCode();
			
			if (HttpStatus.SC_OK != statusCode) {
				System.err.println("ErrorCode: " + statusCode);
			}
			
			HttpEntity entity = response.getEntity();
			
			String strResult = null;
			strResult = EntityUtils.toString(entity);

			return strResult;
		}
		
		public String getResponse() {
			return ret;
		}
	}
}
