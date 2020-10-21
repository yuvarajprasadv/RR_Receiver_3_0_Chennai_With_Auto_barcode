
var docRef = app.activeDocument;  


main(arguments);
function main(argv)
{ 



var layerList = ["Dimensions", "Legend", "Dimension", "Cutter", "Print Marks"];
/*
$.writeln("\n\n ERRCODE:  " + argv[0].equals("false") + "   " + argv[0].localeCompare("false"));
*/
if(argv[0] == "false")
{
	layerList.push("Barcode");
	layerList.push("Barcodes");
}




for(var i = 0; i < layerList.length; i++)
{
	try
	{

		layerObj = app.activeDocument.layers.getByName(layerList[i]);
		layerObj.visible = false;
	

	}
	catch(e)
	{
		errString = "Layer visiblity set false failed: " + e.description;
	}
}

for(var j = 0; j < app.activeDocument.layers.length; j++)
{
	try
	{
		layerObj  = app.activeDocument.layers[j];
		var layerName = layerObj.name;
		if(layerName.lastIndexOf(".ARD") > 0)
		{
			layerObj.visible = false;
			break;
		}
		else if(layerName.lastIndexOf(".ard") > 0)
		{
			layerObj.visible = false;
			break;
		}
	}
	catch(e)
	{
		errString = "Layer visiblity set false failed: " + e.description;
	}
}


 return true;
};  