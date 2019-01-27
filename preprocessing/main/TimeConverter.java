package dimensionality.main;

import dimensionality.Aggregate;
import dimensionality.DataSpec;
import dimensionality.DatasetSpec;
import dimensionality.DcAndOffset;
import dimensionality.DutyCycle;
import dimensionality.TableSpec;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

public class TimeConverter
{
	public void convert() throws IOException, ParseException
	{
		// spec for all datasets
		DataSpec allSpecs = new DataSpec();

		/* get start times of users */
		HashMap<String, HashMap<String, Date>> startTimes = this.readStartTimes(DataSpec.fStudyDuration);
		
		// iterate datasets
		Set<String> datasets = allSpecs.getDatasetNames();
		for(String ds : datasets)
		{
			System.out.println(ds);
			
			// start times for all users in this dataset
			// start times considering all tables
			HashMap<String, Date> userStartTImes = startTimes.get(ds);

			// specs for ds
			DatasetSpec dsSpec = allSpecs.getDatasetSpec(ds);
			// iterate all tables in this dataset
			Set<String> tables = dsSpec.tableSpecs.keySet();
			for (String tbl : tables)
			{
				System.out.println(tbl);
				
				// table specs
				// different table may have different field indices from the same type of data
				TableSpec tblSpec = dsSpec.tableSpecs.get(tbl);
				
				// iterate users
				Set<String> users = userStartTImes.keySet();
				for (String user : users)
				{
					System.out.println(user);
					
					// csv file for this user's data for this table and this dataset
					String fCsv = String.format("%1$s/%2$s/%3$s/%4$s.csv", allSpecs.getRootDataDir(), ds, tbl, user);
					
					Date startTime = userStartTImes.get(user);
					
					// iterate each sampling interval
					for (long sInterval : dsSpec.samplingIntervals)
					{
						String ofPath = fCsv + "-" + sInterval + ".csv";
						
						// TODO : there is different processing for accelerometer
						// TODO : implement
						if (tbl.equals("accel"))
							this.accelToDutyCycle(
									fCsv,
									tblSpec.containsHeader,
									ofPath,
									DataSpec.defaultFS,
									tblSpec.idIndex,
									tblSpec.timeIndex,
									tblSpec.timeFormat,
									tblSpec.accelXIndex,
									tblSpec.accelYIndex,
									tblSpec.accelZIndex,
									startTime,
									sInterval);
						else
							this.toDutyCycle(
									fCsv,
									tblSpec.containsHeader,
									ofPath,
									tblSpec.pickDcRepresentative,
									DataSpec.defaultFS,
									tblSpec.timeFormat,
									tblSpec.timeIndex,
									startTime,
									sInterval);

						System.gc();

						// aggregate for some tables:
						if ( tbl.equals("wifi") || tbl.equals("btooth") )
						{
							Aggregate.aggregateCount(ofPath, true, new int[]{ tblSpec.idIndex, tblSpec.timeIndex }, tblSpec.valueIndex, DataSpec.defaultFS);
						}
					}
				}
			}
		}
	}
	
	public static void main(String[] args) throws IOException, ParseException
	{
		new TimeConverter().convert();
	
	}
	
	public void toDutyCycle(
			String ifPath,
			boolean ifContainsHeader,
			String ofPath,
			boolean pickDcRepresentative,
			String fs,
			String timeFormat, 
			int timeIndex,
			Date startTime,
			double sInterval) throws IOException, ParseException
	{
		// return if input file does not exist:
		if (! new File(ifPath).exists())
			return;
		
		SimpleDateFormat dtFormat = new SimpleDateFormat(timeFormat);

		try(BufferedReader br = new BufferedReader(new FileReader(ifPath));
				BufferedWriter bw = new BufferedWriter(new FileWriter(ofPath, false)))
		{
			// remove header (if exists)
			if (ifContainsHeader)
			{
				String[] headerElements = br.readLine().split(fs);
				headerElements[timeIndex] = "DutyCycle";
				bw.write(String.join(fs, headerElements));
				bw.newLine();
			}
			
			String line = null;
			DcAndOffset prevdcnoff = null;
			String[] prevDcLineElements = null;
			
			while( (line = br.readLine()) != null )
			{
				String[] elements = line.split(fs);
				Date recordTime = dtFormat.parse(elements[timeIndex]);
				DcAndOffset dcnoff = DutyCycle.getDcAndOffset(recordTime.getTime() - startTime.getTime(), sInterval);
				
				/* whether to pick one representative from records with same DC during the conversion of timestamp to DutyCycle */
				if ( pickDcRepresentative )
				{
					// first non-header line
					if (prevdcnoff == null)
					{
						prevDcLineElements = elements;
						prevdcnoff = new DcAndOffset(dcnoff.dc, dcnoff.offset);
					}
					else
					{
						if (dcnoff.dc == prevdcnoff.dc)
						{
							if (dcnoff.offset < prevdcnoff.offset )
							{
								prevDcLineElements = elements;
								prevdcnoff.replace(dcnoff);
							}
						}
						else
						{
							// save prevDcLineElements
							prevDcLineElements[timeIndex] = prevdcnoff.dc + "";
							bw.write(String.join(fs, prevDcLineElements));
							bw.newLine();

							// update
							prevDcLineElements = elements;
							prevdcnoff.replace(dcnoff);
						}
					}
				}
				else
				{
					elements[timeIndex] = dcnoff.dc + "";
					bw.write(String.join(fs, elements));
					bw.newLine();
				}
			}
			if (pickDcRepresentative && prevdcnoff != null)
			{
				prevDcLineElements[timeIndex] = prevdcnoff.dc + "";
				bw.write(String.join(fs, prevDcLineElements));
				bw.newLine();
			}
		}
	}

