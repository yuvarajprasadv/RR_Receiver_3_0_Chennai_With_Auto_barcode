package Rcvr_AAMQ;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.util.Arrays;

import org.apache.log4j.Logger;


public class DJsonParser {
	static Logger log = DLogMQ.monitor("Rcvr_AAMQ.DJsonParser");
	public static JSONObject ParseJson(String jsonStr) throws Exception {
		try {
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonStr);
			return jsonObject;
		}
			catch (ParseException ex) {
				DThrowException.CustomExitWithErrorMsgID(new Exception("RoadRunner raised an exception "), "RoadRunner raised an exception", "14");
				log.error("Error at json parsing string : " + jsonStr +" - "+ ex.getMessage());
			}
			catch (NullPointerException ex) {
				DThrowException.CustomExitWithErrorMsgID(new Exception("RoadRunner raised an exception "), "RoadRunner raised an exception", "14");
				log.error(ex.getMessage());
			}
			catch(Exception ex)
			{
				DThrowException.CustomExitWithErrorMsgID(new Exception("RoadRunner raised an exception "), "RoadRunner raised an exception", "14");
				log.error(ex.getMessage());
			}
			
		return null;
	}
	
	public JSONObject ParseJsonFile(String filePath) throws Exception {
		try {
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(filePath));
			return jsonObject;
		}
			catch (ParseException ex) {
				DThrowException.CustomExitWithErrorMsgID(new Exception("RoadRunner raised an exception "), "RoadRunner raised an exception", "14");
				log.error("Error at json parsing string : " + ex.getMessage());
			}
			catch (NullPointerException ex) {
				DThrowException.CustomExitWithErrorMsgID(new Exception("RoadRunner raised an exception "), "RoadRunner raised an exception", "14");
				log.error(ex.getMessage());
			}
			catch(Exception ex)
			{
				DThrowException.CustomExitWithErrorMsgID(new Exception("RoadRunner raised an exception "), "RoadRunner raised an exception", "14");
				log.error(ex.getMessage());
			}
			
		return null;
	}

	public String getXmlFolderPathFromJson(JSONObject jsonObj, String keyString) throws Exception
	{
		DUtils utils = new DUtils();
		JSONArray array = (JSONArray)jsonObj.get("aaw");
		JSONObject jsonRegionArr = (JSONObject)array.get(0);
		String xmlFolderPath = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path") + (String) jsonRegionArr.get(keyString));
		if(!DUtils.IsFolderExists(xmlFolderPath))
		{
			DAction.UpdateErrorStatusWithRemark("23", "Folder path does not exists: " + xmlFolderPath);
			log.error(MessageQueue.WORK_ORDER + ": " + "Folder doesn't exists: " + xmlFolderPath);
	//		ThrowException.CustomExit(new Exception("Folder path does not exists "), "Folder path does not exists " + "--" + xmlFolderPath);
			DThrowException.CustomExitWithErrorMsgID(new Exception("Folder path does not exists "), xmlFolderPath.toString(), "23");
		}
		return xmlFolderPath;
	}
	
public String geFilePathFromJson(JSONObject jsonObj, String keyString) throws Exception
{
	DUtils utils = new DUtils();
	JSONArray array = (JSONArray)jsonObj.get("aaw");
	JSONObject jsonRegionArr = (JSONObject)array.get(0);
	String docFilePath = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path") + (String) jsonRegionArr.get(keyString));
	if(!utils.FileExists(docFilePath))
	{
		DAction.UpdateErrorStatusWithRemark("23", "Folder path does not exists: " + docFilePath);  //File not found error status to Tornado API
		log.error(MessageQueue.WORK_ORDER + ": " + "Folder or File doesn't exists: " + docFilePath);
	//	ThrowException.CustomExit(new Exception("Folder path or File does not exists "), "Folder path or file does not exists: "+docFilePath);
		DThrowException.CustomExitWithErrorMsgID(new Exception("Folder path does not exists "), docFilePath.toString(), "23");
		
	}
	return docFilePath;
}

