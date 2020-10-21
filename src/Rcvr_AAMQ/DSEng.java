package Rcvr_AAMQ;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine; 
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngineFactory;
import org.apache.log4j.Logger;

public class DSEng{
	
	static Logger log = DLogMQ.monitor("Rcvr_AAMQ.DSEng");
	static String timeOutSec = "600";
	 
	public static  String ExecuteAppleScript(String appleString) throws Exception
	{
		
		try
		{
			ScriptEngineManager mgr = new ScriptEngineManager();
			List<ScriptEngineFactory> factories =
		            mgr.getEngineFactories();
			 for (ScriptEngineFactory factory : factories) 
			 {
			        List<String> extensions = factory.getExtensions(); 
		            for (String ext : extensions) 
		            { 
		                mgr.registerEngineExtension(ext, factory); 
		            }
			        
		            List<String> mimes = factory.getMimeTypes(); 
		            for (String mime : mimes) 
		            { 
		            		mgr.registerEngineExtension(mime, factory); 
		            }
			 }
	        ScriptEngine engine = mgr.getEngineByName("AppleScriptEngine");
	        String result = null;
	        result = (engine.eval(appleString)).toString();
	        return result;
		}
		catch(Exception ex)
		{
		//	log.error(MessageQueue.WORK_ORDER + ": " + "Error on running script enginge: " + ex.getMessage());
			return ex.getMessage();
		}
		
        
	}
	
	public static  String ExecuteAppleScriptForJS(String appleScriptString) throws Exception
	{
		String result = null;
		try
		{
			ScriptEngineManager mgr = new ScriptEngineManager();
			List<ScriptEngineFactory> factories =
			        mgr.getEngineFactories();
			 for (ScriptEngineFactory factory : factories) 
			 {
			        List<String> extensions = factory.getExtensions(); 
			        for (String ext : extensions) 
			        { 
			            mgr.registerEngineExtension(ext, factory); 
			        }
			        
			        List<String> mimes = factory.getMimeTypes(); 
			        for (String mime : mimes) 
			        { 
			        		mgr.registerEngineExtension(mime, factory); 
			        }
			 }
			ScriptEngine engine = mgr.getEngineByName("AppleScriptEngine");
			result = (engine.eval(appleScriptString)).toString();
			return result;
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
			result = ex.getMessage();
			return result;
		}  
	}
	
	 public static  void CallAdobeIllustrator() throws Exception 
	 {
		 try
		 {
		 	String scriptString = "tell application "+ '"' + "Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app" +'"' + " \n with timeout of "+ timeOutSec +" seconds \n"
		 			+ "activate \n"
		 			+ "end timeout \n"
		 			+ "end tell \n "
		 			+ "return application";
		 	ExecuteAppleScript(scriptString);
		 }
		 catch(Exception ex)
		 {
			 log.error(MessageQueue.WORK_ORDER + ": " + "Issue on launching illustrator " + ex.getMessage());
			 log.info(MessageQueue.WORK_ORDER + ": " + "Error Status updated as 14 - Roadrunner exits on error");
			 DThrowException.CatchExceptionWithErrorMsgId(new Exception("Illustrator"), "Roadrunner exits on error", "14");
		 }
	 }
	 public static String GetApplicationFonts() throws Exception
	 {
		 DUtils utils = new DUtils();
		 String pathString = utils.GetPathFromEnvResource("AppFonts.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+") \n"
			+ "end timeout \n"
			+ "end tell";
		 return ExecuteAppleScript(scriptString);
	 }
	 	 
