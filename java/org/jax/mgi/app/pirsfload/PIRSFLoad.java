package org.jax.mgi.app.pirsfload;

import java.util.*;
import java.io.File;

import org.jax.mgi.shr.ioutils.InputXMLDataFile;
import org.jax.mgi.shr.ioutils.XMLDataIterator;
import org.jax.mgi.shr.ioutils.XMLDataInterpreter;
import org.jax.mgi.shr.ioutils.OutputDataFile;
import org.jax.mgi.shr.datetime.DateTime;
import org.jax.mgi.shr.dbutils.BatchProcessor;
import org.jax.mgi.shr.dla.loader.DLALoader;
import org.jax.mgi.shr.dla.loader.DLALoaderException;
import org.jax.mgi.shr.exception.MGIException;
import org.jax.mgi.shr.dla.input.pir.PIRSFInputFile;
import org.jax.mgi.shr.dla.input.pir.PIRSFInputFile.PIRSFSuperFamily;
import org.jax.mgi.shr.ioutils.OutputManager;

/**
 * is a DLALoader for loading PIRSF superfamilies into the database
 * as vocabulary terms and loading marker annotations to these terms
 * based on the results of a mapping algorithm using associated sequences
 * @has nothing
 * @does reads the PIRSF iproclass file, maps the PIRSF superfamily data to
 * MGI mouse markers using an algorithm based on sequence associations and
 * writes the results to MGI as vocabulary and annotation records
 * @company Jackson Laboratory
 * @author M Walker
 *
 */

public class PIRSFLoad extends DLALoader
{
    private static final String NOTMAPPED = "NOTMAPPED";
    private static final String DISCREPANCY = "DISCREPANCY";

    private ProteinSeqLookup proteinLookup = null;
    private EntrezGeneLookup entrezGeneLookup = null;
    private OutputDataFile termfile = null;
    private OutputDataFile annotfile = null;
    private HashMap superfamilyToMarkerMap = new HashMap();
    private HashMap markerToSuperfamilyMap = new HashMap();

    /**
     * constructor
     * @throws DLALoaderException thrown if the super class cannot be
     * instantiated
     */
    public PIRSFLoad() throws DLALoaderException
    {
    }
    /**
     * initialize the internal structures used by this class
     * @assumes nothing
     * @effects internal structures including database caching is initialized
     * @throws MGIException thrown if there is an error during initialization
     */
    protected void initialize() throws MGIException
    {
        super.logger.logInfo("Opening report files");
        String basedir = super.dlaConfig.getReportsDir() + File.separator;

        termfile = new OutputDataFile(basedir + "termfile");
        annotfile = new OutputDataFile(basedir + "annotfile");

        super.logger.logInfo("Initializing cache");

        proteinLookup = new ProteinSeqLookup();
        proteinLookup.initCache();
	OutputDataFile proteinAssocFile = new OutputDataFile (basedir + "cache_proteinGeneAssoc.txt");
	proteinLookup.printCache(proteinAssocFile);
	proteinAssocFile.close();

        entrezGeneLookup = new EntrezGeneLookup();
        entrezGeneLookup.initCache();
	OutputDataFile egAssocFile = new OutputDataFile (basedir + "cache_entrezGeneAssoc.txt");
	entrezGeneLookup.printCache(egAssocFile);
	egAssocFile.close();

    }
    /**
     * closes files
     * @assumes nothing
     * @effects nothing
     * @throws nothing
     */
    protected void postprocess() throws MGIException
    {
        super.logger.logInfo("Closing report files");
        termfile.close();
        annotfile.close();
    }

    /**
     * @does nothing
     * @assumes nothing
     * @effects nothing
     * @throws nothing
     */
    protected void preprocess() throws MGIException
    {
	return;
    }