public String[] getMultiPath(JSONObject jsonObj, String xmlFileName) throws NumberFormatException, Exception
{

	DUtils utils = new DUtils();
	String docFile[] = new String[4];
	String rtArray[] = new String[4];
    JSONArray array = (JSONArray)jsonObj.get("aaw");
  	JSONObject jsonRegionArr = (JSONObject)array.get(0);
	
	docFile[0] = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path") + (String) jsonRegionArr.get("XMLFile"));
	docFile[1] = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path") + (String) jsonRegionArr.get("Master"));
	docFile[2] = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path") +  "//080 QC//");
	docFile[3] = docFile[1];
	if(!utils.FileExists(docFile[2]))
		docFile[2] = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path") +  "//080_QC//");
	rtArray = utils.ArrayOfFileExists(docFile);
	if(rtArray[1] != "TRUE")
	{
		DAction.UpdateErrorStatusWithRemark("23", "Folder path does not exists: " + docFile[Integer.parseInt(rtArray[0])]);  //File not found error status to Tornado API
		log.error(MessageQueue.WORK_ORDER + ": " + "File path doesn't exists: " + docFile[Integer.parseInt(rtArray[0])]);
	//	ThrowException.CustomExit(new Exception("File Path or File does not exists "), "File path or file does not exists " + docFile[Integer.parseInt(rtArray[0])]);
		DThrowException.CustomExitWithErrorMsgID(new Exception("Folder path does not exists "), docFile[Integer.parseInt(rtArray[0])], "23");
	}
	docFile[2] += (getJsonValueForKey(jsonObj, "WO") + "_" + xmlFileName);
	String MasterFileName = docFile[3];
	MasterFileName = MasterFileName.substring(0, MasterFileName.length() - 3);  		
	docFile[3] = MasterFileName  + "_" + xmlFileName;

	return docFile;	
}

public String updateJsonForMultipleJob(JSONObject jsonObj, String xmlDirPath, String xmlFileName)
{
    JSONArray array = (JSONArray)jsonObj.get("aaw");
  	JSONObject jsonRegionArr = (JSONObject)array.get(0);
  	String xmlFilePath =  xmlDirPath + xmlFileName;
  	jsonRegionArr.put("XMLFile", xmlFilePath);
	return jsonObj.toJSONString();
}

public String updateJsonForMultipleJob(JSONObject jsonObj, String xmlDirPath, String xmlFileName, Boolean RR_ReRun3DXML, String fileName)
{
    JSONArray array = (JSONArray)jsonObj.get("aaw");
  	JSONObject jsonRegionArr = (JSONObject)array.get(0);
  	String xmlFilePath =  xmlDirPath + xmlFileName;
  	jsonRegionArr.put("XMLFile", xmlFilePath);
  	jsonRegionArr.put("Master", "050_Production_Art/" + fileName + ".ai");
  	
  	
  	array = (JSONArray)jsonObj.get("region");
  	jsonRegionArr = (JSONObject)array.get(0);
  	jsonRegionArr.put("ReRun3DXML", RR_ReRun3DXML);
  	
	return jsonObj.toJSONString();
}


public String getXmlDirPath(JSONObject jsonObj)
{
    JSONArray array = (JSONArray)jsonObj.get("aaw");
  	JSONObject jsonRegionArr = (JSONObject)array.get(0);
  	String xmlFilePath =  (String) jsonRegionArr.get("XMLFile");
  	return xmlFilePath;
}


	
public  String[] getPath(JSONObject jsonObj) throws NumberFormatException, Exception
{
	DUtils utils = new DUtils();
	String docFile[];
	String rtArray[] = new String[3];
	int preDefLength = 3;
    JSONArray array = (JSONArray)jsonObj.get("aaw");
  	JSONObject jsonRegionArr = (JSONObject)array.get(0);
	String xmlFiles = (String) jsonRegionArr.get("XMLFile");
	String[] xmlFileArray = xmlFiles.split(",");
	docFile = new String[xmlFileArray.length+preDefLength];
	docFile[0]="";
	docFile[1] = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path") + (String) jsonRegionArr.get("Master"));
	docFile[2] = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path") +  "//080 QC//");
	for (int eachXmlCount = 0; eachXmlCount < xmlFileArray.length; eachXmlCount++)
	{
		if(!xmlFileArray[eachXmlCount].contains("~"))//// to run single xml temp until tornado supports for multiple xmls
		{
			xmlFileArray[eachXmlCount] += "~0";
		}
		docFile[eachXmlCount+preDefLength] = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path") + (xmlFileArray[eachXmlCount].split("~"))[0]); //to check files exists
		docFile[0] += utils.RemoveForwardSlash((String) jsonRegionArr.get("Path") + xmlFileArray[eachXmlCount]);
		if (eachXmlCount != xmlFileArray.length-1)
		{
			docFile[0] += ",";	
		}
	}
	
	if(!utils.FileExists(docFile[2]))
		docFile[2] = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path") +  "//080_QC//");
	rtArray = utils.ArrayOfFileExists(docFile);
	if(rtArray[1] != "TRUE"){
		DAction.UpdateErrorStatusWithRemark("23", "File Path or File does not exists  " + docFile[Integer.parseInt(rtArray[0])]);
		log.error(MessageQueue.WORK_ORDER + ": " + "File path doesn't exists: " + docFile[Integer.parseInt(rtArray[0])]);
	//	ThrowException.CustomExit(new Exception("File Path or File does not exists "), "File path or file does not exists " + docFile[Integer.parseInt(rtArray[0])]);
		DThrowException.CustomExitWithErrorMsgID(new Exception("Folder path does not exists "), docFile[Integer.parseInt(rtArray[0])], "23");
	}
	docFile[2] += getJsonValueForKey(jsonObj, "WO");
	return docFile;	
}


