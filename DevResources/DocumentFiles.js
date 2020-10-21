function getUsedFiles(doc){	try	{	     var xmlString = new XML(doc.XMPString);	     var fileRef = xmlString.descendants("stRef:filePath");	     var ln = fileRef.length(), arr = [];	     for (var i = 0; i<ln; i++)	     {	     	arr.push(fileRef[i]);	     }	     return arr;	 }	 catch(e)	 {	 	return "Error on fetching document files list";	 }}	main();    function main(){app.userInteractionLevel = UserInteractionLevel.DONTDISPLAYALERTS;   try   {    var docFileList = getUsedFiles(activeDocument);	return docFileList;    }    catch(e)    {        return "Document is not active to fetch document file list.";     }}