    /**
     * read the iproclass input file and run the mapping algorithm and creates
     * output data files
     * @assumes nothing
     * @effects the data will be created for loading vocabulary and annotations
     * into the database and output data files are created
     * @throws MGIException thrown if there is an error accessing the input
     * file or writing output data
     */
    protected void run() throws MGIException
    {
        PIRSFInputFile infile = new PIRSFInputFile();
        XMLDataIterator iterator = infile.getIterator();
        HashMap noMappings = new HashMap();
        HashSet noMappingsRevised = new HashSet();

	// for each PIRSF record...

	super.logger.logInfo("Iterating through PIRSF records");

        while (iterator.hasNext())
        {
            PIRSFSuperFamily sf = (PIRSFSuperFamily)iterator.next();

	    // skip records that have no name, or the name = id
	    // these are preliminary pirsf superfamilies

            if (sf.pirsfID.equals("unset") ||
                sf.pirsfName.equals("unset") ||
                sf.pirsfName.equals(sf.pirsfID) ||
                sf.pirsfName.equals(Constants.NOT_ASSIGNED))
                continue;

	    // lookup markers in MGI

            HashSet markers = new HashSet();
            markers = findMGIMarkers(sf, proteinLookup, entrezGeneLookup);

	    // if this PIRSF family does not map to any MGI markers....

            if (markers.size() == 0)
            {
		// store non-mapping information

                if (!noMappingsRevised.contains(sf.pirsfID))
                {
                    if (!noMappings.containsKey(sf.pirsfID))
                    {
                        NotMapped notMapped = new NotMapped(sf);
                        noMappings.put(sf.pirsfID, notMapped);
                    }
                    else
                    {
                        NotMapped notMapped = (NotMapped) noMappings.get(sf.pirsfID);
                        notMapped.addSequences(sf);
                    }
                }
                else
                {
                    String s = null;
                }
                continue;
            }

            if (noMappings.containsKey(sf.pirsfID))
            {
                noMappings.remove(sf.pirsfID);
                noMappingsRevised.add(sf.pirsfID);
            }

            SFMarkerAssoc mapped =
                (SFMarkerAssoc)superfamilyToMarkerMap.get(sf.pirsfID);
            if (mapped == null)
            {
                SFMarkerAssoc assoc = new SFMarkerAssoc(sf, markers);
                superfamilyToMarkerMap.put(sf.pirsfID, assoc);
            }
            else
            {
                mapped.addMarkers(markers);
            }

            for (Iterator i = markers.iterator(); i.hasNext();)
            {
                Marker m = (Marker)i.next();
                String thisMarkerAccid = m.getAccid();
                HashSet assocSuperFamilies =
                    (HashSet)markerToSuperfamilyMap.get(thisMarkerAccid);
                if (assocSuperFamilies == null)
                {
                    HashSet set = new HashSet();
                    set.add(sf.pirsfID);
                    markerToSuperfamilyMap.put(thisMarkerAccid, set);
                }
                else
                {
                    assocSuperFamilies.add(sf.pirsfID);
                }
            }
        } // end of PIRSF records

	super.logger.logInfo("Writing data to Term and Annotation files");

        HashSet knownDiscrepancies = new HashSet();
        for (Iterator i = noMappings.values().iterator(); i.hasNext();)
        {
            NotMapped notMapped = (NotMapped)i.next();
            OutputManager.writeln(NOTMAPPED, notMapped.toString());
        }

	// for each superfamily we want to load into MGI...

        for (Iterator i = superfamilyToMarkerMap.values().iterator();
             i.hasNext();)
        {
            SFMarkerAssoc mappedSuperfamily = (SFMarkerAssoc)i.next();
            PIRSFSuperFamily sf2 = mappedSuperfamily.getSuperFamily();

            HashSet markers = mappedSuperfamily.getMarkers();
            boolean oneToMany = false;
            for (Iterator j = markers.iterator(); j.hasNext();)
            {
                Marker m = (Marker)j.next();
                HashSet associatedSFs =
                    (HashSet)this.markerToSuperfamilyMap.get(m.getAccid());

		// if a marker maps to more than one superfamily, flag it

                if (associatedSFs.size() > 1)
                {
                    oneToMany = true;
                    if (!knownDiscrepancies.contains(m.getAccid()))
                    {
                        OutputManager.writeln(DISCREPANCY,
                                              m.getAccid() +
                                              OutputDataFile.TAB +
                                              associatedSFs.toString());
                        knownDiscrepancies.add(m.getAccid());
                    }
                    break;
                }
            }

	    // skip 1-to-N

            if (oneToMany)
                continue;

	    // write PIRSF term to term file

            termfile.writeln(sf2.pirsfName + "\t" + sf2.pirsfID + "\tcurrent\t\t\t\t\t");

	    // write PIRSF/Marker association to annotation file

            for (Iterator j = markers.iterator(); j.hasNext();)
            {
                Marker marker = (Marker)j.next();
		annotfile.writeln(sf2.pirsfID + "\t" + marker.getAccid() + "\t" +
			Constants.JNUMBER + "\t" + Constants.EVIDENCE + "\t" +
			super.dlaConfig.getJobstreamName() + "\t" +
			DateTime.getCurrentDate() + "\t");
	    }
        }
    }

