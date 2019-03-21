package Rcvr_AAMQ;

import java.io.*;
import java.net.*;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;

import java.security.SecureRandom; 
import java.security.cert.CertificateException; 
import java.security.cert.X509Certificate; 

import javax.net.ssl.HostnameVerifier; 
import javax.net.ssl.SSLContext; 
import javax.net.ssl.SSLSession; 
import javax.net.ssl.SSLSocketFactory; 
import javax.net.ssl.TrustManager; 
import javax.net.ssl.X509TrustManager;
import org.apache.log4j.Logger; 


public class HttpConnection {

	   private HostnameVerifier mDefaultHostnameVerifier = null; 
	   private SSLSocketFactory mDefaultSSLSocketFactory = null; 
	   static Logger log = LogMQ.monitor("Rcvr_AAMQ.HttpConnection");

	   
public static String excutePost(String targetURL, String urlParameters)
{
	  HttpURLConnection connection = null; 
		
	  try 
	  {
	    URL url = new URL(targetURL);
	    connection = (HttpURLConnection)url.openConnection();
	    connection.setRequestMethod("POST");
	    connection.setRequestProperty("Content-Type", "text/plain");
	    connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
	    connection.setRequestProperty("Content-Language", "en-US");  
	    connection.setUseCaches(false);
	    connection.setDoOutput(true);

	    DataOutputStream wr = new DataOutputStream (
	    connection.getOutputStream());
	    wr.writeBytes(urlParameters);
	    wr.close();

	    InputStream is = connection.getInputStream();
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	    StringBuilder response = new StringBuilder(); 
	    String line;
	    while((line = rd.readLine()) != null) {
	      response.append(line);
	      response.append('\r');
	    }
	    rd.close();
	    return response.toString();
	  }
	  catch (Exception e) 
	  {
		log.error(e.getMessage());
	    return null;
	  } 
	  finally 
	  {
	    if(connection != null) 
	    {
	      connection.disconnect(); 
	    }
	  }
}

private void trustAllHosts() 
{ 
    TrustManager[] trustAllCerts = new TrustManager[] 
    		{ 
    			new X509TrustManager() 
	    		{ 
		        @Override 
		        public X509Certificate[] getAcceptedIssuers() 
		        { 
		            return new X509Certificate[] {}; 
		        } 
		
		        @Override 
		        public void checkClientTrusted(X509Certificate[] chain, 
		                String authType) throws CertificateException 
		        { 
		        } 
		
		        @Override 
		        public void checkServerTrusted(X509Certificate[] chain, 
		                String authType) throws CertificateException 
		        { 
		        } 
	    
	    		} 
    		}; 

    // trusting-all TrustManager 
    try 
    { 
        mDefaultSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory(); 
        // TrustManager 
        SSLContext sc = SSLContext.getInstance("TLS"); 
        sc.init(null, trustAllCerts, new SecureRandom()); 
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory()); 
    } 
    catch (Exception e) 
    { 
       log.error(e.getMessage());
    } 
}

private final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() 
{ 
    public boolean verify(String hostname, SSLSession session) 
    { 
        return true; 
    } 
}; 

private HttpURLConnection getURLConnection(URL url, boolean trustEveryone) throws IOException 
{
	System.setProperty ("jsse.enableSNIExtension", "false");
	
    HttpURLConnection conn = null;
    if (url.getProtocol().toLowerCase().equals("https")) 
    {
        if (!trustEveryone) 
        {
            conn = (HttpsURLConnection) url.openConnection();
        }
        else 
        {
            trustAllHosts();
            HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
            mDefaultHostnameVerifier = https.getHostnameVerifier();
            https.setHostnameVerifier(DO_NOT_VERIFY);
            conn = https;
        }
    }
    else 
    {
        conn = (HttpURLConnection) url.openConnection();
    }
    return conn;
}

	public static void main(String args[]) throws IOException
	{
		System.out.println("DATE");
		System.out.println( HttpConnection.excutePost("http://10.112.98.48:8080/AAW/message/resp", "cfe5fe50-248d-4f48-b808-07f5c4756502::received::10.112.98.24"));
	}
}