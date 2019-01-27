/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dimensionality;

/**
 *
 * @author tuhin
 */
public class DcAndOffset
{
	public long dc;
	public double offset;

	private DcAndOffset()
	{
	}

	public DcAndOffset(long dc, double offset)
	{
		this.dc = dc;
		this.offset = offset;
	}

	public void replace(DcAndOffset newdcnoff)
	{
		this.dc = newdcnoff.dc;
		this.offset = newdcnoff.offset;
	}
}
