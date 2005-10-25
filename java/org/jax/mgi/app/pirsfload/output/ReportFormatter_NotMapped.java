package org.jax.mgi.app.pirsfload.output;

import org.jax.mgi.shr.dla.output.MGIReportFormatter;
import org.jax.mgi.shr.dla.output.MGIReportFormatter.ColumnHeader;
import org.jax.mgi.shr.dbutils.SQLDataManager;
import org.jax.mgi.shr.ioutils.IOUException;

/**
 * is a class which extends MGIFormatter from the lib_java_dla product and
 * provides functionality for rendering a report in MGI report format of
 * superfamilies not mapped to MGI
 * @has nothing
 * @does formats output in MGI report format
 * @company The Jackson Laboratory
 * @author M Walker
 *
 */


public class ReportFormatter_NotMapped extends MGIReportFormatter
{

    /**
     * get the names of the columns
     * @return the names of the columns
     */
    public ColumnHeader[] getColumnHeaders()
    {
        return new ColumnHeader[] {
            new ColumnHeader("Superfamily ID", 15),
            new ColumnHeader("Superfamily Name", 80),
            new ColumnHeader("Uniprot", 150),
            new ColumnHeader("Refseq", 12),
            new ColumnHeader("Entrez Gene", 11),};
    }

    /**
     * get the description of the report
     * @return the description of the report
     */
    public String getReportDescription()
    {
        return
            "PIRSFLoad - Superfamilies not mapped to MGI";
    }

}
