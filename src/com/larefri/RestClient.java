package com.larefri;

import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import android.os.AsyncTask;

@SuppressWarnings("rawtypes")
public class RestClient  extends AsyncTask{

	@SuppressWarnings({ "unchecked" })
	public Object makeRequestByForm(String path, Map params, List<NameValuePair> form) throws Exception 
	{
	    //instantiates httpclient to make request
	    DefaultHttpClient httpclient = new DefaultHttpClient();

	    //url with the post data
	    HttpPost httpost = new HttpPost(path);

	    //convert parameters into JSON object
	    //JSONObject holder = new JSONObject(params);

	    //passes the results to a string builder/entity
	    //StringEntity se = new StringEntity(holder.toString());

	    //sets the post request as the resulting string
	    //httpost.setEntity(se);
	    List<NameValuePair> nameValuePairs = form;
        
	    httpost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	    //sets a request header so the page receving the request
	    //will know what to do with it
	    //httpost.setHeader("Accept", "application/json");
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
		// TODO Auto-generated method stub
		try {
			Object httpResponse = this.makeRequestByForm((String)params[0], (Map)params[1], (List<NameValuePair>)params[2]);
			return httpResponse;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/*@SuppressWarnings("rawtypes")
	private static JSONObject getJsonObjectFromMap(Map params) throws JSONException {

	    //all the passed parameters from the post request
	    //iterator used to loop through all the parameters
	    //passed in the post request
	    Iterator iter = params.entrySet().iterator();

	    //Stores JSON
	    JSONObject holder = new JSONObject();

	    //using the earlier example your first entry would get email
	    //and the inner while would get the value which would be 'foo@bar.com' 
	    //{ fan: { email : 'foo@bar.com' } }

	    //While there is another entry
	    while (iter.hasNext()) 
	    {
	        //gets an entry in the params
	        Map.Entry pairs = (Map.Entry)iter.next();

	        //creates a key for Map
	        String key = (String)pairs.getKey();

	        //Create a new map
	        Map m = (Map)pairs.getValue();   

	        //object for storing Json
	        JSONObject data = new JSONObject();

	        //gets the value
	        Iterator iter2 = m.entrySet().iterator();
	        while (iter2.hasNext()) 
	        {
	            Map.Entry pairs2 = (Map.Entry)iter2.next();
	            data.put((String)pairs2.getKey(), (String)pairs2.getValue());
	        }

	        //puts email and 'foo@bar.com'  together in map
	        holder.put(key, data);
	    }
	    return holder;
	}*/
}
