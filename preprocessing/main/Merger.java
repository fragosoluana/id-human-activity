package dimensionality.main;

import dimensionality.DataSpec;
import dimensionality.DatasetSpec;
import dimensionality.MyException;
import dimensionality.TableSpec;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;


public class Merger
{
	public static void main(String[] args) throws MyException, IOException, ParseException, InterruptedException
	{
		new Merger().go(DataSpec.fStudyDuration);
	}


	public void mergeFiles(
			String ifPath1,
			boolean hasHeader1,
			int timeIndex1,
			
			String ifPath2,
			boolean hasHeader2,
			int timeIndex2,
			
			String fs,
			String ofPath) throws IOException
	{
		try (BufferedReader br1 = new BufferedReader(new FileReader(ifPath1));
				BufferedReader br2 = new BufferedReader(new FileReader(ifPath2));
				BufferedWriter bw = new BufferedWriter(new FileWriter(ofPath, true)))
		{
			// strip off headers
			if (hasHeader1)
				bw.write( br1.readLine() );
			bw.write(fs);
			if (hasHeader2)
				bw.write( br2.readLine() );
			bw.newLine();

			String line1 = br1.readLine();
			String line2 = br2.readLine();
			
			while (line1 != null &&  line2 != null)
			{
				String[] elements1 = line1.split(fs);
				String[] elements2 = line2.split(fs);
		
				int dc1 = -1, dc2 = -2;
				try {
					dc1 = Integer.parseInt(elements1[timeIndex1]);
					dc2 = Integer.parseInt(elements2[timeIndex2]);
				}
				catch(Exception ex) {
					ex.printStackTrace();
				}
				
				if (dc1 == dc2)
				{
					LinkedList<String> joinedLine = new LinkedList<>();
					
					for (String s : elements1)
						joinedLine.add(s);
					
					for (String s : elements2)
						joinedLine.add(s);
					
					bw.write(String.join(fs, joinedLine));
					bw.newLine();
					joinedLine.clear();
					
					line1 = br1.readLine();
					line2 = br2.readLine();
				}
				else
				{
					if (dc1 < dc2)
						line1 = br1.readLine();
					else
						line2 = br2.readLine();
				}
			}
		}
	}
	
