package org.jax.mgi.app.pirsfload;

import org.jax.mgi.shr.cache.CacheException;
import org.jax.mgi.shr.cache.KeyValue;
import org.jax.mgi.shr.cache.FullCachedLookup;
import org.jax.mgi.shr.dbutils.SQLDataManager;
import org.jax.mgi.shr.dbutils.SQLDataManagerFactory;
import org.jax.mgi.dbs.SchemaConstants;
import org.jax.mgi.shr.dbutils.RowDataInterpreter;
import org.jax.mgi.shr.dbutils.DBException;
import org.jax.mgi.shr.dbutils.RowReference;
import org.jax.mgi.shr.config.ConfigException;

/**
 * is full cached lookup for marker associations to protein sequences
 * @has internal cache
 * @does provides a lookup to the cache
 * @company Jackson Laboratory
 * @author M Walker
 *
 */

public class ProteinSeqLookup extends FullCachedLookup {

    /**
     * constructor
     * @throws ConfigException thrown if there is an error accessing the
     * configuration
     * @throws DBException thrown if there is an error accessing the
     * database
     * @throws CacheException thrown if there is an error accessing the
     * cache
     */
    public ProteinSeqLookup()
    throws ConfigException, DBException, CacheException
    {
        super(SQLDataManagerFactory.getShared(SchemaConstants.MGD));
    }
    /**
     * lookup a marker associated to a given swissprot, trembl or
     * refseq sequence
     * @param seq the protein sequence
     * @return the associated marker
     * @throws DBException thrown if there is an error accessing the database
     * @throws CacheException thrown if there is an exception accessing the
     * cache
     */
    public Marker lookup(String seq)
    throws DBException, CacheException
    {
        return (Marker)super.lookupNullsOk(seq);
    }
    /**
     * get the query for fully initializing the cache
     * marker/protein sequences
     * @return the query for fully initializing the cache
     */
    public String getFullInitQuery()
    {
        return
            "select proteinid = a1.accID, mgiid = a2.accID, markerKey = a2._Object_key " +
            "from ACC_Accession a1, ACC_Accession a2, MRK_Marker m " +
            "where a1._LogicalDB_key in (41, 13, 27) " +
            "and a1._MGIType_key = 2 " +
            "and a1._Object_key = a2._Object_key " +
            "and a2.private = 0  " +
            "and a2._LogicalDB_key = 1 " +
            "and a2._MGIType_key = 2 " +
            "and a2.preferred = 1 " +
            "and a1._Object_key = m._Marker_key " +
            "and m._Organism_key = 1";

    }
    /**
     * get the RowDataInterpreter object for interpreting the query results
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
            return new KeyValue(row.getString("proteinid"), marker);
        }
    }

}
