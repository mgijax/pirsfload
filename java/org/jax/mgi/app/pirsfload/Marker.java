package org.jax.mgi.app.pirsfload;

/**
 * a plain old java object for storing marker information
 * @has marker attributes
 * @does  nothing
 * @company Jackson Laboratory
 * @author M Walker
 *
 */

public class Marker
{
    private String accid = null;
    private String type = null;
    private int key = 0;

    /**
     * constructor
     * @param accid the accession id of the marker
     * @param type the type of marker
     * @param key the databse key of object
     */
    public Marker(String accid, String type, int key)
    {
        this.accid = accid;
        this.type = type;
        this.key = key;
    }

    /**
     * get the accession id of marker
     * @return the accession id of marker
     */
    public String getAccid()
    {
        return this.accid;
    }

    /**
     * get the type of marker as stored in database
     * @return the type of marker
     */
    public String getType()
    {
        return this.type;
    }

    /**
     * get the database key of the marker
     * @return the database key
     */
    public int getKey()
    {
        return this.key;
    }

    /**
     * override of equals method from Object class
     * @param o the object to compare to
     * @return true if the two objects are equal, false otherwise
     */
    public boolean equals(Object o)
    {
        if (!(o instanceof Marker))
            return false;
        Marker m = (Marker)o;
        if (this.key == m.getKey())
            return true;
        else
            return false;
    }

    /**
     * override of hashCode method from Object class
     * @return the object hash code
     */
    public int hashCode()
    {
        return (new Integer(this.getKey())).hashCode();
    }

    /**
     * override of toString method from Object class
     * @return the string representation of this instance
     */
    public String toString()
    {
        return this.getAccid() + ";" + this.getKey();
    }

}