	public void go(String fStudyDuration) throws IOException, ParseException
	{
		// start times for all users:
		HashMap<String, HashMap<String, Date>> startTimes =
				new TimeConverter().readStartTimes(fStudyDuration);

		// specs for all dataset
		DataSpec allSpecs = new DataSpec();

		// all datasets found in DataSpec.java file
		Set<String> dsets = allSpecs.getDatasetNames();

		for(String dset : dsets)
		{
			System.out.println(dset);
			
			// dataset specs
			DatasetSpec dsSpec = allSpecs.getDatasetSpec(dset);
			
			// directory where merged files are saved for dataset $dset
			String mergeDirectory = String.format("%1$s/%2$s/merged", allSpecs.getRootDataDir(), dset);
			Files.createDirectories(new File(mergeDirectory).toPath());
			
			// table specs
			HashMap<String, TableSpec> tblSpecs = dsSpec.tableSpecs;
			// tables in the dataset
			String[] tbls = tblSpecs.keySet().toArray(new String[0]);
			
			Set<String> users = startTimes.get(dset).keySet();
			// sampling interval determines duty cycle numbers
			for (long sInterval : dsSpec.samplingIntervals)
			{
				for (String user : users)
				{
					// TODO: merge data for all tables
					String ofPath = String.format("%1$s/%2$s/merged/%3$s-%4$s.csv", allSpecs.getRootDataDir(), dset, user, sInterval);

					boolean failed = false;
					for(int i = 1; i < tbls.length; i++ )
					{
						try
						{
							TableSpec tSpec1 = tblSpecs.get(tbls[i-1]);
							TableSpec tSpec2 = tblSpecs.get(tbls[i]);

							String ifPath1;
							if (i == 1)
								ifPath1 = String.format("%1$s/%2$s/%3$s/%4$s.csv-%5$s.csv", allSpecs.getRootDataDir(), dset, tSpec1.table, user, (long)sInterval);
							else
								ifPath1 = ofPath;

							String ifPath2 = String.format("%1$s/%2$s/%3$s/%4$s.csv-%5$s.csv", allSpecs.getRootDataDir(), dset, tSpec2.table, user, (long)sInterval);

							// temporary output path:
							String tmpOfPath = String.format("%1$s.tmp", ofPath);


							if ( ! new File(ifPath1).exists() || ! new File(ifPath1).exists() )
								throw new MyException("Not all files exist!");

							// merge
							this.mergeFiles(
									ifPath2,
									tSpec2.containsHeader,
									1, // assuming that time/dutycycle field is the 2nd one

									ifPath1,
									tSpec1.containsHeader,
									1, // assuming that time/dutycycle field is the 2nd one

									allSpecs.defaultFS,
									tmpOfPath);

							Files.move(
									new File(tmpOfPath).toPath(),
									new File(ofPath).toPath(),
									StandardCopyOption.REPLACE_EXISTING,
									StandardCopyOption.ATOMIC_MOVE);
						}
						catch(Exception ex)
						{
							// failed to get all data for this user
							failed = true;

							System.err.println(user + " files are being deleted!");
							// delete files for this user because data for some
							// tables are missing
							File f = new File(ofPath);
							if( f.exists() )
								f.delete();
							f = new File(ofPath + ".tmp");
							if( f.exists() )
								f.delete();

							// process next user
							break;
						}
					}
					
					// if NOT failed
					// add hours, minutes
					if (! failed)
					{
						appendHourMinute(
								ofPath,
								1, // assuming that time/dutycycle field is the 2nd one
								startTimes.get(dset).get(user),
								tblSpecs.get(tbls[0]).containsHeader,
								DataSpec.defaultFS,
								sInterval
						);
					}
					
					
					System.gc();
				} /* each user */
			} /* each sampling interval */
		} /* each dataset */

		/**
		 * N-d Tree operation
		 */
		/*
		int numDimensions = 3;
		LinkedList<double[]> points = getPoints(numDimensions);
		
		NDTree root = NDTree.createRoot(numDimensions);
		for (double[] point : points)
		{
			assert numDimensions == point.length : "Number of dimensions in the data is NOT " + numDimensions;
			root.insert(point);
		}*/
	}

	private static LinkedList<double[]> getPoints(int numDimensions)
	{
		LinkedList<double[]> points = new LinkedList<>();
		
		/** TODO: load points */
		for (int i = 0; i < 100; i++ )
		{
			double[] p = new double[numDimensions];
			for (int k = 0; k < numDimensions; k++)
			{
				p[k] = Math.random();
			}
			points.add(p);
		}
		
		return points;
	}

	private void appendHourMinute(
			String ofPath,
			int timeIndex,
			Date startTime,
			boolean hasHeader,
			String fs,
			long sInterval) throws IOException
	{
		try(BufferedReader br = new BufferedReader(new FileReader(ofPath));
				BufferedWriter bw = new BufferedWriter(new FileWriter(ofPath + ".tmp")))
		{
			if (hasHeader)
			{
				bw.write(br.readLine());
				bw.write(fs);
				bw.write("hour");
				bw.write(fs);
				bw.write("minute");
				bw.newLine();
			}
			
			String line = null;
			
			Calendar cal = Calendar.getInstance();
			while ( (line = br.readLine()) != null )
			{
				String[] elements = line.split(fs);
				int dc = Integer.parseInt( elements[timeIndex] );
				cal.setTime( new Date ( startTime.getTime() + dc*sInterval*1000) );
				int hour   = cal.get(Calendar.HOUR_OF_DAY);
				int minute = cal.get(Calendar.MINUTE);
				bw.write(line);
				bw.write(fs);
				bw.write(hour+"");
				bw.write(fs);
				bw.write(minute+"");
				bw.newLine();
			}
		}


		Files.move(
				new File(ofPath + ".tmp").toPath(),
				new File(ofPath).toPath(),
				StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.ATOMIC_MOVE);
	}

}
