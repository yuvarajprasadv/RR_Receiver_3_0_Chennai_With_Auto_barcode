
main(arguments);

function main(argv)
{      
    try
    {
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
	//String[] pdfProperties = {"acrobatLayers", "optimization"}; //for properties order reference; should match in code as well as in js
	
    if ( app.documents.length > 0 ) 
    {
        var pdfFileName = new File ( pdfTargetPath );
        var savePdfOpts = new PDFSaveOptions();
                
        savePdfOpts.compatibility = PDFCompatibility.ACROBAT6;
        
        if(pdfProperties[0].toString() === 'true')
        {
        		savePdfOpts.acrobatLayers = true;
        	}
        	else if (pdfProperties[0].toString() == 'false')
        		savePdfOpts.acrobatLayers = false;
        
        if(pdfProperties[1].toString() === "true")
        		savePdfOpts.optimization = true;
        	else if (pdfProperties[1].toString() === "false")
        		savePdfOpts.optimization = false;
        		
       
        sourceDoc.saveAs(pdfFileName, savePdfOpts );
        return 'Success Normal PDF';
    }
    else
    {
        var errorString;
        errorString = "PDF Export failed. Document is not loaded";
        return errorString;
    }
}

