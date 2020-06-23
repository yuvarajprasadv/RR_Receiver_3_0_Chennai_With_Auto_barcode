package Rcvr_AAMQ;

import java.util.Arrays;
import java.util.List;

import javax.script.ScriptEngine; 
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngineFactory;
import org.apache.log4j.Logger;

public class SEng{
	
	static Logger log = LogMQ.monitor("Rcvr_AAMQ.SEng");
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
			result = ex.getMessage();
			return result;
		}  
	}
	
	 public static  void CallAdobeIllustrator() throws Exception 
	 {
		 	String scriptString = "tell application "+ '"' + "Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app" +'"' + " \n with timeout of "+ timeOutSec +" seconds \n"
		 			+ "activate \n"
		 			+ "end timeout \n"
		 			+ "end tell \n "
		 			+ "return application";
		 	ExecuteAppleScript(scriptString);
	 }
	 public static String GetApplicationFonts() throws Exception
	 {
		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromResource("AppFonts.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+") \n"
			+ "end timeout \n"
			+ "end tell";
		 return ExecuteAppleScript(scriptString);
	 }
	 	 
	 public static void OpenDocument(String arryStr) throws Exception
	 {

		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromResource("PreDocument.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr+'"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 ExecuteAppleScript(scriptString);
	 }
	 
	 
	 public static String GetDocumentFonts() throws Exception
	 {
		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromResource("DocumentFonts.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+") \n"
			+ "end timeout \n"
			+ "end tell";
		 return ExecuteAppleScript(scriptString);
	 }
	 
	 public static String GetDocumentFiles() throws Exception
	 {   
		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromResource("DocumentFiles.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+") \n"
			+ "end timeout \n"
			+ "end tell";
		 return ExecuteAppleScript(scriptString);
	 }
	 
	 public static void DocumentPreProcess() throws Exception
	 {
		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromResource("FontMissing.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+ '"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+") \n"
			+ "end timeout \n"
			+ "end tell";
		 String fontMissing = ExecuteAppleScript(scriptString);
		 if (fontMissing != "")
		 {
			 System.out.println(fontMissing);
			 log.error(MessageQueue.WORK_ORDER + ": " + "Font Missing :" + fontMissing);
			 ThrowException.CatchException(new Exception("Font Missing"));
		 }
	 }

	 public static String CallTyphoonShadow(String arryStr[]) throws Exception
	 {
		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromResource("MainDocument.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+ '"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '\"'+arryStr[0]+'\"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 return ExecuteAppleScript(scriptString);
	 }
	 
	 public static void MergeSwatch(String arryStr[]) throws Exception  {

		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromResource("ColorSpace.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr[0]+'"' +", "+ '"'+arryStr[1]+'"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 ExecuteAppleScript(scriptString);
	 	}
	 
	 
	 public static void ApplyStyleOverFlow(String arryStr[]) throws Exception
	 {
		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromResource("ApplyStyleOverFlow.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr[0]+'"' +", "+ '"'+arryStr[1]+'"' +", "+ '"'+arryStr[2]+'"'  +"} \n"
			+ "end timeout \n"
			+"end tell";
		 ExecuteAppleScript(scriptString);
	 }
	 
	 public static void ApplyElementStyle(String arg[]) throws Exception
     {
		 XmlUtiility xmlUtls = new XmlUtiility();
		 Utils utils = new Utils();
		 String[] arryStr1 = new String[1];
	     arryStr1[0] = "0";
         
		 String pathStrings = utils.GetPathFromResource("ApplyStyle.js");
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
				    			String pathString = utils.GetPathFromResource("ApplyStyle.js");
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
		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromResource("PostDocument.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr[2]+'"' +", "+ '"'+arryStr[3]+'"' +", "+ '"'+arryStr[1]+'"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 ExecuteAppleScript(scriptString);
	 	}
	 
	 public static void PostDocumentProcessJPEG(String arryStr[]) throws Exception  {

		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromResource("PostDocumentJPEG.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr[2]+'"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 ExecuteAppleScript(scriptString);
	 	}
	 
	 public static void PostDocumentProcessForSingleJobFilename(String arryStr[]) throws Exception  {

		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromResource("PostDocumentWithFileName.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr[2]+'"' +", "+ '"'+arryStr[3]+'"'+", "+ '"'+arryStr[1]+'"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 ExecuteAppleScript(scriptString);
	 	}
	 
	 public static void PostDocumentProcessForSingleJobFilenameJPEG(String arryStr[]) throws Exception  {

		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromResource("PostDocumentWithFileNameJPEG.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr[2]+'"' +", "+ '"'+arryStr[3]+'"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 ExecuteAppleScript(scriptString);
	 	}
	 
	 public static String PostDocumentMultipleProcess(String arryStr[]) throws Exception
	 {
		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromResource("PostDocumentMultiple.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr[2]+'"' +", "+ '"'+arryStr[3]+'"' +", "+ '"'+MessageQueue.VERSION+'"'  +"} \n"
			+ "end timeout \n"
			+"end tell";
		return ExecuteAppleScript(scriptString);
	 }
	 
	 public static String PostDocMultiPDFPreset(String arryStr[], String pdfPresetArr[]) throws Exception
	 {
		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromResource("PostDocMultiPDFPreset.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr[2]+'"' +", "+ '"'+arryStr[3]+'"' +", "+ '"'+pdfPresetArr[0]+'"' +", "+ '"'+pdfPresetArr[1]+'"' +"} \n"
			+ "end timeout \n"
			+"end tell";
		return ExecuteAppleScript(scriptString);
	 }
	 
	 public static void PostDocumentClose() throws Exception
	 {
		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromResource("PostDocumentClose.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+") \n"
			+ "end timeout \n"
			+ "end tell";
		 ExecuteAppleScript(scriptString);
	 }
	 
	 public static void PostDocumentProcessOnError(String arryStr[]) throws Exception  {

		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromResource("PostDocumentOnError.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr[2]+'"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 ExecuteAppleScript(scriptString);
		 
	 	}
	 
	 public static String SetLegendVisibleOff() throws Exception  
	 {
		 String[] arryStr1 = new String[1];
		 arryStr1[0] = "false";
		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromResource("LegendVisibleOff.js");
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
		 String pathString = utils.GetPathFromResource("LayerOff.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arryStr1[0]+'"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 return ExecuteAppleScript(scriptString);
	 }
	 
	 public static void OnError() throws Exception  {

		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromResource("OnError.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+") \n"
			+ "end timeout \n"
			+ "end tell";
		 ExecuteAppleScript(scriptString);
	 }
		 
	 public static String MountVolume(String serverName, String userName, String userPass, String shareDirectory) throws Exception  
	 {	 

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
	 
	 
	 public static void ForTest(String arry) throws Exception  {

		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromResource("Test.js");
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+arry+'"' +","+ '"'+MessageQueue.VERSION+'"'  +"} \n"
			+ "end timeout \n"
			+"end tell";
		ExecuteAppleScript(scriptString);
	 }
	 
	 
	 public static void FindIllustratorVersion() throws Exception
	 {
		   FileSystem fls = new FileSystem();	
		   String scriptString = fls.ReadFile("/Users/yuvaraj/Desktop/Desktop 2/FindTheVersion.txt");
		   System.out.println(ExecuteAppleScript(scriptString)); 
	 }
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 
	 public static String ExecuteJS(String jsFile, String fileSavePath) throws Exception  {
		 Utils utils = new Utils();
		 String pathString = utils.GetPathFromResource(jsFile);
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+fileSavePath+'"' +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 return ExecuteAppleScriptForJS(scriptString);
	 	}
	 
	 public static String ExecuteJS(String jsFile, String fileSavePath, String[] jsProperties) throws Exception  {
		 Utils utils = new Utils();
		
		 String pathString = utils.GetPathFromResource(jsFile);
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+fileSavePath+'"' +", "+ Arrays.deepToString(jsProperties) +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 
		 return ExecuteAppleScriptForJS(scriptString);
	 	}
	 
	 public static String ExecuteJS(String jsFile, String fileSavePath, String[] jsProperties, String extraArgument) throws Exception  {
		 Utils utils = new Utils();
		
		 String pathString = utils.GetPathFromResource(jsFile);
		 String scriptString = "tell application "+ '"' +"Applications:Adobe Illustrator "+ MessageQueue.VERSION +":Adobe Illustrator.app"+'"' +"\n with timeout of "+ timeOutSec +" seconds \n"
			+ "do javascript (file "+'"'+pathString+'"'+")  with arguments {"+ '"'+fileSavePath+'"' +", "+ Arrays.deepToString(jsProperties) +", "+ extraArgument +"} \n"
			+ "end timeout \n"
			+ "end tell";
		 return ExecuteAppleScriptForJS(scriptString);
	 	}
	 
	 public static String ExportAsNormalPDF(String pdfSavePath, String jsonString) throws Exception  {
		 JSUtils jsUtils = new JSUtils();
		 String[] pdfProperties = {"acrobatLayers", "optimization"};
		 jsonString = "{ \"acrobatLayers\":\"true\", \"optimization\":true}";
		 
		 return ExecuteJS("JS_ExportAsNormalPDF.js", pdfSavePath, jsUtils.ExtractJsonToStringArray(pdfProperties, jsonString));
	 	}
	 
	 public static String ExportAsClipJPEG(String jpegSavePath, String jsonString) throws Exception  {
		 JSUtils jsUtils = new JSUtils();
		 String[] jpegProperties = {"antiAliasing", "qualitySetting", "artBoardClipping", "horizontalScale", "verticalScale"};
		 jsonString = "{ \"antiAliasing\":\"true\", \"qualitySetting\":35, \"artBoardClipping\":false,\"horizontalScale\":500, \"verticalScale\":500}";
		 
		 return ExecuteJS("JS_ExportAsClipJPEG.js", jpegSavePath, jsUtils.ExtractJsonToStringArray(jpegProperties, jsonString));
	 	}
	 
	 public static String ExportAsNormalJPEG(String jpegSavePath, String jsonString) throws Exception  {
		 JSUtils jsUtils = new JSUtils();
		 String[] jpegProperties = {"antiAliasing", "qualitySetting", "artBoardClipping", "horizontalScale", "verticalScale"};
		 jsonString = "{ \"antiAliasing\":\"true\", \"qualitySetting\":70, \"artBoardClipping\":true,\"horizontalScale\":100, \"verticalScale\":100}";
		 
		 return ExecuteJS("JS_ExportAsClipJPEG.js", jpegSavePath, jsUtils.ExtractJsonToStringArray(jpegProperties, jsonString));

	 	}
	 
	 
	 public static String ExportAsNormalisedPDF(String pdfSavePath, String jsonString) throws Exception  {
		 JSUtils jsUtils = new JSUtils();
		 String[] jpegProperties = {"antiAliasing", "qualitySetting", "artBoardClipping", "horizontalScale", "verticalScale"};
		 jsonString = "{ \"antiAliasing\":\"true\", \"qualitySetting\":70, \"artBoardClipping\":true,\"horizontalScale\":100, \"verticalScale\":100}";
		 
		 return ExecuteJS("JS_ExportAsClipJPEG.js", pdfSavePath, jsUtils.ExtractJsonToStringArray(jpegProperties, jsonString), MessageQueue.VERSION );

	 	}
	 
	 public static String SaveDocAs(String docSavePath, String jsonString) throws Exception  {
		 JSUtils jsUtils = new JSUtils();
		 String[] docProperties = {"fontSubsetThreshold", "pdfCompatible"};
		 jsonString = "{ \"fontSubsetThreshold\":0, \"pdfCompatible\":true}";

		 return ExecuteJS("JS_SaveDocAs.js", docSavePath, jsUtils.ExtractJsonToStringArray(docProperties, jsonString));
	 	}
	 
	 
	 public static String OutlineText() throws Exception
	 {
		 return ExecuteJS("OutlinePDF.js", "");
	 }
	 
	 
	 public static void main(String[] args) throws Exception
	 {
//		 String[] arryStr= new String[1];
//		 arryStr[0] = "Pantone 143 C";
//		 arryStr[1] = "Pantone 583 C";
//		 MessageQueue.VERSION  = "CC 2017";
//		 MergeSwatch(arryStr);
		 Thread.sleep(5000);
		 MessageQueue.VERSION  = "CC 2018";
		// ForTest("/Users/yuvaraj/Desktop/");
		 

		System.out.println( ExportAsNormalPDF("/Users/yuvaraj/Desktop/", ""));
		 
		 
	 }


}