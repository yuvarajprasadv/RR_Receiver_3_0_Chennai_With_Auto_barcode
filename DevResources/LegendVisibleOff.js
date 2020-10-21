
var docRef = app.activeDocument;  


main(arguments);
function main(argv)
{ 

var layerVisibleTrue = ["Dimensions", "Dimension", "Cutter", "Barcode", "Barcodes"];
if(argv[0] == "true")
{
	layerVisibleTrue.push("Print Marks");
}
else if(argv[0] == "false")
{
	var layerLegendObj = app.activeDocument.layers.getByName("Print Marks");
    layerLegendObj.visible = true;
    layerLegendObj.locked = false;
    layerLegendObj.remove();
}


for(var i = 0; i < layerVisibleTrue.length; i++)
{
	try
	{

		layerObj = app.activeDocument.layers.getByName(layerVisibleTrue[i]);
		layerObj.visible = true;
	

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