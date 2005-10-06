package org.jax.mgi.app.pirsfload.output;

import org.jax.mgi.shr.dla.output.MGIReportFormatter;
import org.jax.mgi.shr.dla.output.MGIReportFormatter.ColumnHeader;
import org.jax.mgi.shr.dbutils.SQLDataManager;
import org.jax.mgi.shr.ioutils.IOUException;


public class ReportFormatter_NotMapped extends MGIReportFormatter
{

    public ColumnHeader[] getColumnHeaders()
    {
        return new ColumnHeader[] {
            new ColumnHeader("Superfamily ID", 15),
            new ColumnHeader("Superfamily Name", 80),
            new ColumnHeader("Uniprot", 150),
            new ColumnHeader("Refseq", 12),
            new ColumnHeader("Entrez Gene", 11),};
    }

    public String getReportDescription()
    {
        return
            "PIRSFLoad - Superfamilies not mapped to MGI";
    }

}
