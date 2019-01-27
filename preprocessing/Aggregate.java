package dimensionality;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Aggregate
{
	public static void aggregateCount(String ifile, boolean hasHeader, int[] keyFieldIndices, int agFieldIndex, String fs) throws IOException
	{
		// return if input file does not exist:
		if (! new File(ifile).exists())
			return;

		// first write to a temp file
		try ( BufferedReader br = new BufferedReader(new FileReader(ifile));
				BufferedWriter bw = new BufferedWriter(new FileWriter(ifile + ".tmp", false)))
		{
			if (hasHeader)
			{
				String[] headerElements = br.readLine().split(fs);
				for (int i : keyFieldIndices )
				{
					bw.write(headerElements[i]);
					bw.write(fs);
				}
				bw.write("Count");
				bw.newLine();
			}
			
			String line = null;
			String[] prevElements = null;
			SortedUniqueList<String> values = new SortedUniqueList<>();
			
			while ( (line = br.readLine()) != null )
			{
				String[] elements = line.split(fs);
				
				// first record
				if (prevElements == null)
				{
					try
					{
						values.add(elements[agFieldIndex]);
						prevElements = elements;
					}
					catch (Exception ex)
					{
						System.err.print("bad record found in file ");
						System.err.println(ifile);
						System.err.print("Bad line: ");
						System.err.println(line);						
					}
					continue;
				}

				// if same as previous record
				boolean isSame = true;
				for (int i : keyFieldIndices)
				{
					if ( ! elements[i].equals(prevElements[i]) )
					{
						isSame = false;
						break;
					}
				}

				// if same as previous record,
				// add the new value to the list
				// else, dump + reset + initialize
				if (isSame)
				{
					// same key
					try
					{
						values.add(elements[agFieldIndex]);
					}
					catch (Exception ex)
					{
						System.err.print("bad record found in file ");
						System.err.println(ifile);
						System.err.print("Bad line: ");
						System.err.println(line);						
					}
				}
				else // different key
				{
					// new key found
					// dump
					if ( values.size() > 0 )
					{
						for (int i : keyFieldIndices)
						{
							bw.write(prevElements[i]);
							bw.write(fs);
						}
						bw.write(values.size() + "");
						bw.newLine();
					}

					// reset
					prevElements = null;
					values.clear();
					
					try
					{
						values.add(elements[agFieldIndex]);
						// initialize
						prevElements = elements;
					}
					catch (Exception ex)
					{
						System.err.print("bad record found in file ");
						System.err.println(ifile);
						System.err.print("Bad line: ");
						System.err.println(line);						
					}
					
					// clean up:
					elements = null;
				}

			}
			
			// address last record
			if (values.size() > 0)
			{
				for (int i : keyFieldIndices)
				{
					bw.write(prevElements[i]);
					bw.write(fs);
				}
				bw.write(values.size() + "");
				bw.newLine();

				// reset + clean up
				prevElements = null;
				values.clear();
				values = null;
			}
		} // try block
		
		// move tmp file to the original path
		Files.move(new File(ifile + ".tmp").toPath(),
				new File(ifile).toPath(),
				StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.ATOMIC_MOVE );

		// invoke gc
		System.gc();
	}
}
