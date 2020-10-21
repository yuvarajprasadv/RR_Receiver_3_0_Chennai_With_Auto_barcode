

main(arguments);
function main(argv)
{      
    try
    {          
    		app.userInteractionLevel = UserInteractionLevel.DONTDISPLAYALERTS;
        var sourceDoc = app.activeDocument;
        return ExportDocumentAsJPEG(sourceDoc, argv[0], argv[1].slice());
    }catch(exception){
        var errorString;
        errorString = "PDF Export failed. Document is not active";
        
       // $.writeln(exception);
        return errorString;
    }
}


function ExportDocumentAsJPEG(docSource, docTargetPath, jpegProperties)
{
	//jpegProperties = {"antiAliasing", "qualitySetting", "artBoardClipping", "horizontalScale", "verticalScale", "optimization"};
		 
	try{
    	if(app.documents.length > 0)
    	{
    		var docFileSpec = new File ( docTargetPath );
    		var exportOptions = new ExportOptionsJPEG();
    		var type = ExportType.JPEG;
    		
    		 
        	exportOptions.antiAliasing = jpegProperties[0];
        	exportOptions.qualitySetting = jpegProperties[1];
        	exportOptions.artBoardClipping = jpegProperties[2];
        exportOptions.horizontalScale = jpegProperties[3];
        exportOptions.verticalScale = jpegProperties[4];
        exportOptions.optimization = jpegProperties[5];
        
    		docSource.exportFile(docFileSpec, type, exportOptions);
    		return "JPEG exported successfully";
    		
    	}
    	else
        {
            var errorString;
            errorString = "PDF Export failed. Document is not loaded";
            return errorString;
        }
    }
    catch(exception)
    {
        return "PDF Export failed";
    }
}

