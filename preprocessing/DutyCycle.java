package dimensionality;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DutyCycle
{
	public static DcAndOffset getDcAndOffset(long millisecDiff, double sInterval)
	{
		// use SI units.
		// so convert milliseconds to seconds by dividing by 1000
		double d = (millisecDiff/1000) /sInterval;
		long dc = (long) Math.round(d);
		double offset = dc > d ? dc - d : d - dc;
		return new DcAndOffset(dc, offset);
	}

	// add duty cycle to data
	public static void addDC(
			String ifpath,
			String ofpath,
			Date startTime,
			double sInterval,
			String timeFormat,
			int timeIndex,
			boolean hasHeader,
			String fs,
			int[] outputIndices) throws IOException, ParseException
	{
		SimpleDateFormat dtFormat = new SimpleDateFormat(timeFormat);
		
		try(
				BufferedReader br = new BufferedReader(new FileReader(ifpath));
				BufferedWriter bw = new BufferedWriter(new FileWriter(ofpath, false)))
		{
			// remove header (if exists)
			if (hasHeader)
			{
				// transfer header (add dc at the end)
				bw.write( String.format("%1$s%2$s%3$s", br.readLine(), fs, "dc") );
			}

			String line = null;
			DcAndOffset prevdcnoff = null;
			String[] prevLineElements = null;
			
			while( (line = br.readLine()) != null )
			{
				String[] elements = line.split(fs);
				Date recordTime = dtFormat.parse(elements[timeIndex]);
				DcAndOffset dcnoff = getDcAndOffset(recordTime.getTime() - startTime.getTime(), sInterval);
				
				// first non-header line
				if (prevdcnoff == null)
				{
					prevLineElements = elements;
					prevdcnoff = new DcAndOffset(dcnoff.dc, dcnoff.offset);
				}
				else
				{
					if (dcnoff.dc == prevdcnoff.dc)
					{
						if (dcnoff.offset < prevdcnoff.offset )
						{
							prevLineElements = elements;
							prevdcnoff.replace(dcnoff);
						}
					}
					else
					{
						// write prevLineElements
						bw.write(String.join(fs, prevLineElements));
						bw.write(fs);
						bw.write(prevdcnoff.dc + "");
						
						prevLineElements = elements;
						prevdcnoff.replace(dcnoff);
					}
				}
			}
			if (prevdcnoff != null)
			{
				// write prevLineElements
				bw.write(String.join(fs, prevLineElements));
				bw.write(fs);
				bw.write(prevdcnoff.dc + "");
			}
		}
	}

}
