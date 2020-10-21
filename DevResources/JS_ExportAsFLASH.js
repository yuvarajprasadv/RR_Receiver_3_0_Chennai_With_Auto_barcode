
main(arguments);

function main(argv)
{      
    try
    {
    		app.userInteractionLevel = UserInteractionLevel.DONTDISPLAYALERTS;
        sourceDoc = app.activeDocument;
        return ExportDocumentAsFLASH(sourceDoc, argv[0], argv[1].slice());
        
    }catch(exception){
        var errorString;
        errorString = "PDF Export failed. Document is not active";
        return errorString;
    }
}

function ExportDocumentAsFLASH(docSource, docTargetPath, flashProperties)
{
	//flashProperties = {"resolution", "convertTextToOutlines"};
	if(app.documents.length > 0)
	{
		var docFileSpec = new File ( docTargetPath );
		var exportOptions = new ExportOptionsFlash();
		var type = ExportType.FLASH;
		
		exportOptions.resolution = flashProperties[0];
		exportOptions.convertTextToOutlines = flashProperties[1];
		
		docSource.exportFile(docFileSpec, type, exportOptions);
	}
}

