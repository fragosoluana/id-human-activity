package dimensionality;

public class TableSpec
{
	public String table;
	
	/* whether or not input file contains header */
	public boolean containsHeader;
	
	// merge same DC records
	// whether to pick one representative from records with same DC during the conversion of timestamp to DutyCycle
	public boolean pickDcRepresentative;

	/* indices of different files in the input file */
	public int idIndex;
	public int timeIndex;
	public int accuIndex;
	public int valueIndex;
	public int accelXIndex;
	public int accelYIndex;
	public int accelZIndex;
	
	
	/* time format in input file */
	public String timeFormat;
}
