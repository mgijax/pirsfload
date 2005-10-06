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
            "select a.accID as 'proteinAccid', a2.accID as 'markerAccid' ," +
            "       t.name as 'markerType', m._Marker_key as 'markerKey' " +
            "from MRK_Marker m, ACC_Accession a, ACC_Accession a2, " +
            "     MRK_Types t " +
            "where  a._LogicalDB_key in (41, 13, 27) " +
            "and a._MGIType_key = 2 " +
            "and a._Object_key = m._Marker_key " +
            "and m._Organism_key = 1  " +
            "and a2._Object_key = m._Marker_key " +
            "and a2.private = 0  " +
            "and a2._LogicalDB_key = 1 " +
            "and a2._MGIType_key = 2 " +
            "and a2.preferred = 1 " +
            "and t._Marker_Type_key = m._Marker_Type_key";

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