package Rcvr_AAMQ;

import org.apache.log4j.Logger;

public class DThrowException {
	static Logger log = DLogMQ.monitor("Rcvr_AAMQ.DThrowException");
	
	public static void CatchException(Exception exp) throws Exception
	{
		try
		{
			throw exp; 
		}
		finally
		{
		System.out.println(exp.getMessage());
		DAction.sendStatusMsg((String) exp.getMessage());
		DAction.sendRespStatusMsg("exit on error");
		DSEng.OnError();
		//RR Error exit status
		Thread.sleep(5000);
		DAction.UpdateErrorStatusWithRemark("14", "Roadrunner exits on error ");//RR error exit (14) status to Tornado API
		log.info("Sent error status id 14:" + "Roadrunner exits on error");
		MessageQueue.WORK_ORDER = "";
		MessageQueue.GATE = true;
		}
	}
	
	public static void CatchExceptionWithErrorMsgId(Exception exp, String errorMsg, String id) throws Exception
	{
		try
		{
			throw exp; 
		}
		finally
		{
		System.out.println(exp.getMessage());
		DAction.sendStatusMsg((String) exp.getMessage());
		DAction.sendRespStatusMsg("exit on error");
		DSEng.OnError();
		//RR Error exit status
		Thread.sleep(5000);
		DAction.UpdateErrorStatusWithRemark(id, "Roadrunner exits on error " + errorMsg.toString());//RR error exit (14) status to Tornado API
		log.info("Sent error status id " + id + ": Roadrunner exits on error " + errorMsg.toString());
		MessageQueue.WORK_ORDER = "";
		MessageQueue.GATE = true;
		}
	}
	
	public static void CustomExit(Exception exp, String errorMsg) throws Exception
	{
		DFileSystem fls = new DFileSystem();
		try
		{
			throw exp; 
		}
		finally
		{
		System.out.println(errorMsg);
		fls.AppendFileString("Error :"+ errorMsg.toString()+"\n\n");
		DAction.sendStatusMsg(errorMsg);
		DAction.sendRespStatusMsg("exit on error");
		DSEng.OnError();
		//RR Error exit status
		Thread.sleep(5000);
		DAction.UpdateErrorStatusWithRemark("14", "Roadrunner exits on error: " +  errorMsg.toString());//RR error exit (14) status to Tornado API
		log.info("Sent error status id 14:" + "Roadrunner exits on error " + errorMsg.toString());
		MessageQueue.GATE = true;
		}
	}
	
	public static void CustomExitWithErrorMsgID(Exception exp, String errorMsg, String id) throws Exception
	{
		DFileSystem fls = new DFileSystem();
		try
		{
			throw exp; 
		}
		finally
		{
		System.out.println(errorMsg);
		fls.AppendFileString("Error :"+ errorMsg.toString()+"\n\n");
		DAction.sendStatusMsg(errorMsg);
		DAction.sendRespStatusMsg("exit on error");
		DSEng.OnError();
		//RR Error exit status
		Thread.sleep(5000);
		DAction.UpdateErrorStatusWithRemark(id, errorMsg.toString());
		log.info("Sent error status id "+ id +":" + errorMsg.toString());
		MessageQueue.GATE = true;
		}
	}
	

}