	/**
	 * @param fStudyDuration
	 * @return [ (dataset -> [ (user, start_time) ]) ]
	 * @throws java.io.IOException
	 * @throws java.text.ParseException
	 */
	public HashMap<String, HashMap<String, Date>> readStartTimes(String fStudyDuration) throws IOException, ParseException
	{
		HashMap<String, HashMap<String, Date>> startTimes = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(fStudyDuration)))
		{
			// discard the header
			br.readLine();
			
			String line = null;
			while ((line = br.readLine()) != null)
			{
				String[] parts = line.split(DataSpec.defaultFS);
				String dset = parts[0];
				String userid = parts[1];
				Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(parts[3]);
				
				if (! startTimes.containsKey(dset))
				{
					HashMap<String, Date> x = new HashMap<>();
					x.put(userid, startTime);
					startTimes.put(dset, x);
				}
				else
				{
					if (startTimes.get(dset).containsKey(userid))
					{
						Date x = startTimes.get(dset).get(userid);
						if (x.after(startTime))
							startTimes.get(dset).put(userid, startTime);
					}
					else
						startTimes.get(dset).put(userid, startTime);
				}
			}
		}

		return startTimes;
	}

	private void accelToDutyCycle (
			String ifPath,
			boolean ifContainsHeader,
			String ofPath,
			String fs,
			int idIndex,
			int timeIndex,
			String timeFormat, 
			int iX,
			int iY,
			int iZ,
			Date startTime,
			double sInterval ) throws IOException, ParseException
	{
		// return if input file does not exist:
		if (! new File(ifPath).exists())
			return;
		
		SimpleDateFormat dtFormat = new SimpleDateFormat(timeFormat);

		try(BufferedReader br = new BufferedReader(new FileReader(ifPath));
				BufferedWriter bw = new BufferedWriter(new FileWriter(ofPath, false)))
		{
			// remove header (if exists)
			if (ifContainsHeader)
			{
				br.readLine();
				bw.write(String.join(fs, new String[]{"user_id,DutyCycle,mean,stddev"}));
				bw.newLine();
			}
			
			String line = null;
			DcAndOffset prevdcnoff = null;

			// TODO: assuming that the file has the same uid
			String uid = null;

			double mean;
			double stddev;
			LinkedList<Double> values = null;
			
			while( (line = br.readLine()) != null )
			{
				String[] elements = line.split(fs);
				
				// assuming that the file has the same uid
				if (uid == null)
					uid = elements[idIndex];

				Date recordTime = dtFormat.parse(elements[timeIndex]);
				DcAndOffset dcnoff = DutyCycle.getDcAndOffset(recordTime.getTime() - startTime.getTime(), sInterval);

				// calc accel
				double x = Double.parseDouble(elements[iX]);
				double y = Double.parseDouble(elements[iY]);
				double z = Double.parseDouble(elements[iZ]);
				double accel = Math.sqrt( x*x + y*y + z*z );

				if (prevdcnoff == null || prevdcnoff.dc != dcnoff.dc)
				{
					if ( prevdcnoff == null )
					{
						values = new LinkedList<>();
					}
					else if (prevdcnoff.dc != dcnoff.dc)
					{
						// get mean, stdde
						double[] m_s = mean_stddev(values);
						mean = m_s[0];
						stddev = m_s[1];

						// dump
						bw.write(String.join(fs, new String[]{ uid, prevdcnoff.dc+"", mean+"", stddev+"" }));
						bw.newLine();
						
						// restart getting values
						values.clear();
					}
				}

				values.add(accel);
				prevdcnoff = new DcAndOffset(dcnoff.dc, dcnoff.offset);
				
				//elements[timeIndex] = dcnoff.dc + "";
			}
			
			// treat any remaining values
			if (values != null && values.size() > 0)
			{
				double[] m_s = mean_stddev(values);
				mean = m_s[0];
				stddev = m_s[1];

				// dump
				bw.write(String.join(fs, new String[]{ uid, prevdcnoff.dc+"", mean+"", stddev+"" }));
				bw.newLine();

				// clear values
				values.clear();
			}

		}
	}
	
	public double [] mean_stddev(LinkedList<Double> list)
	{
		int N = list.size();

		double sum  = 0;
		for ( double e : list)
			sum += e;
		
		double mean = sum/N;
		
		sum = 0;
		double diff;
		for ( double e : list)
		{
			diff = e-mean;
			sum += diff*diff;
		}
		double stddev = Math.sqrt(sum)/N;
		
		return new double[]{ mean, stddev };
	}

}
