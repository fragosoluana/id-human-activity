package dimensionality;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;


public class DataSpec
{
	public static final String defaultFS = ",";
	public static final int    defaultAccuracy = Integer.MAX_VALUE;
	public static final String fStudyDuration = "path to studyduration.csv";
	
	// Root directory containing input data for all datasets
	private final String rootDataDir = "path to the folder which contains all the shed datasets";
	public String getRootDataDir() {
		return this.rootDataDir;
	}

	private HashMap<String, DatasetSpec> dsetSpecs;
	
	public DataSpec()
	{
		LinkedList<DatasetSpec> dspecList = new LinkedList<DatasetSpec>();

		// <editor-fold defaultstate="collapsed" desc="raw table specifications">
		/* specs for wifi data from table */
		TableSpec s7_9_wifiSpec = new TableSpec();
		s7_9_wifiSpec.table = "wifi";
		/* whether or not input file contains header */
		s7_9_wifiSpec.containsHeader = true;
		/* whether to pick one representative from records with same DC during the conversion of timestamp to DutyCycle */
		s7_9_wifiSpec.pickDcRepresentative = false;
		/* indices of different files in the input file */
		s7_9_wifiSpec.idIndex = 0;
		s7_9_wifiSpec.timeIndex = 1;
		s7_9_wifiSpec.accuIndex = -1; // <0 means: does not exist
		s7_9_wifiSpec.valueIndex = 2;
		/* time format in input file */
		s7_9_wifiSpec.timeFormat = "yyyy-MM-dd HH:mm:ss";

		/* specs for battery data from table */
		TableSpec s7_9_batterySpec = new TableSpec();
		s7_9_batterySpec.table = "battery";
		/* whether or not input file contains header */
		s7_9_batterySpec.containsHeader = true;
		/* whether to pick one representative from records with same DC during the conversion of timestamp to DutyCycle */
		s7_9_batterySpec.pickDcRepresentative = true;
		/* indices of different files in the input file */
		s7_9_batterySpec.idIndex = 0;
		s7_9_batterySpec.timeIndex = 1;
		s7_9_batterySpec.accuIndex = -1;
		/* time format in input file */
		s7_9_batterySpec.timeFormat = "yyyy-MM-dd HH:mm:ss";

//		/* specs for bluetooth data from table */
//		TableSpec s7_9_btoothSpec = new TableSpec();
//		s7_9_btoothSpec.table = "btooth";
//		/* whether or not input file contains header */
//		s7_9_btoothSpec.containsHeader = true;
//		/* whether to pick one representative from records with same DC during the conversion of timestamp to DutyCycle */
//		s7_9_btoothSpec.pickDcRepresentative = false;
//		/* indices of different files in the input file */
//		s7_9_btoothSpec.idIndex = 0;
//		s7_9_btoothSpec.timeIndex = 1;
//		s7_9_btoothSpec.accuIndex = -1;
//		s7_9_btoothSpec.valueIndex = 3;
//		/* time format in input file */
//		s7_9_btoothSpec.timeFormat = "yyyy-MM-dd HH:mm:ss";

		/* specs for accelerometer data from table */
		TableSpec s7_9_accelSpec = new TableSpec();
		s7_9_accelSpec.table = "accel";
		/* whether or not input file contains header */
		s7_9_accelSpec.containsHeader = true;
		/* whether to pick one representative from records with same DC during the conversion of timestamp to DutyCycle */
		s7_9_accelSpec.pickDcRepresentative = true;
		/* indices of different files in the input file */
		s7_9_accelSpec.idIndex = 0;
		s7_9_accelSpec.timeIndex = 1;
		s7_9_accelSpec.accuIndex = 2;
		/* accelerometer coponent indices */
		s7_9_accelSpec.accelXIndex = 3;
		s7_9_accelSpec.accelYIndex = 4;
		s7_9_accelSpec.accelZIndex = 5;
		/* time format in input file */
		s7_9_accelSpec.timeFormat = "yyyy-MM-dd HH:mm:ss";

		/* specs for gps data from table */
		TableSpec s7_9_gpsSpec = new TableSpec();
		s7_9_gpsSpec.table = "gps";
		/* whether or not input file contains header */
		s7_9_gpsSpec.containsHeader = true;
		/* whether to pick one representative from records with same DC during the conversion of timestamp to DutyCycle */
		s7_9_gpsSpec.pickDcRepresentative = true;
		/* indices of different files in the input file */
		s7_9_gpsSpec.idIndex = 0;
		s7_9_gpsSpec.timeIndex = 1;
		s7_9_gpsSpec.accuIndex = 2;
		/* time format in input file */
		s7_9_gpsSpec.timeFormat = "yyyy-MM-dd HH:mm:ss";
		// </editor-fold>

		/*
		dspecList.add( new DatasetSpec() {
			public void load()
			{
				this.name = "shed7";
				this.samplingIntervals = new long[] { 5*60, 10*60, 30*60, 60*60, 120*60, 4*60*60, 480*60 };

				this.tableSpecs = new Hashtable<>();
				this.tableSpecs.put("wifi",    s7_9_wifiSpec);
				this.tableSpecs.put("battery", s7_9_batterySpec);
				this.tableSpecs.put("btooth",  s7_9_btoothSpec);
				this.tableSpecs.put("gps",     s7_9_gpsSpec);
				this.tableSpecs.put("accel",   s7_9_accelSpec);
			}
		});
		*/
//
		dspecList.add( new DatasetSpec() {
			public void load()
			{
				this.name = "SHED7";
				this.samplingIntervals = new long[] { 10*60 };

				this.tableSpecs = new HashMap<>();
				this.tableSpecs.put("wifi",    s7_9_wifiSpec);
				this.tableSpecs.put("battery", s7_9_batterySpec);
//				this.tableSpecs.put("btooth",  s7_9_btoothSpec);
				this.tableSpecs.put("gps",     s7_9_gpsSpec);
				this.tableSpecs.put("accel",   s7_9_accelSpec);
			}
		});
//		
//		dspecList.add( new DatasetSpec() {
//			public void load()
//			{
//				this.name = "SHED8";
//				this.samplingIntervals = new long[] { 10*60 };
//
//				this.tableSpecs = new HashMap<>();
//				this.tableSpecs.put("wifi",    s7_9_wifiSpec);
//				this.tableSpecs.put("battery", s7_9_batterySpec);
////				this.tableSpecs.put("btooth",  s7_9_btoothSpec);
//				this.tableSpecs.put("gps",     s7_9_gpsSpec);
//				this.tableSpecs.put("accel",   s7_9_accelSpec);
//			}
//		});
//		
//		dspecList.add( new DatasetSpec() {
//			public void load()
//			{
//				this.name = "SHED9";
//				this.samplingIntervals = new long[] { 10*60 };
//
//				this.tableSpecs = new HashMap<>();
//				this.tableSpecs.put("wifi",    s7_9_wifiSpec);
//				this.tableSpecs.put("battery", s7_9_batterySpec);
////				this.tableSpecs.put("btooth",  s7_9_btoothSpec);
//				this.tableSpecs.put("gps",     s7_9_gpsSpec);
//				this.tableSpecs.put("accel",   s7_9_accelSpec);
//			}
//		});
		
//		dspecList.add( new DatasetSpec() {
//			public void load()
//			{
//				this.name = "SHED10";
//				this.samplingIntervals = new long[] { 10*60 };
//
//				this.tableSpecs = new HashMap<>();
//				this.tableSpecs.put("wifi",    s7_9_wifiSpec);
//				this.tableSpecs.put("battery", s7_9_batterySpec);
////				this.tableSpecs.put("btooth",  s7_9_btoothSpec);
//				this.tableSpecs.put("gps",     s7_9_gpsSpec);
//				this.tableSpecs.put("accel",   s7_9_accelSpec);
//			}
//		});

		this.dsetSpecs = new HashMap<>();
		for ( DatasetSpec spec : dspecList )
			this.dsetSpecs.put(spec.name, spec);
	}

	public DatasetSpec getDatasetSpec(String shortName)
	{
		return this.dsetSpecs.get(shortName);
	}

	public Set<String> getDatasetNames()
	{
		return this.dsetSpecs.keySet();
	}
}
