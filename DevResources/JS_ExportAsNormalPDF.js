
main(arguments);

function main(argv)
{      
    try
    {

    		app.userInteractionLevel = UserInteractionLevel.DONTDISPLAYALERTS;
        sourceDoc = app.activeDocument;
        return SaveDocAsPDF(sourceDoc, argv[0], argv[1].slice());
        
    }catch(exception){
        var errorString;
        errorString = "PDF Export failed. Document is not active";
        return errorString;
    }
}


function SaveDocAsPDF (sourceDoc, pdfTargetPath, pdfProperties)
{
	//String[] pdfProperties = {"acrobatLayers", "optimization", "preserveEditability"};//for properties order reference; should match in code as well as in js
	
    if ( app.documents.length > 0 ) 
    {
    
    	try{
  //  	$.writeln(pdfProperties[0]);
        var pdfFileName = new File ( pdfTargetPath );
        var savePdfOpts = new PDFSaveOptions();
        
        savePdfOpts.compatibility = PDFCompatibility.ACROBAT6;
        
    		savePdfOpts.acrobatLayers = pdfProperties[0];
    		savePdfOpts.optimization = pdfProperties[1];
    		savePdfOpts.preserveEditability = pdfProperties[2];
    		
        sourceDoc.saveAs(pdfFileName, savePdfOpts );
        return 'Success Normal PDF';
        }
        catch(e)
        {
        		var errorString;
        		errorString = "PDF Export failed : " + e;
        		return errorString;
        }
    }
    else
    {
        var errorString;
        errorString = "PDF Export failed. Document is not loaded";
        return errorString;
    }
}

