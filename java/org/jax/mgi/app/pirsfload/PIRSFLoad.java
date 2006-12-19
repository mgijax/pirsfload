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
    private ProteinSeqLookup proteinLookup = null;
    private EntrezGeneLookup entrezGeneLookup = null;
    private OutputDataFile termfile = null;
    private OutputDataFile annotfile = null;
    private OutputDataFile onetomanyfile = null;
    private OutputDataFile proteinAssocFile = null;
    private OutputDataFile egAssocFile = null;
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
        onetomanyfile = new OutputDataFile(basedir + "oneToMany.txt");
	proteinAssocFile = new OutputDataFile (basedir + "cache_proteinGeneAssoc.txt");
	egAssocFile = new OutputDataFile (basedir + "cache_entrezGeneAssoc.txt");

        super.logger.logInfo("Initializing cache");

        proteinLookup = new ProteinSeqLookup();
        proteinLookup.initCache();
	proteinLookup.printCache(proteinAssocFile);
	proteinAssocFile.close();

        entrezGeneLookup = new EntrezGeneLookup();
        entrezGeneLookup.initCache();
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
	onetomanyfile.close();
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

	// for each PIRSF record...

	super.logger.logInfo("Iterating through PIRSF records");

        while (iterator.hasNext())
        {
            PIRSFSuperFamily sf = (PIRSFSuperFamily)iterator.next();

	    // skip records that have no name
	    // or the name = id
	    // or name is any pirsf id
	    // these are preliminary pirsf superfamilies

            if (sf.pirsfID.equals("unset") ||
		sf.pirsfName.startsWith("SF") ||
		sf.pirsfName.startsWith("PIRSF") ||
                sf.pirsfName.equals("unset") ||
                sf.pirsfName.equals(sf.pirsfID) ||
                sf.pirsfName.equals(Constants.NOT_ASSIGNED))
                continue;

	    // lookup markers in MGI

            HashSet markers = new HashSet();
            markers = findMGIMarkers(sf, proteinLookup, entrezGeneLookup);

	    // store pirsf/marker associations

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

	    // store marker/pirsf associations

            for (Iterator i = markers.iterator(); i.hasNext();)
            {
                Marker marker = (Marker)i.next();
                HashSet associatedSFs = (HashSet)markerToSuperfamilyMap.get(marker.getAccid());
                if (associatedSFs == null)
                {
                    HashSet set = new HashSet();
                    set.add(sf.pirsfID);
                    markerToSuperfamilyMap.put(marker.getAccid(), set);
                }
                else
                {
                    associatedSFs.add(sf.pirsfID);
                }
            }
        } // end of PIRSF records

	super.logger.logInfo("Writing data to Term and Annotation files");

	// for each superfamily

        for (Iterator i = superfamilyToMarkerMap.values().iterator(); i.hasNext();)
        {
            SFMarkerAssoc mappedSuperfamily = (SFMarkerAssoc)i.next();
            PIRSFSuperFamily sf = mappedSuperfamily.getSuperFamily();
	    HashSet markers = mappedSuperfamily.getMarkers();

	    // write PIRSF term to term file

            termfile.writeln(sf.pirsfName + "\t" + sf.pirsfID + "\tcurrent\t\t\t\t\t");

	    // write PIRSF/Marker association to annotation file

            for (Iterator j = markers.iterator(); j.hasNext();)
            {
                Marker marker = (Marker)j.next();

		// if a marker maps to more than one superfamily, skip it

                HashSet associatedSFs = (HashSet)markerToSuperfamilyMap.get(marker.getAccid());
                if (associatedSFs.size() > 1)
		{
                    onetomanyfile.writeln(marker.getAccid() + OutputDataFile.TAB + associatedSFs.toString());
		    continue;
                }

		annotfile.writeln(sf.pirsfID + "\t" + marker.getAccid() + "\t" +
			Constants.JNUMBER + "\t" + Constants.EVIDENCE + "\t\t\t" +
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
}
