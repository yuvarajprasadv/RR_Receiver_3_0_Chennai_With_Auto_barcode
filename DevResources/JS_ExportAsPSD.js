
main(arguments);

function main(argv)
{      
    try
    {
    		app.userInteractionLevel = UserInteractionLevel.DONTDISPLAYALERTS;
        sourceDoc = app.activeDocument;
        return ExportDocumentAsPSD(sourceDoc, argv[0], argv[1].slice());
        
    }catch(exception){
        var errorString;
        errorString = "PDF Export failed. Document is not active";
        return errorString;
    }
}

function ExportDocumentAsPSD(docSource, docTargetPath, psdProperties)
{
	if(app.documents.length > 0)
	{
		var docFileSpec = new File ( docTargetPath );
		var exportOptions = new ExportOptionsPhotoshop();
		var type = ExportType.PHOTOSHOP;
		
        	exportOptions.antiAliasing = psdProperties[0];
		exportOptions.resolution = psdProperties[1];
		exportOptions.editableText = psdProperties[2];
		
		docSource.exportFile(docFileSpec, type, exportOptions);
	}
}