//public String getMasterAIWithoutPathValidate(JSONObject jsonObj, String keyString)
//{
//	Utils utils = new Utils();
//	JSONArray array = (JSONArray)jsonObj.get("aaw");
//	JSONObject jsonRegionArr = (JSONObject)array.get(0);
//	String docFilePath = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path") + (String) jsonRegionArr.get(keyString));
//	return docFilePath;
//}

public String getJsonValueForKey(JSONObject jsonObj, String jsonKey)
{
	JSONArray array = (JSONArray)jsonObj.get("aaw");
  	JSONObject jsonRegionArr = (JSONObject)array.get(0);
	try{
		return (String) jsonRegionArr.get(jsonKey);
	}
	catch(Exception Ex)
	{
	   log.error(MessageQueue.WORK_ORDER + ": " + "Invalid Key value search '" + jsonKey+"'" + "  " + Ex.getMessage());	
	}
	return null;
}

public boolean getJsonBooleanValueForKey(JSONObject jsonObj,String jsonGroupKey, String jsonKey)
{
	JSONArray array = (JSONArray)jsonObj.get(jsonGroupKey);
  	JSONObject jsonRegionArr = (JSONObject)array.get(0);
	try{
		return (boolean) jsonRegionArr.get(jsonKey);
	}
	catch(Exception Ex)
	{
	   log.error(MessageQueue.WORK_ORDER + ": " + "Invalid Key value search '" + jsonKey+"'" + "  " + Ex.getMessage());	
	}
	return false;
}

public String getJsonValueFromGroupKey(JSONObject jsonObj, String jsonGroupKey, String jsonKey)
{
	DUtils utils = new DUtils();
	JSONArray array = (JSONArray)jsonObj.get(jsonGroupKey);
  	JSONObject jsonRegionArr = (JSONObject)array.get(0);
	try{
		return utils.RemoveForwardSlash((String) jsonRegionArr.get(jsonKey));
	}
	catch(Exception Ex)
	{
	   log.error(MessageQueue.WORK_ORDER + ": " + "Invalid Key value search '" + jsonGroupKey +", "+ jsonKey + "'" + "  " + Ex.getMessage());	
	}
	return null;
}

	public static void main(String[] args)
	{
		String jstring = "{\"isApplyRule\":false,\"aaw\":[{\"Path\":\"\\/Volumes\\/PG_HairCare_RTL_WIP\\/104169080\\/401990996\\/\",\"Master\":\"050_Production_Art\\/91180820.002_SHFP_VN.ai\",\"Revision\":\"01\",\"WO\":\"401990996\",\"XMLFile\":\"100_XML\\/A01\\/GS1_40199099601.xml\",\"MasterTemplate\":\"\\/Volumes\\/Tornado FMCG Library\\/PnG\\/Haircare\\/Pantene\\/PAN0002\\/Copy Template\\/\"}],\"source\":\"HUBX_RR\",\"Id\":\"747466bc-2803-4ce6-a3a8-054950ec4fc1\",\"region\":[{\"ProjectName\":\"RRPENANG_PnG_V001\",\"env\":\"production\",\"RR3DXML\":false,\"Location\":\"PENANG1PNG\"}],\"type\":\"none\",\"version\":\"CC 2018\"}";
		DJsonParser jsp = new DJsonParser();
		try {
			jsp.ParseJson(jstring);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}