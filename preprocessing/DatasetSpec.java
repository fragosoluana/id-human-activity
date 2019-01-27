package dimensionality;

import java.util.HashMap;

public abstract class DatasetSpec
{
	public String name;
	public long[] samplingIntervals;
	
	/* sample count threshold */
	/* assigning 0 will make it ineffective */
	public int sampleCountThreshold;

	// specs for tables
	public HashMap<String, TableSpec> tableSpecs;

	public DatasetSpec()
	{
		load();
	}
	
	public abstract void load();
}
