package org.jax.mgi.app.pirsfload.output;

import org.jax.mgi.shr.dla.output.MGIReportFormatter;
import org.jax.mgi.shr.dla.output.MGIReportFormatter.ColumnHeader;
import org.jax.mgi.shr.dbutils.SQLDataManager;
import org.jax.mgi.shr.ioutils.IOUException;


public class ReportFormatter_Discrepancy extends MGIReportFormatter
{

    public ColumnHeader[] getColumnHeaders()
    {
        return new ColumnHeader[] {
            new ColumnHeader("MGI Marker", 15),
            new ColumnHeader("Mapped Superfamilies", 30)};
    }

    public String getReportDescription()
    {
        return
            "PIRSFLoad - Markers mapped to multiple superfamilies " +
            "(not loaded)";
    }

}
