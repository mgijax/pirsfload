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


public class ProteinSeqLookup extends FullCachedLookup {

    public ProteinSeqLookup()
    throws ConfigException, DBException, CacheException
    {
        super(SQLDataManagerFactory.getShared(SchemaConstants.MGD));
    }
    public Marker lookup(String seq)
    throws DBException, CacheException
    {
        return (Marker)super.lookupNullsOk(seq);
    }
    public String getFullInitQuery()
    {
        return
            "select a.accid as 'proteinAccid', a2.accid as 'markerAccid' ," +
            "       t.name as 'markerType', m._marker_key as 'markerKey' " +
            "from mrk_marker m, acc_accession a, acc_accession a2, " +
            "     mrk_types t " +
            "where  a._logicaldb_key in (41, 13, 27) " +
            "and a._mgitype_key = 2 " +
            "and a._object_key = m._marker_key " +
            "and m._organism_key = 1  " +
            "and a2._object_key = m._marker_key " +
            "and a2.private = 0  " +
            "and a2._logicaldb_key = 1 " +
            "and a2._mgitype_key = 2 " +
            "and t._marker_type_key = m._marker_type_key";

    }
    public RowDataInterpreter getRowDataInterpreter()
    {
        return new Interpreter();
    }

    public class Interpreter implements RowDataInterpreter
    {
        public Object interpret(RowReference row)
        throws DBException
        {
            Marker marker =
                new Marker(row.getString("markerAccid"),
                           row.getString("markerType"),
                           row.getInt("markerKey").intValue());
            return new KeyValue(row.getString("proteinAccid"), marker);
        }
    }

}