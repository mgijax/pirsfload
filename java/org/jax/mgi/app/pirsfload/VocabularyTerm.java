package org.jax.mgi.app.pirsfload;

public class VocabularyTerm {

    private String term = null;
    private String accid = null;

    public VocabularyTerm(String term, String accid) {
        this.term = term;
        this.accid = accid;
    }

    public String toString()
    {
        return this.term + "\t" + this.accid + "\tnon-obsolete\t\t\t\t\t";
    }

}