package org.jax.mgi.app.pirsfload.output;

import org.jax.mgi.shr.ioutils.OutputDataFile;
import org.jax.mgi.shr.config.ConfigException;
import org.jax.mgi.shr.dla.output.HTMLFormatter;

/**
 * is a class which extends HTMLFormatter from the lib_java_dla product and
 * provides functionality for rendering a report in HTML of superfamilies
 * not mapped to MGI
 * @has nothing
 * @does formats output in HTML
 * @company The Jackson Laboratory
 * @author M Walker
 *
 */


public class HTMLFormatter_NotMapped extends HTMLFormatter
{

    /**
     * counstructor
     * @throws ConfigException thrown if there is an error accessing the
     * configuration
     */
    public HTMLFormatter_NotMapped()
    throws ConfigException
    {
        super();

    }

    /**
     * get the header text for this format
     * @return header text
     */
    public String getHeader()
    {
        String stdHead = super.getStandardHeader();
        return stdHead + OutputDataFile.CRT +
            "<TABLE " + HTMLFormatterConstants.TABLE_ATTRIBUTES + ">" +
            "<TR " + HTMLFormatterConstants.TR_ATTRIBUTES + "><TD>Superfamily ID" +
            "</TD><TD>Superfamily Name</TD><TD>Uniprot</TD><TD>" +
            "Refseq</TD><TD>Entrez Gene</TD></TR>" +
            OutputDataFile.CRT;
    }

    /**
     * get the trailer text for this format
     * @return trailer text
     */
    public String getTrailer()
    {
        String stdTrailer = super.getTrailer();
        return "</TABLE>" + OutputDataFile.CRT + stdTrailer;
    }


    /**
     * format the given object in HTML
     * @param data object to format
     * @return formatted string
     */
    public String format(Object data)
    {
        String s= (String)data;
        String[] fields = s.split(OutputDataFile.TAB);
        String sfid = fields[0];
        String sfname = fields[1];
        String uniprot = fields[2];
        String refseq = fields[3];
        String locusid = fields[4].trim();

        String sfid_html = super.formatPIRSFAnchorTag(sfid);
        String uniprot_html = null;
        if (uniprot.length() > 2)
            uniprot_html = super.formatAccidList(uniprot.substring(1, uniprot.length() - 1));
        else
            uniprot_html = "&nbsp;";
        String refseq_html = null;
        if (refseq.length() > 2)
            refseq_html = super.formatAccidList(refseq.substring(1, refseq.length() - 1));
        else
            refseq_html = "&nbsp;";
        String locusid_html = null;
        if (locusid.length() > 2)
            locusid_html = super.formatEntrezGeneList(locusid.substring(1, locusid.length() - 1));
        else
            locusid_html = "&nbsp;";


        String rowstart = "<TR>" + OutputDataFile.CRT;
        String rowend = "</TR>" + OutputDataFile.CRT;

        String s2 = rowstart + "<TD>" + sfid_html + "</TD><TD>" + sfname +
            "</TD><TD>" + uniprot_html +
            "</TD><TD>" + refseq_html +
            "</TD><TD>" + locusid_html +"</TD>" + rowend;

        return s2;
    }

    /**
     * required by the HTMLFormatter base class, but not used
     */
    public void postprocess() {}
    /**
     * required by the HTMLFormatter base class, but not used
     */
    public void preprocess() {}


}
