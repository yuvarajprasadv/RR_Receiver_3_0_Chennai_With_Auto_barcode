
main(arguments);
function main(argv)
{ 
app.userInteractionLevel = UserInteractionLevel.DONTDISPLAYALERTS;
    try
    {
        var swatchObj = app.activeDocument.swatches.getByName("ACC");
        if(swatchObj.name == "ACC")
        {
            swatchObj.name = argv[0];
            return "null";
        }
     }
     catch(ex)
     {
        return "Error on swatch Renaming " + ex.description;
     }
}
  