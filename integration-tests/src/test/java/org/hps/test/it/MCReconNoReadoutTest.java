package org.hps.test.it;

import java.io.File;

import org.hps.job.JobManager;
import org.hps.util.test.TestUtil;
import org.lcsim.util.test.TestUtil.TestOutputFile;

import junit.framework.TestCase;

/**
 * Test MC recon steering file without readout simulation.
 *
 * @author Jeremy McCormick, SLAC
 */
public class MCReconNoReadoutTest extends TestCase {

    static String TEST_FILE = "e-_1.056GeV_SLIC-v05-00-00_Geant4-v10-01-02_QGSP_BERT_HPS-EngRun2015-Nominal-v3.slcio";

    /**
     * List of steering files to run.
     */
    static String[] STEERING_FILES = {
        "/org/hps/steering/readout/HPSReconNoReadout.lcsim"
    };

    /**
     * Test recon steering files.
     * @throws Exception if any error occurs running the recon job
     */
    public void testSteeringFiles() throws Exception {

        File inputFile = TestUtil.downloadTestFile(TEST_FILE);

        for (String steeringFile : STEERING_FILES) {
            System.out.println("running steering file " + steeringFile);
            File outputFile = new TestOutputFile(new File(steeringFile).getName().replace(".lcsim", ""));
            JobManager job = new JobManager();
            job.addVariableDefinition("outputFile", outputFile.getPath());
            job.addInputFile(inputFile);
            job.setup(steeringFile);
            job.run();
        }
    }
}
