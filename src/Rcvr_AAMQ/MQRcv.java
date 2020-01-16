package Rcvr_AAMQ;


import org.apache.log4j.Logger;

import com.rabbitmq.client.*;

public class MQRcv extends MessageQueue {
	static Logger log = LogMQ.monitor("Rcvr_AAMQ.MQRcv");
	
	public void handleRecovery( Recoverable recoverable ) {

		if( recoverable instanceof Channel ) {
			int channelNumber = ((Channel) recoverable).getChannelNumber();
		}
	}
	
	
  public static void main(String[] argv) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost(MessageQueue.HOST_IP);
	    factory.setVirtualHost(MessageQueue.VHOST);
	    factory.setUsername(MessageQueue.USER_NAME);
	    factory.setPassword(MessageQueue.PASSWORD);
	    factory.setRequestedChannelMax(1);
	    factory.setAutomaticRecoveryEnabled(true);
	    factory.setNetworkRecoveryInterval(5000);
	    factory.setRequestedHeartbeat(5);
	    Connection connection = null;
	    Channel channel = null;
	    
	    try
	    {
		    connection = factory.newConnection();
		    channel = connection.createChannel(); 
		    ((Recoverable)connection) .addRecoveryListener(new RecoveryListener() {
				
				@Override
				public void handleRecovery(Recoverable recoverable) {
					if( recoverable instanceof Channel ) {
						int channelNumber = ((Channel) recoverable).getChannelNumber();
					}
					
				}
			});
		    channel.exchangeDeclare(MessageQueue.EXCHANGE_NAME, MessageQueue.EXCHANGE_TYPE);
		    
	    } 
	    catch (java.net.ConnectException e)
	    {
	    		Thread.sleep(5000);
	    		Address[] addresses = {new Address(MessageQueue.HOST_IP)};
	    		connection = factory.newConnection(addresses);
	    		 channel = connection.createChannel(); 
	    		 ((Recoverable)connection) .addRecoveryListener(new RecoveryListener() {
	    				
	    				@Override
	    				public void handleRecovery(Recoverable recoverable) {
	    					if( recoverable instanceof Channel ) {
	    						int channelNumber = ((Channel) recoverable).getChannelNumber();
	    					}
	    					
	    				}
	    			});
		     
		    channel.exchangeDeclare(MessageQueue.EXCHANGE_NAME, MessageQueue.EXCHANGE_TYPE);
		    
		}


   if (argv.length < 1) {
      System.err.println("Binding key parameter missing");
      System.exit(1);
    }
    String bindingKey = argv[0];  
    String queueName = bindingKey + "Division"; 

    log.info(bindingKey + ":" + queueName);
    System.out.println(bindingKey + ":" + queueName);
    channel.queueDeclare(queueName, false, false, false, null);
    channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
    log.info(" [*] Waiting for messages. To exit press CTRL+C");

 //   Action.Mount();
 ////   Action.AddVolumes();
    MessageQueue.RecvMessage(channel, queueName);

  }
}