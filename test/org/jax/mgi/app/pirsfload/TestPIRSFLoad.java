package org.jax.mgi.app.pirsfload;

import junit.framework.*;
import org.jax.mgi.shr.exception.*;

public class TestPIRSFLoad
    extends TestCase
{
    private PIRSFLoad pIRSFLoad = null;

    public static void main(String[] args)
    throws Exception
    {
        PIRSFLoad load = new PIRSFLoad();
        load.run();
    }

    public TestPIRSFLoad(String name)
    {
        super(name);
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();
        /**@todo verify the constructors*/
        pIRSFLoad = new PIRSFLoad();
    }

    protected void tearDown()
        throws Exception
    {
        pIRSFLoad = null;
        super.tearDown();
    }

    public void testRun()
        throws MGIException
    {
        pIRSFLoad.load();
    }

}
