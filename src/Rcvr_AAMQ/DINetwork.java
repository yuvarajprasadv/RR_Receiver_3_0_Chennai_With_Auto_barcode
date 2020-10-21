package Rcvr_AAMQ;

import java.net.*;
import java.util.Collections;
import java.util.Enumeration;

import org.apache.log4j.Logger;

public class DINetwork 
{
	static Logger log = DLogMQ.monitor("Rcvr_AAMQ.DINetwork");
	 public String GetClientIPAddrs()
	 {
	        try {
	            InetAddress ipAddr = InetAddress.getLocalHost();
	          
	            return (ipAddr.getHostAddress());
	        } catch (Exception ex) 
	        {
	        		log.error(MessageQueue.WORK_ORDER + ": " + "Error on fetching client IP address");
	           // ex.printStackTrace();
	        }
			return null;
	  }
	 
	 
		public String GetClientIPAddr()
		{
			try
			{
			Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		    for (NetworkInterface netint : Collections.list(nets))
		    {
		        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
		        if(netint.getDisplayName().equals("en0"))
		        for (InetAddress inetAddress : Collections.list(inetAddresses)) 
		        {
		        		if((inetAddress.toString().split("\\.")).length ==  4 )
		            return inetAddress.getHostAddress();
		        }
		     }
			return null;
			}
			catch (Exception SocketException)
			{
				return null;
			}
		}

	 public static void main(String[] args) throws UnknownHostException
	 {
		 DINetwork inet = new DINetwork();
		 System.out.println( inet.GetClientIPAddr());
		    
	 }

}