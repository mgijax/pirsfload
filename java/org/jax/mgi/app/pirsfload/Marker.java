package org.jax.mgi.app.pirsfload;



public class Marker
{
    private String accid = null;
    private String type = null;
    private int key = 0;

    public Marker(String accid, String type, int key)
    {
        this.accid = accid;
        this.type = type;
        this.key = key;
    }

    public String getAccid()
    {
        return this.accid;
    }

    public String getType()
    {
        return this.type;
    }

    public int getKey()
    {
        return this.key;
    }

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

    public int hashCode()
    {
        return (new Integer(this.getKey())).hashCode();
    }

    public String toString()
    {
        return this.getAccid() + ";" + this.getKey();
    }

}
