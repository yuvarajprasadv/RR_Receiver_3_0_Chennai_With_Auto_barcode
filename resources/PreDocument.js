
main(arguments);
function main(argv)
{
	app.userInteractionLevel = UserInteractionLevel.DONTDISPLAYALERTS;
	var f = new File(argv[0]);
	sourceDoc = app.open(f);
	$.sleep (3000);
	return sourceDoc;
}
