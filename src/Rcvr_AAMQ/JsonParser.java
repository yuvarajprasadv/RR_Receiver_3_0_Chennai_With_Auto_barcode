package Rcvr_AAMQ;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Arrays;

import org.apache.log4j.Logger;


public class JsonParser {
	static Logger log = LogMQ.monitor("Rcvr_AAMQ.JsonParser");
	public static JSONObject ParseJson(String jsonStr) {
		try {
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonStr);
			return jsonObject;
		} catch (ParseException ex) {
			log.error(ex.getMessage());
		} catch (NullPointerException ex) {
			log.error(ex.getMessage());
		}
		return null;
	}

	public String getXmlFolderPathFromJson(JSONObject jsonObj, String keyString) throws Exception
	{
		Utils utils = new Utils();
		JSONArray array = (JSONArray)jsonObj.get("aaw");
		JSONObject jsonRegionArr = (JSONObject)array.get(0);
		String xmlFolderPath = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path") + (String) jsonRegionArr.get(keyString));
		if(!Utils.IsFolderExists(xmlFolderPath))
		{
			log.error(MessageQueue.WORK_ORDER + ": " + "Folder doesn't exists: " + xmlFolderPath);
			ThrowException.CustomExit(new Exception("Folder path does not exists "), "Folder path does not exists " + "--" + xmlFolderPath);
		}
		return xmlFolderPath;
	}
	
public String geFilePathFromJson(JSONObject jsonObj, String keyString) throws Exception
{
	Utils utils = new Utils();
	JSONArray array = (JSONArray)jsonObj.get("aaw");
	JSONObject jsonRegionArr = (JSONObject)array.get(0);
	String docFilePath = "";
	if(keyString != "")
		docFilePath = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path") + (String) jsonRegionArr.get(keyString));
	else
		docFilePath = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path"));
	if(!utils.FileExists(docFilePath))
	{
		log.error(MessageQueue.WORK_ORDER + ": " + "Folder or File doesn't exists: " + docFilePath);
		ThrowException.CustomExit(new Exception("Folder path or File does not exists "), "Folder path or file does not exists ");
	}
	return docFilePath;
}

public String[] getMultiPath(JSONObject jsonObj, String xmlFileName) throws NumberFormatException, Exception
{

	Utils utils = new Utils();
	String docFile[] = new String[4];
	String rtArray[] = new String[4];
    JSONArray array = (JSONArray)jsonObj.get("aaw");
  	JSONObject jsonRegionArr = (JSONObject)array.get(0);
	
//	docFile[0] = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path") + (String) jsonRegionArr.get("XMLFile")) + xmlFileName;
	docFile[0] = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path") + (String) jsonRegionArr.get("XMLFile"));
	docFile[1] = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path") + (String) jsonRegionArr.get("Master"));
	docFile[2] = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path") +  "//080 QC//");
	docFile[3] = docFile[1];
	if(!utils.FileExists(docFile[2]))
		docFile[2] = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path") +  "//080_QC//");
	rtArray = utils.ArrayOfFileExists(docFile);
	if(rtArray[1] != "TRUE"){
		log.error(MessageQueue.WORK_ORDER + ": " + "File path doesn't exists: " + docFile[Integer.parseInt(rtArray[0])]);
		ThrowException.CustomExit(new Exception("File Path or File does not exists "), "File path or file does not exists " + docFile[Integer.parseInt(rtArray[0])]);
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

public String getXmlDirPath(JSONObject jsonObj)
{
    JSONArray array = (JSONArray)jsonObj.get("aaw");
  	JSONObject jsonRegionArr = (JSONObject)array.get(0);
  	String xmlFilePath =  (String) jsonRegionArr.get("XMLFile");
  	return xmlFilePath;
}


	
public  String[] getPath(JSONObject jsonObj) throws NumberFormatException, Exception
{

	Utils utils = new Utils();
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
//	utils.XmlMultiFileExists(docFile[0]);
	if(!utils.FileExists(docFile[2]))
		docFile[2] = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path") +  "//080_QC//");
	rtArray = utils.ArrayOfFileExists(docFile);
	if(rtArray[1] != "TRUE"){
		log.error(MessageQueue.WORK_ORDER + ": " + "File path doesn't exists: " + docFile[Integer.parseInt(rtArray[0])]);
		ThrowException.CustomExit(new Exception("File Path or File does not exists "), "File path or file does not exists " + docFile[Integer.parseInt(rtArray[0])]);
	}
	docFile[2] += getJsonValueForKey(jsonObj, "WO");
	return docFile;	
}

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

public String getJsonValueFromGroupKey(JSONObject jsonObj, String jsonGroupKey, String jsonKey)
{
	Utils utils = new Utils();
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


public String getMasterAIWithoutPathValidate(JSONObject jsonObj, String keyString)
{
	Utils utils = new Utils();
	JSONArray array = (JSONArray)jsonObj.get("aaw");
	JSONObject jsonRegionArr = (JSONObject)array.get(0);
	String docFilePath = utils.RemoveForwardSlash((String) jsonRegionArr.get("Path") + (String) jsonRegionArr.get(keyString));
	return docFilePath;
}


	public static void main(String[] args)
	{
		
		String arxmlFileArray = "alksjfda,aslfdkjl";
		System.out.println(Arrays.toString(arxmlFileArray.split(",")));
	}
}