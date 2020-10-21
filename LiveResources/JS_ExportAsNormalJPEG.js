

main(arguments);
function main(argv)
{      
    try
    {
        sourceDoc = app.activeDocument;
        return ExportDocumentAsJPEG(sourceDoc, argv[0]);
    }catch(exception){
        var errorString;
        errorString = "PDF Export failed. Document is not active";
        return errorString;
    }
}


function ExportDocumentAsJPEG(docSource, docTargetPath)
{
	try{
    	if(app.documents.length > 0)
    	{
    		var docFileSpec = new File ( docTargetPath );
    		var exportOptions = new ExportOptionsJPEG();
    		var type = ExportType.JPEG;
    		exportOptions.antiAliasing = true;
        exportOptions.qualitySetting = 70;
    		docSource.exportFile(docFileSpec, type, exportOptions);
    		
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