    /**
    *
    * looks up Marker in MGI by refseq ID or entrezgene ID
    *
    */
    private HashSet findMGIMarkers(PIRSFSuperFamily sf,
                                   ProteinSeqLookup proteinLookup,
                                   EntrezGeneLookup entrezGeneLookup)
    throws MGIException
    {
        HashSet markers = new HashSet();

	// lookup marker by uniprot id

        for (Iterator i = sf.uniprot.iterator(); i.hasNext();)
        {
            String protein = (String)i.next();
            Marker marker = proteinLookup.lookup(protein);
            if (marker != null)
                markers.add(marker);
        }

	// lookup marker by refseq id

        if (markers.size() == 0 && !sf.refseqID.equals("unset"))
	{
            for (Iterator i = sf.refseqID.iterator(); i.hasNext();)
            {
                String refseq = (String)i.next();
                Marker marker = proteinLookup.lookup(refseq);
                if (marker != null)
                    markers.add(marker);
	    }
        }

	// lookup marker by entrezgene id

        if (markers.size() == 0 && !sf.entrezID.equals("unset"))
        {
            Marker marker = entrezGeneLookup.lookup(sf.entrezID);
            if (marker != null)
                markers.add(marker);
        }
        return markers;
    }


    private class SFMarkerAssoc
    {
        private PIRSFSuperFamily superfamily = null;
        private HashSet markers = null;

        public SFMarkerAssoc(PIRSFSuperFamily superfamily,
                             HashSet markers)
        {
            this.superfamily = superfamily;
            this.markers = markers;
        }

        public void addMarkers(HashSet markers)
        {
            this.markers.addAll(markers);
        }

        public PIRSFSuperFamily getSuperFamily()
        {
            return this.superfamily;
        }

        public HashSet getMarkers()
        {
            return this.markers;
        }
    }

    private class NotMapped
    {
        private String id = null;
        private String name = null;
        private HashSet uniprot = new HashSet();
        private HashSet refseq = new HashSet();
        private HashSet entrezGene = new HashSet();

        public NotMapped(PIRSFSuperFamily sf)
        {
            this.id = sf.pirsfID;
            this.name = sf.pirsfName;
            this.uniprot.addAll(sf.uniprot);
            this.refseq.addAll(sf.refseqID);
            if (!sf.entrezID.equals("unset"))
                this.entrezGene.add(sf.entrezID);
        }

        public void addSequences(PIRSFSuperFamily sf)
        {
            this.uniprot.addAll(sf.uniprot);
            this.refseq.addAll(sf.refseqID);
            if (!sf.entrezID.equals("unset"))
                this.entrezGene.add(sf.entrezID);
        }

        public int hashCode()
        {
            return this.id.hashCode();
        }

        public boolean equals(Object o)
        {
            if (!(o instanceof NotMapped))
                return false;
            NotMapped notMapped = (NotMapped)o;
            if (notMapped.id.equals(this.id))
                return true;
            else
                return false;
        }

        public String toString()
        {
            return this.id + OutputDataFile.TAB +
                this.name + OutputDataFile.TAB +
                this.uniprot.toString() + OutputDataFile.TAB +
                this.refseq.toString() + OutputDataFile.TAB +
                this.entrezGene.toString();
        }

    }
}
