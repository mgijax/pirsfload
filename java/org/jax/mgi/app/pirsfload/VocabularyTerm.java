package org.jax.mgi.app.pirsfload;

/**
 * a plain old java object for storing vocabulary term information
 * @has a vocabulary term and an accession id for the term
 * @does nothing
 * @company Jackson Laboratory
 * @author M Walker
 *
 */

public class VocabularyTerm {

    private String term = null;
    private String accid = null;

    /**
     * constructor
     * @param term the vocabulary term
     * @param accid the accession id of the term
     */
    public VocabularyTerm(String term, String accid) {
        this.term = term;
        this.accid = accid;
    }

    /**
     * override of toString method from Object class
     * @return the string representation
     */
    public String toString()
    {
        return this.term + "\t" + this.accid + "\tnon-obsolete\t\t\t\t\t";
    }

}