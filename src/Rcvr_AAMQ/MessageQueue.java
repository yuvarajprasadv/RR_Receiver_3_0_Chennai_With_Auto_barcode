package Rcvr_AAMQ;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import com.rabbitmq.client.*;

public class MessageQueue extends Action {
	  protected final static String EXCHANGE_NAME = "Region";
	  protected final static String EXCHANGE_TYPE = "topic";
	  protected final static String USER_NAME = "aaw";
	  protected final static String PASSWORD = "aaw";
	  protected final static String VHOST = "AAW";
	  

	  protected static String TORNADO_HOST = "";
	  protected static String TORNADO_ENV = "";
	  
	  
	  protected final static String TORNADO_HOST_LIVE_1 = "https://tornado.schawk.com/tornado";	//LIVE new linux server dns
	  protected final static String TORNADO_HOST_LIVE_2 = "http://172.28.42.147:8080/tornado"; //LIVE new linux server ip
	  protected final static String TORNADO_HOST_LIVE_3 = "http://172.26.40.58:8080/tornado";	//LIVE linux alternative 
	  
	  protected final static String TORNADO_HOST_DEV = "http://172.28.42.151:8082/tornado"; // JAVA DEV
	  protected final static String TORNADO_HOST_QA = "http://172.28.42.168:8080/tornado"; // JAVA QA
	  
	  protected static String SCHAWK_EMAIL_HOST = "smtp.schawk.com";
	  protected static String SCHAWK_EMAIL_PORT = "25";

	  public static boolean GATE = true;
	  public static String MSGID = "";
	  public static boolean STATUS = true;
	  public static String ERROR = "";
	  public static String VERSION = "";
	  public static String MESSAGE = "";
	  public static String WORK_ORDER = "";
	  public static String LOCATION = "";

	  public static String PDF_PROPERTIES = "";
	  
	  //PDF-Config-Single
	  public static boolean sPdfNormal = false;
	  public static boolean sPdfPreset = false;
	  public static boolean sPdfNormalised = false;
	  
	  //PDF-Config-Multiple
	  public static boolean mPdfNormal = false;
	  public static boolean mPdfPreset = false;
	  public static boolean mPdfNormalised = false;
	  
	  //RR HUBX CTY INI
	  public static String category = "Road Runner";
	  
	  //RR TYPE
	  public static String RR_SOURCE="TORNADO_RR";

//	 protected final static String HOST_IP = "192.168.43.10";			// local system
	 protected final static String HOST_IP = "172.28.42.158";			// LIVE
//	 protected final static String HOST_IP =  "S2PTTRNMSGQ01P.asia.schawk.com"; //LIVE Dns
	 
	  static Logger log = LogMQ.monitor("Rcvr_AAMQ.MessageQueue");
	  
	  public static void RecvMessage(Channel channel, String queueName) throws Exception {
		  
		 if (GATE)
		 {
			MessageQueue.ERROR = "";
			STATUS = true;
			GATE = false;
			
			
		    Consumer consumer = new DefaultConsumer(channel) {
		        @Override
		        public void handleDelivery(String consumerTag, Envelope envelope,
		        AMQP.BasicProperties properties, byte[] body) throws IOException {
		          try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					log.error(e.getMessage());
				}
		          String message = new String(body, "UTF-8");
		          Date date = new Date();
		          SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		          System.out.println("Received: "+formatter.format(date)+" '" + envelope.getRoutingKey() + "':'" + message + "'");
		          log.info("Message received: "+formatter.format(date)+" '" + envelope.getRoutingKey() + "':'" + message + "'");
		          
		          try {
		        	  	MESSAGE = message;
					  JSONObject jsonObj = JsonParser.ParseJson(MESSAGE);
					  JsonParser jsonPars = new JsonParser();
					  TORNADO_ENV = (String) jsonPars.getJsonValueFromGroupKey(jsonObj, "region", "env");
					  
					  DDataOutput DO = new DDataOutput();
			          boolean isCustomisedOutput = DO.IsCustomizedDataExist(jsonObj);
			          
//			          if(!isCustomisedOutput)
//			          {
//				          if(TORNADO_ENV.equalsIgnoreCase("production"))
//						  {
//							  Action.acknowledge(message);
//						  }
//						  else if(TORNADO_ENV.equalsIgnoreCase("development") || TORNADO_ENV.equalsIgnoreCase("qa"))
//							  Action.acknowledge(message);
//			          }
//			          else if(isCustomisedOutput)
//			          {
//						  if(TORNADO_ENV.equalsIgnoreCase("production"))
//						  {
//							//  Action.acknowledge(message);
//							  DAction.acknowledge(message);
//						  }
//						  else if(TORNADO_ENV.equalsIgnoreCase("development") || TORNADO_ENV.equalsIgnoreCase("qa"))
//							  DAction.acknowledge(message);
//			          }
			          
			          if(TORNADO_ENV.equalsIgnoreCase("production"))
					  {
						  Action.acknowledge(message);
					  }
					  else if(TORNADO_ENV.equalsIgnoreCase("development") || TORNADO_ENV.equalsIgnoreCase("qa"))
						  DAction.acknowledge(message);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
				}
		        }
		      };
		      channel.basicConsume(queueName, true, consumer);
		 	}
		  }
	  
	  public static String getRouting(String strings){
		    return strings;
		  }

}
