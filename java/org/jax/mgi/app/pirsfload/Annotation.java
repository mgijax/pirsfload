package org.jax.mgi.app.pirsfload;

import org.jax.mgi.shr.datetime.DateTime;

/**
 * is a class that represents an annotation between a marker and a
 * vocabulary term
 * @has instance variables to represent the annotation
 * @does nothing
 * @company Jackson Laboratory
 * @author M Walker
 *
 */

public class Annotation {

    /**
     * the term key
     */
    public String termid = null;
    /**
     * the term value
     */
    public String term = null;
    /**
     * the marker key
     */
    public String markerid = null;
    /**
     * the jnumbr for the annotation
     */
    public String jnum = null;
    /**
     * the evidence string
     */
    public String evidence = null;
    /**
     * the name of the user responsible for creating the annotation
     */
    public String user = null;

    /**
     * constructor
     * @param termid the term key
     * @param markerid the marker key
     * @param jnum the jnumber
     * @param evidence the evidence string
     * @param user the user responsible for creating the annotation
     */
    public Annotation(String termid, String markerid,
                      String jnum, String evidence, String user)
    {
        this.termid = termid;
        this.markerid = markerid;
        this.jnum = jnum;
        this.evidence = evidence;
        this.user = user;
    }

    /**
     * override of the Object method toString()
     * @return the string representation
     */
    public String toString()
    {
        return this.termid + "\t" + this.markerid + "\t" + this.jnum + "\t" +
            this.evidence + "\t\t\t" + this.user + "\t" +
            DateTime.getCurrentDate() + "\t";
    }

}