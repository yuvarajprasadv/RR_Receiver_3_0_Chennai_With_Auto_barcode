
var docRef = app.activeDocument;  


main(arguments);
function main(argv)
{
var placedHidden = true;
var placedLocked  = true;
var placedItemLayerLocked = true;
var placedItemLayerVisible = true; 

var iCount = 0;

try{
	for(var j = 0; j < app.activeDocument.placedItems.length; j++)
	{
			placedArt = app.activeDocument.placedItems[j];
			placedLocked = placedArt.locked;
			placedHidden = placedArt.hidden;
            
             try{
                  var filePath = placedArt.file;
                 }
             catch(ex)
             {
                 continue;
                }
             
			var placedItemLayer = app.activeDocument.layers.getByName(placedArt.layer.name);
			placedItemLayerLocked = placedItemLayer.locked;
              placedItemLayerVisible = placedItemLayer.visible;
			
             placedItemLayer.locked = false;
             placedItemLayer.visible = true;
			
			placedArt.locked = false;
			placedArt.hidden = false;
			placedArt.embed();
             j--;
			
			placedArt.locked = placedLocked;
			placedArt.hidden = placedHidden;
            
             placedItemLayer.locked = placedItemLayerLocked;
             placedItemLayer.visible = placedItemLayerVisible;
             
			
			
	}
}
catch(e)
	{
		errString = "Embed failed: " + e.description;
		
	}

 return true;
}