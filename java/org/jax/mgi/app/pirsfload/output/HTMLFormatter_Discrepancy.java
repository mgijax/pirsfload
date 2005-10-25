package org.jax.mgi.app.pirsfload.output;

import org.jax.mgi.shr.ioutils.OutputDataFile;
import org.jax.mgi.shr.config.ConfigException;
import org.jax.mgi.shr.dla.output.HTMLFormatter;

/**
 * is a class which extends HTMLFormatter from the lib_java_dla product and
 * provides functionality for rendering a discrepancy report in HTML
 * @has nothing
 * @does formats output in HTML
 * @company The Jackson Laboratory
 * @author M Walker
 *
 */


public class HTMLFormatter_Discrepancy extends HTMLFormatter
{

    /**
     * counstructor
     * @throws ConfigException thrown if there is an error accessing the
     * configuration
     */
    public HTMLFormatter_Discrepancy()
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
            "<TR " + HTMLFormatterConstants.TR_ATTRIBUTES + "><TD>MGI Marker" +
            "</TD><TD>Mapped Superfamilies</TD></TR>" +
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
        String marker = fields[0];
        String sflist = fields[1];

        String marker_html = super.formatAccidAnchorTag(marker);
        String sflist_html =
            super.formatPIRSFList(sflist.substring(1, sflist.length() - 2));

        String rowstart = "<TR>" + OutputDataFile.CRT;
        String rowend = "</TR>" + OutputDataFile.CRT;

        String s2 = rowstart + "<TD>" + marker_html + "</TD><TD>" +
            sflist_html + "</TD>" + rowend;

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
