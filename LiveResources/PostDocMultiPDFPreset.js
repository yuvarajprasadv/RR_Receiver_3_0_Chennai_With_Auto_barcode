
main(arguments);
var errString = "null";
function main(argv)
{      
        sourceDoc = app.activeDocument;
     	SavePdfPreset(sourceDoc,  argv[0],  argv[2],  argv[3]);
     	return SaveSourceDoc(sourceDoc, argv[1]);
}

function SavePdfPreset(sourceDoc, pdfTargetPath, pdfPresetFilePath, pdfPresetFileName)
{

	try
	{
		var presetFile = new File (pdfPresetFilePath);
		sourceDoc.importPDFPreset(presetFile, true);
				
		var pdfFileName = new File ( pdfTargetPath );
        savePdfOpts = new PDFSaveOptions();
        savePdfOpts.pDFPreset = pdfPresetFileName;
        
        sourceDoc.saveAs( pdfFileName, savePdfOpts );
	}
	catch(e)
	{
		errString = "Export pdf with preset failed: " + e.description;
	}
}


function SaveSourceDoc(docSource, docTargetPath)
{
     if ( app.documents.length > 0 ) 
    {
        var docFileName = new File ( docTargetPath );
        var saveOpts = new IllustratorSaveOptions();
    		saveOpts.embedLinkedFiles = true;
    		saveOpts.fontSubsetThreshold = 0.0;
   	 	saveOpts.pdfCompatible = true;
        docSource.saveAs( docFileName, saveOpts );
        return errString;
    }
}
