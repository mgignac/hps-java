package org.hps.recon.ecal.cluster;

import java.util.List;

import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.EventHeader;
import org.lcsim.event.base.BaseCalorimeterHit;
import org.lcsim.util.Driver;

/**
 * This driver specifically alters the time offsets of each crystal for 2015 data relative to pass 6. This is done
 * because the original calibration had a few crystals that were out of time and was calibrated with the first RF
 * signal. These offsets make all crystals relative to the second rf time which is used to set the event time for a
 * triggered cluster. TODO: This should be combined with pass6 and put into the database after the final tweak pass is
 * complete.
 */
public class ClusterHitTCorrDriver extends Driver {

    // These offsets are relative to what was used from the database for pass6
    // ix+23, by iy+5. If in top, subtract 1 from iy. If in right, subtract 1 from ix.
    private static final double[][] OFFSETS = {
            {0.731636, -0.1625, -0.1100132, -0.2097527, -0.070508, -0.0985568, 0.03064861, 0.0659009, -0.0809285,
                    -0.155452, 0.0771659, -0.1182397, -0.3471135, 0.1241333, 0.2765786, 0.0183527, -0.1502422,
                    -0.31602266, -0.2979307, -0.247525, 0.1731786, 0.0325443, 0.1852708, -0.1025, 0.127856, 0.1908519,
                    -0.1184372, -0.1757908, 0.15461859, -0.0424439, 0.0975, 0.008791, -0.1794873, -0.1065643,
                    -0.2373617, -0.0575, -0.2476379, -0.2075, -0.2575, -0.1051946, -0.1905678, 0.1225, -0.6327995,
                    -0.20606285, 0.7225, -1.6225},
            {-0.804537, -0.2575, -0.2025, -0.0807391, 0.1975, -0.168898, -0.0771997, 0.1375, 0.0037381, -0.181406,
                    -0.0092101, -0.2825, -0.1308451, 0.3631068, 0.1254114, -0.1954517, 0.0911673, 0.10240356,
                    -0.3023037, 0.1355385, -0.0056466, -0.413843, -0.1875, 0.0926308, -0.1479074, 0.06556973, -0.0725,
                    -0.25387882, 0.127806, -0.0295907, -0.1925, 0.06278483, 0.0711183, -0.0411272, -0.0400874,
                    -0.1625113, 0.1590156, 0.0034054, -0.1999182, 0.00257759, -0.3575, -0.1925, -0.2993746, -0.2825,
                    -0.6041969, -0.7925},
            {0.3275, -0.1663107, -0.0225, -0.1228114, 0.1470665, -0.1036745, -0.1025, -0.2047299, 0.0638829,
                    -0.16133117, 0.1632783, -0.0444952, 0.2680172, 0.3475548, 0.0295439, -0.6875008, -0.061364,
                    -0.0796302, -0.1457953, 0.1598082, 0.0525, -0.0257534, -0.0825, 0.0366608, -0.196249, -0.0975,
                    -0.0946822, 0.10830705, 0.147723, 0.1434783, 0.07348083, 0.1207932, -0.0673457, -0.0126779,
                    -0.1352822, -0.0575, -0.110774, -0.0759109, 0.0377492, -0.0225, 0.1080423, 0.0425, -0.2725,
                    -0.1684038, -0.5088469, -0.0925},
            {-0.4625, 0.1775, -0.1725, 0.1225, -0.0688769, -0.0825, 0.0413575, 0.1200126, 0.1254245, -0.0512794,
                    -0.0608441, -0.1845481, -0.392909, -0.1240293, 0.4066574, 0.4194054, -0.2511903, -0.2825, -0.1625,
                    -0.0475, -0.5575, -0.2675, -0.1325, -0.1325, 0.206819, -0.0575, -0.0222567, 0.1475, 0.0825,
                    0.060201, 0.1975, 0.0308863, 0.1025, -0.0225, -0.013667, -0.1670427, 0.0896259, -0.115672,
                    0.0252131, -0.1975, -0.107063, 0.4275, -0.0075, -0.1110737, -0.4725, -0.5925},
            {-0.083547, -0.132534, 0.014395, -0.1225, 0.149054, -0.3675, 0.208037, -0.3625, 0.0121713, -0.08756715,
                    0.1986281, -0.1412442, 0.0557431, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.2875, -0.215435, -0.0775,
                    -0.1579732, -0.00370849, 0.0925, -0.3175, 0.1575, 0.0175, 0.068356, 0.0275, 0.0796036, -0.0552551,
                    0.018586, -0.0571336, 0.0608483, -0.247937552, 0.0272649, 0.03010379, -0.0783827, -0.0075, -0.3025,
                    0.3875, -0.58198},
            {-0.6075, -0.093355, -0.0969671, -0.2175, 0.0325, -0.031956, -0.0829451, 0.0503807, -0.2241145, -0.0359618,
                    0.0506417, -0.1239427, 0.1893141, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.4325, -0.2675, -0.217103,
                    -0.0616863, 0.1101912, -0.060917, -0.0835527, 0.0575, 0.0475, -0.009147, 0.0578838, 0.0069627,
                    0.0408659, -0.0513671, -0.0112259, 0.1110902, -0.1720962, -0.1180274, 0.06871907, -0.0025,
                    -0.0862501, -0.2354536, -0.3075, -1.417194},
            {0.1194706, -0.08041988, -0.1077863, 0.1481893, -0.0641751, -0.051378, -0.04136388, -0.1575, 0.0081597,
                    -0.0701061, 0.0086603, -0.14962, -0.1954328, 0.0148327, 0.2521256, -0.16912103, -0.1175, 0.0375,
                    0.8873608, -0.224069, -0.0128892, -0.2875, -0.1075, 0.0375, -0.058814, -0.0133626, 0.1475,
                    0.173321, 0.0825, 0.040546, 0.041709, 0.0251368, -0.0575, -0.1136101, -0.0301162, -0.1125, 0.0225,
                    -0.185068, -0.1474075, -0.0725, -1.6075, -0.0582114, -0.2234694, -0.2125, -0.1175, -0.172385},
            {-0.0875, 0.136129, -0.0125, -0.0875, -0.2097815, -0.1595193, -0.052444, -0.033848, 0.0675, 0.201675,
                    -0.0655129, -0.1044728, -0.0430091, 0.0122804, 0.1810478, -0.14868372, -0.10179017, -0.0092951,
                    -0.105502, -0.1542615, -0.0871289, 0.0575, -0.0925, -0.137417, -0.0774963, -0.0882555, 0.074455,
                    0.1289489, 0.09836, -0.1329085, -0.0347011, -0.0442986, -0.0592823, 0.0252948, -0.0615604,
                    -0.0998687, -0.004105, -0.2175, -0.1499061, -0.0782729, -0.0210423, -0.2695361, -0.1765748,
                    0.2169724, -0.2325, -0.1125},
            {-0.0975, 0.0975, 0.1025, 0.0175, -0.11354966, -0.23675624, -0.073492, 0.0321982, -0.1621539, 0.2106517,
                    -0.0172448, -0.0025, -0.1232094, 0.345042, -0.2896987, -0.0775987, 0.1906324, -0.1569566,
                    -0.10689543, 0.0059499, -0.3087685, -0.1575, -0.0325927, -0.133885, -0.1925, 0.0075, 0.0075,
                    0.091707, -0.004635, -0.1644798, -0.0442213, -0.2575, -0.174065, -0.3025, 0.0044777, -0.1625,
                    -0.1368078, -0.0599044, 0.0287408, -0.0914427, -0.2525, -0.189499, -0.2271684, -0.0200413,
                    -0.976364, -0.3075},
            {-0.297055, 0.008139286, -0.246493, 0.0675, -0.3725, -0.0075, -0.1475, -0.1075, -0.1125, -0.0143693,
                    0.0675, -0.2972707, 0.0116054, -0.1275, -0.1975, -0.062049, -0.0547311, -0.0377056, -0.1125,
                    -0.1845299, -0.1075, 0.1969785, -0.2471708, -0.0764265, 0.184715, 0.067933, -0.082235, -0.0425,
                    -0.2225, 0.0756024, -0.1075, -0.0371792, -0.0775, -0.111122, -0.15117577, -0.0425, -0.1575,
                    -0.0008167, -0.1500484, -0.4225, -0.0706456, -0.0326774, -0.3875, -0.3925, -0.4975, -0.6075}};

    // Call the offset, correct the time, and set the time
    public void process(EventHeader event) {

        // Get the hits in the event
        List<CalorimeterHit> hits = event.get(CalorimeterHit.class, "EcalCalHits");

        for (CalorimeterHit iHit : hits) {
            double oldT = iHit.getTime();
            int ix = iHit.getIdentifierFieldValue("ix");
            int iy = iHit.getIdentifierFieldValue("iy");

            if (ix >= 0) {
                ix -= 1;
            }
            if (iy >= 0) {
                iy -= 1;
            }
            double toffset = OFFSETS[iy + 5][ix + 23];
            ((BaseCalorimeterHit) iHit).setTime(oldT - toffset);
            // System.out.println("old time\t"+oldT+"\t offset\t"+toffset+"\tix\t"+ix+"\tiy\t"+iy);

        }
    }
}
