package com.larefri;

import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.ProgressDialog;
import android.os.AsyncTask;

@SuppressWarnings("rawtypes")
public class RestClient  extends AsyncTask<Object,String, Object>{

	private Object result;
	protected ProgressDialog dialog;
    
	
	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	@SuppressWarnings({ "unchecked" })
	public Object makeRequestByForm(String path, Map params, List<NameValuePair> form) throws Exception 
	{
	    //instantiates HTTPClient to make request
	    DefaultHttpClient httpclient = new DefaultHttpClient();

	    //URL with the post data
	    HttpPost httpost = new HttpPost(path);

	    List<NameValuePair> nameValuePairs = form;
        
	    httpost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	    for(int i=0; i<form.size(); i++){
	    	httpost.setHeader("Content-Disposition", "form-data;name=\""+form.get(i).getName()+"\"");
	    }

	    //Handles what is returned from the page 
	    ResponseHandler responseHandler = new BasicResponseHandler();
	    return httpclient.execute(httpost, responseHandler);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object doInBackground(Object... params) {
		try {
			Object httpResponse = this.makeRequestByForm((String)params[0], (Map)params[1], (List<NameValuePair>)params[2]);
			return httpResponse;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Object result) {
		setResult(result);
	}

	@Override
	public String toString() {
		return this.getResult().toString();
	}
}
