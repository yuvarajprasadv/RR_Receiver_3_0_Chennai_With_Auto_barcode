package Rcvr_AAMQ;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LogMQ {
	static Logger log;
	public static Logger monitor(String logClassName)
	{ 
	   try
	   {
			Utils utils = new Utils();
		  	Class logClass = Class.forName(logClassName);
		   	log = Logger.getLogger(logClass.getName());
			String logFilePath = utils.GetPathFromResource("logger.properties");
		   	PropertyConfigurator.configure(logFilePath);
		   	return log;
	   }
	   catch (Exception ex)
	   {
		  System.out.println(ex.getMessage());
	}
	return null;
	}

}