	 public static void OpenDocument(String arryStr) throws Exception
	 {

		 DUtils utils = new DUtils();
		 String pathString = utils.GetPathFromEnvResource("PreDocument.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr+'"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 ExecuteAppleScript(scriptString);
	 }
	 
	 
	 public static String GetDocumentFonts() throws Exception
	 {
		 DUtils utils = new DUtils();
		 String pathString = utils.GetPathFromEnvResource("DocumentFonts.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+") \n"
			+ "end timeout \n"
			+ "end tell";
		 return ExecuteAppleScript(scriptString);
	 }
	 
	 public static String GetDocumentFiles() throws Exception
	 {   
		 DUtils utils = new DUtils();
		 String pathString = utils.GetPathFromEnvResource("DocumentFiles.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+") \n"
			+ "end timeout \n"
			+ "end tell";
		 return ExecuteAppleScript(scriptString);
	 }
	 
	 public static void DocumentPreProcess() throws Exception
	 {
		 DUtils utils = new DUtils();
		 String pathString = utils.GetPathFromEnvResource("FontMissing.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+ '"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+") \n"
			+ "end timeout \n"
			+ "end tell";
		 String fontMissing = ExecuteAppleScript(scriptString);
		 if (fontMissing != "")
		 {
			 System.out.println(fontMissing);
			 log.error(MessageQueue.WORK_ORDER + ": " + "Font Missing :" + fontMissing);
			 DThrowException.CatchException(new Exception("Font Missing"));
		 }
	 }

	 public static String CallTyphoonShadow(String arryStr[]) throws Exception
	 {
		 DUtils utils = new DUtils();
		 String pathString = utils.GetPathFromEnvResource("MainDocument.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+ '"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '\"'+arryStr[0]+'\"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 return ExecuteAppleScript(scriptString);
	 }
	 
	 public static void SetSwathColorFromTo(String swatchFrom, String swatchTo) throws Exception  {

		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "tell application \"Adobe Illustrator\"'s document 1 \n"
			+ "try \n"
			+ "set spot "+ '"' + swatchFrom + '"' + "'s color to spot " +'"' + swatchTo + '"' +"'s color" + "\n"
			+ "delay \n"
			+ "end try \n"
			+ "end tell \n"
			+ "end timeout \n"
			+ "end tell";
		// System.out.println(scriptString);
		 ExecuteAppleScript(scriptString);
	 	}
	 
	 
	 public static void MergeSwatch(String arryStr[]) throws Exception  {

		 DUtils utils = new DUtils();
		 String pathString = utils.GetPathFromEnvResource("ColorSpace.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr[0]+'"' +", "+ '"'+arryStr[1]+'"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 ExecuteAppleScript(scriptString);
	 	}
	 
	 public static String SwatchTest() throws Exception  
	 {

		 try
		 {
		 DUtils utils = new DUtils();
		 String pathString = utils.GetPathFromEnvResource("GetSwatchList.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
				 + "do javascript (file "+'"'+pathString+'"'+") \n"
				 + "end timeout \n"
				 + "end tell";
		 return ExecuteAppleScript(scriptString);
		 }
		 catch (Exception ex)
		 {
			 log.error(MessageQueue.WORK_ORDER + ": " + "Failed to apply Swatch color merge");
			 return null;
		 }
		
	 }
	 
	 public static String SetLayerVisibleOff() throws Exception  
	 {
		 String[] arryStr1 = new String[1];
		 arryStr1[0] = "none";
		 DUtils utils = new DUtils();
		 String pathString = utils.GetPathFromEnvResource("LayerOff.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr1[0]+'"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 return ExecuteAppleScript(scriptString);
	 }
	 
	 public static void ApplyStyleOverFlow(String arryStr[]) throws Exception
	 {
		 DUtils utils = new DUtils();
		 String pathString = utils.GetPathFromEnvResource("ApplyStyleOverFlow.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr[0]+'"' +", "+ '"'+arryStr[1]+'"' +", "+ '"'+arryStr[2]+'"'  +"} \n"
			+ "end timeout \n"
			+"end tell";
		 ExecuteAppleScript(scriptString);
	 }
	 
	 public static void ApplyElementStyle(String arg[]) throws Exception
     {
		 DXmlUtiility xmlUtls = new DXmlUtiility();
		 DUtils utils = new DUtils();
		 String[] arryStr1 = new String[1];
	     arryStr1[0] = "0";
         
		 String pathStrings = utils.GetPathFromEnvResource("ApplyStyle.js");
		 String scriptStrings = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathStrings+'"'+")  with arguments {"+ '"'+arryStr1[0]+'"' +"} \n"
			+ "end timeout \n"
			+ "end tell";

		   String arrCopyElements = "";
		   arrCopyElements = ExecuteAppleScript(scriptStrings);
		   System.out.println("COPY EL  : "+ arrCopyElements + "\n");
		   for(String eachElement:(arrCopyElements.split(",")))
		   {  
			   String copyElement = eachElement.toString();
			   System.out.println("COPY EL  : "+ copyElement + "    "+arg[0]+  "\n");
			   String[] elements = copyElement.split("~");
			    for(String linkID:elements)
			    {
				    	if(linkID.length() > 3)
				    	{
				    		String stn = "";
				    		stn = xmlUtls.GS1XmlParseElement(arg[0], linkID.toString());
				    		System.out.println(linkID +"::"+stn);
				    		if (stn != null)
				    		{
				    			String pathString = utils.GetPathFromEnvResource("ApplyStyle.js");
					   		 	String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
					   			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+"1"+", "+ '"'+linkID+'"' +", "+ '"'+stn+'"' +", "+  '"'+copyElement+'"' +"} \n"
					   			+ "end timeout \n"
					   			+"end tell";
					   		 	System.out.println(ExecuteAppleScript(scriptString));
				    		}
				   		
				    	}
			    	
			    }
		  }
 
     }
	 
	 public static void PostDocumentProcess(String arryStr[]) throws Exception  {

		 DUtils utils = new DUtils();
		 String pathString = utils.GetPathFromEnvResource("PostDocument.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr[2]+'"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 ExecuteAppleScript(scriptString);
	 	}
	 
	 public static void PostDocumentProcessForSingleJobFilename(String arryStr[]) throws Exception  {

		 DUtils utils = new DUtils();
		 String pathString = utils.GetPathFromEnvResource("PostDocumentWithFileName.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr[2]+'"' +", "+ '"'+arryStr[3]+'"' +", "+ '"'+MessageQueue.VERSION+'"'  +", "+ '"'+arryStr[1]+'"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 ExecuteAppleScript(scriptString);
	 	}
	 public static void PostDocumentProcessFor3DXML(String arryStr[]) throws Exception  {

		 DUtils utils = new DUtils();
		 String pathString = utils.GetPathFromEnvResource("PostDocumentFor3D.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr[2]+'"' +", "+ '"'+arryStr[3]+'"' +", "+ '"'+arryStr[1]+'"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 ExecuteAppleScript(scriptString);
	 	}
	 
	 public static String PostDocumentMultipleProcess(String arryStr[]) throws Exception
	 {
		 DUtils utils = new DUtils();
		 String pathString = utils.GetPathFromEnvResource("PostDocumentMultiple.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr[2]+'"' +", "+ '"'+arryStr[3]+'"' +", "+ '"'+MessageQueue.VERSION+'"'  +", "+ '"'+arryStr[1]+'"' +"} \n"
			+ "end timeout \n"
			+"end tell";
		return ExecuteAppleScript(scriptString);
	 }
	 
	 public static String PostDocMultiPDFPreset(String arryStr[], String pdfPresetArr[]) throws Exception
	 {
		 DUtils utils = new DUtils();
		 String pathString = utils.GetPathFromEnvResource("PostDocMultiPDFPreset.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr[2]+'"' +", "+ '"'+arryStr[3]+'"' +", "+ '"'+pdfPresetArr[0]+'"' +", "+ '"'+pdfPresetArr[1]+'"' +"} \n"
			+ "end timeout \n"
			+"end tell";
		return ExecuteAppleScript(scriptString);
	 }
	 
	 public static void PostDocumentClose() throws Exception
	 {
		 DUtils utils = new DUtils();
		 String pathString = utils.GetPathFromEnvResource("PostDocumentClose.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+") \n"
			+ "end timeout \n"
			+ "end tell";
		 ExecuteAppleScript(scriptString);
	 }
	 
	 public static String SetLegendVisibleOff(String legendVisible) throws Exception  
	 {
		 String[] arryStr1 = new String[1];
		 arryStr1[0] = legendVisible;
		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromEnvResource("LegendVisibleOff.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr1[0]+'"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 return ExecuteAppleScript(scriptString);
	 }
	 
	 
	 public static String SetLayerVisibleOff(String barCodeVisible) throws Exception  
	 {
		 String[] arryStr1 = new String[1];
		 arryStr1[0] = barCodeVisible;
		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromEnvResource("TUCLayerOff.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr1[0]+'"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 return ExecuteAppleScript(scriptString);
	 }
	 public static void PostDocumentProcessOnError(String arryStr[]) throws Exception  {

		 DUtils utils = new DUtils();
		 String pathString = utils.GetPathFromEnvResource("PostDocumentOnError.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr[2]+'"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 ExecuteAppleScript(scriptString);
		 
	 	}
	 
	 public static String ExecuteIllustratorActions(String arryStr[]) throws Exception  {

		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "tell application "+'"'+"Adobe Illustrator"+'"'+"'s document 1 \n"
			+ "set spot "+ '"'+ arryStr[0] +'"'+"'s color to spot "+ '"'+ arryStr[1] +'"'+"'s color \n"
			+ "delay 1 \n"
			+ "do script "+'"'+ "Select All Unused"+'"'+" from "+'"'+ "Merge Swatches"+'"'+" without dialogs \n"
			+ "delete swatch "+'"'+arryStr[1]+'"'+" \n"
			+ "delay 1 \n"
			+ "set name of swatch " +'"' + arryStr[0] + '"' +  " to " + '"' + arryStr[1] + '"' + " \n"
			+ "end tell \n"
			+ "end timeout \n"
			+ "end tell";
		return ExecuteAppleScript(scriptString);
		 
	 	}
	 
	 /*
	  	 public static String ExecuteIllustratorActions(String arryStr[]) throws Exception  {

		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "tell application "+'"'+"Adobe Illustrator"+'"'+"'s document 1 \n"
			+ "set spot "+ '"'+ arryStr[0] +'"'+"'s color to spot "+ '"'+ arryStr[1] +'"'+"'s color \n"
			+ "do script "+'"'+ "Select All Unused"+'"'+" from "+'"'+ "Merge Swatches"+'"'+" without dialogs \n"
			+ "delete swatch "+'"'+arryStr[1]+'"'+"\n"
			+ "end tell \n"
			+ "end timeout \n"
			+ "end tell";
		return ExecuteAppleScript(scriptString);
		 
	 	} 
	  
	  */
	 
	 public static void OnError() throws Exception  {

		 DUtils utils = new DUtils();
		 String pathString = utils.GetPathFromEnvResource("OnError.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+") \n"
			+ "end timeout \n"
			+ "end tell";
		 ExecuteAppleScript(scriptString);
	 }
		 
	 public static String MountVolume(String serverName, String userName, String userPass, String shareDirectory) throws Exception  
	 {	 
		 userName = "asia\\\\chautoscript";
		 userPass = "TeamI#ndi@";
		String scriptString = "tell application \"Finder\" \n"
		+ "with timeout of "+ timeOutSec +" seconds \n"
		+ "set serverName to "+ '"' +serverName + '"' +" \n"
		+ "set userName to  "+ '"' + userName + '"' +"  \n"
		+ "set userPass to "+ '"' + userPass + '"' +" \n"
		+ "set shareDirectory to "+ '"' + shareDirectory + '"' +" \n"
		+ "set networkSmbString to \"smb://\" & serverName & \"/\" & shareDirectory \n"
			+ "if not (disk shareDirectory exists) then \n"
					+ "try \n"
				 		+ "mount volume networkSmbString as user name userName with password userPass \n"
				 		+ "on error errtext number errnum \n"
				 		+ "if errnum = -55 then \n"
				 			+ "return \"Error mounting on volume \" & shareDirectory \n"
				 		+ "else \n"
				 			+ "return \"Error mounting on volume \" & shareDirectory \n"
				 		+ "end if \n"
				 	+ "end try \n"
			 	+ "end if \n" 
			 	+ "end timeout \n"
				+ "end tell \n" 
				+ "return \"Volume mounted: \" &shareDirectory ";
		return ExecuteAppleScript(scriptString); 
		 
	 	}
	 
	 public static void FindIllustratorVersion() throws Exception
	 {
		   DFileSystem fls = new DFileSystem();	
		   String scriptString = fls.ReadFile("/Users/yuvaraj/Desktop/Desktop 2/FindTheVersion.txt");
		   System.out.println(ExecuteAppleScript(scriptString)); 
	 }
	 
	 
	 public static String ExecuteJS(String jsFile, String fileSavePath) throws Exception  {
		 DUtils utils = new DUtils();
		 String pathString = utils.GetPathFromEnvResource(jsFile);
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+fileSavePath+'"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 return ExecuteAppleScriptForJS(scriptString);
	 	}
	 
	 public static String ExecuteJS(String jsFile, String fileSavePath, String[] jsProperties) throws Exception  {
		 DUtils utils = new DUtils();
		
		 String pathString = utils.GetPathFromEnvResource(jsFile);
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+fileSavePath+'"' +", "+ Arrays.deepToString(jsProperties) +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 
		 return ExecuteAppleScriptForJS(scriptString);
	 	}
	 
	 public static String ExecuteJS(String jsFile, String fileSavePath, String[] jsProperties, String extraArgument) throws Exception  {
		 DUtils utils = new DUtils();
		
		 String pathString = utils.GetPathFromEnvResource(jsFile);
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+fileSavePath+'"' +", "+ Arrays.deepToString(jsProperties) +", "+ '"'+extraArgument+'"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 return ExecuteAppleScriptForJS(scriptString);
	 	}
	 
	 public static String ExportAsNormalPDF(String pdfSavePath, String jsonString) throws Exception  {
		 DJSUtils jsUtils = new DJSUtils();
		 String[] pdfProperties = {"acrobatLayers", "optimization", "preserveEditability"};
		 if(jsonString == "")
			 jsonString = "{ \"acrobatLayers\":\"true\", \"optimization\":true, \"preserveEditability\":true}";
		 
		 return ExecuteJS("JS_ExportAsNormalPDF.js", pdfSavePath, jsUtils.ExtractJsonToStringArray(pdfProperties, jsonString));
	 	}
	 
	 public static String ExportAsClipJPEG(String jpegSavePath, String jsonString) throws Exception  {
		 DJSUtils jsUtils = new DJSUtils();
		 String[] jpegProperties = {"antiAliasing", "qualitySetting", "artBoardClipping", "horizontalScale", "verticalScale", "optimization"};
		// jsonString = "{ \"antiAliasing\":\"true\", \"qualitySetting\":35, \"artBoardClipping\":false,\"horizontalScale\":500, \"verticalScale\":500}";
		 jsonString = "{ \"antiAliasing\":\"true\", \"qualitySetting\":100, \"artBoardClipping\":true,\"horizontalScale\":100, \"verticalScale\":100, \"optimization\":true}";
		 
		 return ExecuteJS("JS_ExportAsClipJPEG.js", jpegSavePath, jsUtils.ExtractJsonToStringArray(jpegProperties, jsonString));
	 	}
	 
	 public static String ExportAsNormalJPEG(String jpegSavePath, String jsonString) throws Exception  {
		 DJSUtils jsUtils = new DJSUtils();
		 String[] jpegProperties = {"antiAliasing", "qualitySetting", "artBoardClipping", "horizontalScale", "verticalScale", "optimization"};
		 if(jsonString == "")
			 jsonString = "{ \"antiAliasing\":\"true\", \"qualitySetting\":70, \"artBoardClipping\":true,\"horizontalScale\":100, \"verticalScale\":100, \"optimization\":true}";
		 
		 return ExecuteJS("JS_ExportAsClipJPEG.js", jpegSavePath, jsUtils.ExtractJsonToStringArray(jpegProperties, jsonString));

	 	}
	 
	 
	 
//	 public static String ExportAsPNG8(String png8SavePath, String jsonString) throws Exception  {
//		 JSUtils jsUtils = new JSUtils();
//		 String[] png8Properties = {"antiAliasing", "artBoardClipping", "horizontalScale", "verticalScale", "transparency", "saveAsHTML"};
//		 jsonString = "{ \"antiAliasing\":\"false\", \"artBoardClipping\":\"false\", \"horizontalScale\":500, \"verticalScale\":500, \"transparency\":\"true\", \"saveAsHTML\":\"false\"}";
//		 
//		 return ExecuteJS("JS_ExportAsPNG8.js", png8SavePath, jsUtils.ExtractJsonToStringArray(png8Properties, jsonString));
//
//	 	}
	 
	 
	 public static String ExportAsPNG(String png24SavePath, String jsonString) throws Exception  {
		 DJSUtils jsUtils = new DJSUtils();
		 String[] png24Properties = { "type", "antiAliasing", "artBoardClipping", "horizontalScale", "verticalScale", "transparency", "saveAsHTML"};
		 if(jsonString == "")
			 jsonString = "{\"type\":24, \"antiAliasing\":\"false\", \"artBoardClipping\":\"false\", \"horizontalScale\":500, \"verticalScale\":500, \"transparency\":\"true\", \"saveAsHTML\":\"false\"}";
		 
		 return ExecuteJS("JS_ExportAsPNG.js", png24SavePath, jsUtils.ExtractJsonToStringArray(png24Properties, jsonString));

	 	}
	 
	 public static String ExportAsPSD(String psdSavePath, String jsonString) throws Exception  {
		 DJSUtils jsUtils = new DJSUtils();
		 String[] psdProperties = {"antiAliasing", "resolution", "editableText"};
		 if(jsonString == "")
			 jsonString = "{ \"antiAliasing\":true, \"resolution\":300, \"editableText\":true}";
		 
		 return ExecuteJS("JS_ExportAsPSD.js", psdSavePath, jsUtils.ExtractJsonToStringArray(psdProperties, jsonString));

	 	}
	 
	 public static String ExportAsGIF(String gifSavePath, String jsonString) throws Exception  {
		 DJSUtils jsUtils = new DJSUtils();
		 String[] gifProperties = {"antiAliasing", "artBoardClipping", "horizontalScale", "verticalScale", "transparency", "saveAsHTML"};
		 if(jsonString == "")
			 jsonString = "{ \"antiAliasing\":true, \"artBoardClipping\":false,\"horizontalScale\":100, \"verticalScale\":100, \"transparency\":true, \"saveAsHTML\":true}";
		 
		 return ExecuteJS("JS_ExportAsGIF.js", gifSavePath, jsUtils.ExtractJsonToStringArray(gifProperties, jsonString));

	 	}
	 
	 public static String ExportAsFLASH(String flashSavePath, String jsonString) throws Exception  {
		 DJSUtils jsUtils = new DJSUtils();
		 String[] flashProperties = {"resolution", "convertTextToOutlines"};
		 jsonString = "{ \"resolution\":300, \"convertTextToOutlines\":false}";
		 
		 return ExecuteJS("JS_ExportAsFLASH.js", flashSavePath, jsUtils.ExtractJsonToStringArray(flashProperties, jsonString));

	 	}
	 
	 
	 public static String ExportAsTIFF(String tiffSavePath, String jsonString) throws Exception  {
		 DJSUtils jsUtils = new DJSUtils();
		 String[] tiffProperties = {"resolution", "antiAliasing", "IZWCompression"};
		 if(jsonString == "")
			 jsonString = "{\"resolution\":150, \"antiAliasing\":true, \"IZWCompression\":false}";
		 
		 return ExecuteJS("JS_ExportAsTIFF.js", tiffSavePath, jsUtils.ExtractJsonToStringArray(tiffProperties, jsonString));

	 	}
	 
	 
	 public static String ExportAsNormalisedPDF(String normalizedPDFSavePath, String jsonString) throws Exception  {
		 DJSUtils jsUtils = new DJSUtils();
		 String[] normalisedPDFProperties = {"embedImages", "addPreview", "copyImages", "copyImagesNotOnServers", "fitMediaBoxToArtwork", "expandPatterns", "contourizeBitmaps", "outlineText", "includeHiddenObjectsAndLayers", "includeNotes", "blendResolution", "border_Mode"};
		 jsonString = "{ \"embedImages\":false, \"addPreview\":false, \"copyImages\":false, \"copyImagesNotOnServers\":false, \"fitMediaBoxToArtwork\":false, \"expandPatterns\":false, \"contourizeBitmaps\":false, \"outlineText\":false, \"includeHiddenObjectsAndLayers\":true, \"includeNotes\":true, \"blendResolution\":600, \"border_Mode\":3}";
		 

		 
		 return ExecuteJS("JS_ExportAsNormalisedPDF.js", normalizedPDFSavePath, jsUtils.ExtractJsonToStringArray(normalisedPDFProperties, jsonString), MessageQueue.VERSION);

	 	}
						 
																		// 	scripter.embedImages = pdfProperties[0];
																	// 	scripter.addPreview   = pdfProperties[1];
																	//		scripter.copyImages   = pdfProperties[2];
																	//		scripter.copyImagesNotOnServers = pdfProperties[3];
																	//		scripter.fitMediaBoxToArtwork = pdfProperties[4];
																	//		scripter.expandPatterns = pdfProperties[5];
																	//		scripter.contourizeBitmaps = pdfProperties[6];
																	//		scripter.outlineText = pdfProperties[7];
																	//		scripter.includeHiddenObjectsAndLayers = pdfProperties[8];
																	//		scripter.includeNotes = pdfProperties[9];
																	// 	scripter.blendResolution = pdfProperties[10];  // default 600
																	// 	scripter.borderMode = pdfProperties[11];  // default 3         	
 
 
 
	 
	 public static String Export_DocumentAI(String docSavePath, String jsonString) throws Exception  {
		 DJSUtils jsUtils = new DJSUtils();
		 String[] docProperties = {"fontSubsetThreshold", "pdfCompatible"};
		 jsonString = "{ \"fontSubsetThreshold\":0, \"pdfCompatible\":true}";

		 return ExecuteJS("JS_SaveDocAs.js", docSavePath, jsUtils.ExtractJsonToStringArray(docProperties, jsonString));
	 	}
	 
	 
	 public static String OutlineText() throws Exception
	 {
		 return ExecuteJS("OutlinePDF.js", "");
	 }

	 public static String EmbedPlacedItems() throws Exception
	 {
		 return ExecuteJS("JS_EmbedPlacedItems.js", "");
	 }
	 
	 
	 public static void main(String[] args) throws Exception
	 {
		 MessageQueue.VERSION = "CC 2018";
		 
		 DSEng.ExportAsTIFF("/Users/yuvaraj/Desktop/test.tiff", "");
	 }


}