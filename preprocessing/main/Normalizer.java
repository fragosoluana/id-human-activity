package dimensionality.main;

import dimensionality.DataSpec;
import dimensionality.DatasetSpec;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Set;


public class Normalizer
{
	public static void main(String[] args) throws IOException
	{
		Normalizer n = new Normalizer();
		// CONFIG >>
		int[] indices = new int[] { 0, 1, 2, 3, 4, 5, 6 };
		// << CONFIG
		boolean hasHeader = true;
		double[][] minmaxValues = n.getMinMax(indices, hasHeader);
		
		n.normalize(indices, hasHeader, minmaxValues);
	}
	
	public void normalize(int[] indices, boolean hasHeader, double[][] minmaxValues) throws IOException
	{
		double[] minValues = minmaxValues[0];
		double[] maxValues = minmaxValues[1];
		
		DataSpec allSpecs = new DataSpec();

		// all datasets found in DataSpec.java file
		Set<String> dsets = allSpecs.getDatasetNames();

		for(String dset : dsets)
		{
			System.out.println(dset);
			
			// dataset specs
			DatasetSpec dsSpec = allSpecs.getDatasetSpec(dset);
			
			String mergeDir = allSpecs.getRootDataDir() + "/" + dset + "/merged";
			
			File[] files = new File(mergeDir).listFiles(new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String name)
				{
					return name.endsWith(".csv");
				}
			});


			for (File f : files)
			{
				try ( BufferedReader br = new BufferedReader(new FileReader(f));
						BufferedWriter bw = new BufferedWriter( new FileWriter(f.getPath() + ".normalized", false) ) )
				{
					if (hasHeader)
					{
						bw.write( br.readLine() );
						bw.newLine();
					}

					String line = null;
					while ( (line = br.readLine()) != null )
					{
						String[] parts = line.split(allSpecs.defaultFS);

						int i = 0;
						for(int j : indices)
						{
							double x = Double.parseDouble( parts[j] );

							parts[j] = "" + 1.0 * ( x - minValues[i] ) / ( maxValues[i] - minValues[i] );

							i++;
						}
						
						bw.write(String.join(allSpecs.defaultFS, parts));
						bw.newLine();
					}
				}

				System.gc();
			}

		} /* each dataset */
	}
	
	public double[][] getMinMax(int[] indices, boolean hasHeader) throws IOException
	{
		double[] minValues = new double[indices.length];
		double[] maxValues = new double[indices.length];
		
		for(int i = 0; i < indices.length; i++)
		{
			minValues[i] = Double.MAX_VALUE;
			maxValues[i] = -Double.MAX_VALUE;
		}
		
		DataSpec allSpecs = new DataSpec();

		// all datasets found in DataSpec.java file
		Set<String> dsets = allSpecs.getDatasetNames();

		for(String dset : dsets)
		{
			// dataset specs
			DatasetSpec dsSpec = allSpecs.getDatasetSpec(dset);
			
			String mergeDir = allSpecs.getRootDataDir() + "/" + dset + "/merged";
			
			File[] files = new File(mergeDir).listFiles(new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String name)
				{
					return name.endsWith(".csv");
				}
			});


			for (File f : files)
			{
				try (BufferedReader br = new BufferedReader(new FileReader(f)))
				{
					if (hasHeader)
						br.readLine();

					String line = null;
					while ( (line = br.readLine()) != null )
					{
						String[] parts = line.split(allSpecs.defaultFS);

						int i = 0;
						for(int j : indices)
						{
							double x = Double.parseDouble( parts[j] );

							if ( x < minValues[i] )
								minValues[i] = x;

							if ( x > maxValues[i] )
								maxValues[i] = x;

							i++;
						}
					}
				}

				System.gc();
			}

		} /* each dataset */
		
		return new double[][] { minValues, maxValues };
	}
}
