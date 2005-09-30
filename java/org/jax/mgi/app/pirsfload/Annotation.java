package org.jax.mgi.app.pirsfload;

import org.jax.mgi.shr.datetime.DateTime;



public class Annotation {

    public String termid = null;
    public String term = null;
    public String markerid = null;
    public String jnum = null;
    public String evidence = null;
    public String user = null;

    public Annotation(String termid, String markerid,
                      String jnum, String evidence, String user)
    {
        this.termid = termid;
        this.markerid = markerid;
        this.jnum = jnum;
        this.evidence = evidence;
        this.user = user;
    }

    public String toString()
    {
        return this.termid + "\t" + this.markerid + "\t" + this.jnum + "\t" +
            this.evidence + "\t\t\t" + this.user + "\t" +
            DateTime.getCurrentDate() + "\t";
    }

}