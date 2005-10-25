package org.jax.mgi.app.pirsfload.output;

import org.jax.mgi.shr.dla.output.MGIReportFormatter;
import org.jax.mgi.shr.dla.output.MGIReportFormatter.ColumnHeader;
import org.jax.mgi.shr.dbutils.SQLDataManager;
import org.jax.mgi.shr.ioutils.IOUException;

/**
 * is a class which extends MGIFormatter from the lib_java_dla product and
 * provides functionality for rendering a discrepancy report in MGI report
 * format
 * @has nothing
 * @does formats output in MGI reprot format
 * @company The Jackson Laboratory
 * @author M Walker
 *
 */


public class ReportFormatter_Discrepancy extends MGIReportFormatter
{

    /**
     * get the names of the columns
     * @return the names of the columns
     */
    public ColumnHeader[] getColumnHeaders()
    {
        return new ColumnHeader[] {
            new ColumnHeader("MGI Marker", 15),
            new ColumnHeader("Mapped Superfamilies", 30)};
    }

    /**
     * get the description of the report
     * @return the description of the report
     */
    public String getReportDescription()
    {
        return
            "PIRSFLoad - Markers mapped to multiple superfamilies " +
            "(not loaded)";
    }

}
