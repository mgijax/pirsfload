package org.jax.mgi.app.pirsfload;

import org.jax.mgi.shr.cache.CacheException;
import org.jax.mgi.shr.cache.KeyValue;
import org.jax.mgi.shr.cache.FullCachedLookup;
import org.jax.mgi.shr.dbutils.SQLDataManager;
import org.jax.mgi.shr.dbutils.SQLDataManagerFactory;
import org.jax.mgi.dbs.SchemaConstants;
import org.jax.mgi.dbs.mgd.LogicalDBConstants;
import org.jax.mgi.shr.dbutils.RowDataInterpreter;
import org.jax.mgi.shr.dbutils.DBException;
import org.jax.mgi.shr.dbutils.RowReference;
import org.jax.mgi.shr.config.ConfigException;

/**
 *
 * is a FullCachedLookup storing EntrezGene associations to markers
 * @has internal cache of Entrez Gene to marker associations
 * @does provides a lookup for accessing the cache
 * @company Jackson Laboratory
 * @author M Walker
 *
 */

public class EntrezGeneLookup extends FullCachedLookup {

    /**
     * constructor
     * @throws ConfigException thrown if there is an error accessing the
     * configuration
     * @throws DBException thrown if there is an error accessing the database
     * @throws CacheException thrown if there is an error accessing the
     * cache
     */
    public EntrezGeneLookup()
    throws ConfigException, DBException, CacheException
    {
        super(SQLDataManagerFactory.getShared(SchemaConstants.MGD));
    }

    /**
     * look up an associated marker to a given Entrez Gene id
     * @param entrezid the Entrez Gene id
     * @return the associated marker
     * @throws DBException thrown if there is an error accessing the database
     * @throws CacheException thrown if there is an error accessing the
     * configuration
     */
    public Marker lookup(String entrezid)
    throws DBException, CacheException
    {
        return (Marker)super.lookupNullsOk(entrezid);
    }

    /**
     * get the query for fully initializing the cache
     * mouse markers annotated to EntrezGene
     * @return the initialization query
     */
    public String getFullInitQuery()
    {
        return
            "select a1.accID as entrezid, a2.accID as mgiid, a2._Object_key as markerKey " +
            "from ACC_Accession a1, ACC_Accession a2, MRK_Marker m " +
            "where a1._LogicalDB_key = " + LogicalDBConstants.ENTREZ_GENE + " " +
            "and a1._MGIType_key = 2 " +
            "and a1.preferred = 1 " +
            "and a1._Object_key = a2._Object_key " +
            "and a2._MGIType_key = 2 " +
            "and a2._LogicalDB_key = 1 " +
            "and a2.preferred = 1 " +
            "and a2.prefixPart = 'MGI:' " +
            "and a1._Object_key = m._Marker_key " +
	    "and m._Organism_key = 1 ";
    }

    /**
     * get the RowDataInterpreter for interpreting initialization query
     * @return the RowDataInterpreter
     */
    public RowDataInterpreter getRowDataInterpreter()
    {
        return new Interpreter();
    }

    private class Interpreter implements RowDataInterpreter
    {
        public Object interpret(RowReference row)
        throws DBException
        {
            Marker marker =
                new Marker(row.getString("mgiid"),
                           row.getInt("markerKey").intValue());
            return new KeyValue(row.getString("entrezid"), marker);
        }
    }

}
