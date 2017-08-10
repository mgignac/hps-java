package org.hps.recon.tracking;

import java.util.List;

import org.lcsim.constants.Constants;
import org.lcsim.fit.threepointcircle.CircleFit;
import org.lcsim.fit.helicaltrack.HelicalTrackCross;
import org.lcsim.fit.helicaltrack.HelicalTrackHit;
import org.lcsim.fit.twopointcircle.TwoPointCircleFit;
import org.lcsim.fit.twopointcircle.TwoPointLineFit;
import org.lcsim.recon.tracking.seedtracker.SeedCandidate;
import org.lcsim.recon.tracking.seedtracker.SeedStrategy;
import org.lcsim.recon.tracking.seedtracker.diagnostic.ISeedTrackerDiagnostics;

/**
 * HPS version of LCSim class
 * @author Richard Partridge
 * @author Miriam Diamond <mdiamond@slac.stanford.edu>
 * @version $Id: 2.0 07/07/17$
 */
public class FastCheck extends org.lcsim.recon.tracking.seedtracker.FastCheck {

    private double _nsigErr;

    public FastCheck(SeedStrategy strategy, double bfield, ISeedTrackerDiagnostics diag) {
        super(strategy, bfield, diag);
        setNSigErr(3);
    }

    public double getNSigErr() {
        return _nsigErr;
    }

    public void setNSigErr(double input) {
        _nsigErr = input;
    }

    private double calculateMSerror(double hit1x, double hit2x, double hit3x, double p) {
        // angle approximation
        // radlength=0.003417, angle = (0.0136 / p) * Math.sqrt(radlength) * (1.0 + 0.038 * Math.log(radlength))
        double angle = 0.00062343 / p;

        double dist1 = hit1x - hit2x;
        double dist2 = hit2x - hit3x;
        double MSerr = angle * Math.sqrt(dist1 * dist1 + dist2 * dist2);

        return MSerr;
    }

    private double estimateMomentum(double slope, double rcurve) {
        return Math.sqrt(1 + slope * slope) * _bfield * Constants.fieldConversion * rcurve;
    }

    @Override
    public boolean CheckHitSeed(HelicalTrackHit hit, SeedCandidate seed) {
        if (super.getSkipChecks())
            return true;

        CorrectHitPosition(hit, seed);
        if (hit.r() < super.getDMax())
            return false;

        //  Check the hit against each hit in the seed
        for (HelicalTrackHit hit2 : seed.getHits()) {

            // Check that hits are outside the maximum DCA
            CorrectHitPosition(hit2, seed);
            if (hit2.r() < super.getDMax())
                continue;

            if (!TwoPointCircleCheckAlgorithm(hit, hit2))
                return false;
        }

        return true;
    }

    public boolean TwoPointCircleCheck(HelicalTrackHit hit1, HelicalTrackHit hit2, SeedCandidate seed) {
        if (super.getSkipChecks())
            return true;

        //  Initialize the hit coordinates for an unknown track direction
        CorrectHitPosition(hit1, seed);
        CorrectHitPosition(hit2, seed);

        //  Check that hits are outside the maximum DCA
        if (hit1.r() < super.getDMax() || hit2.r() < super.getDMax())
            return false;

        return TwoPointCircleCheckAlgorithm(hit1, hit2);
    }

    public boolean TwoPointCircleCheckAlgorithm(HelicalTrackHit hit1, HelicalTrackHit hit2) {
        if (super.getSkipChecks())
            return true;

        if (super.getDoSectorBinCheck()) {
            if (!zSectorCheck(hit1, hit2))
                return false;
        }

        // Try to find a circle passing through the 2 hits and the maximum DCA
        boolean success = false;
        try {
            success = _cfit2.FitCircle(hit1, hit2, super.getDMax());
        } catch (Exception x) {
        }

        // Check for success
        if (!success)
            return false;

        // Initialize the minimum/maximum arc lengths
        double s1min = 1.0e99;
        double s1max = -1.0e99;
        double s2min = 1.0e99;
        double s2max = -1.0e99;

        // Loop over the circle fits and find the min/max arc lengths
        for (TwoPointCircleFit fit : _cfit2.getCircleFits()) {
            double s1 = fit.s1();
            double s2 = fit.s2();
            if (s1 < s1min)
                s1min = s1;
            if (s1 > s1max)
                s1max = s1;
            if (s2 < s2min)
                s2min = s2;
            if (s2 > s2max)
                s2max = s2;
        }

        // If we are consistent with a straight-line fit, update the minimum s1,
        // s2
        TwoPointLineFit lfit = _cfit2.getLineFit();
        if (lfit != null) {

            // Find the distance from the DCA to the maximum DCA circle
            double x0 = lfit.x0();
            double y0 = lfit.y0();
            double s0 = 0.;
            double s0sq = super.getDMax() * super.getDMax() - (x0 * x0 + y0 * y0);
            if (s0sq > super.getEps() * super.getEps())
                s0 = Math.sqrt(s0sq);

            // Update the minimum arc length to the distance from the DCA to the
            // hit
            s1min = lfit.s1() - s0;
            s2min = lfit.s2() - s0;
        }

        // Calculate the allowed variation in hit r and z (not 1 sigma errors!)
        double dr1 = Math.max(super.getNSig() * hit1.dr(), super.getDMax());
        double dr2 = Math.max(super.getNSig() * hit2.dr(), super.getDMax());
        double dz1 = dz(hit1);
        double dz2 = dz(hit2);

        // Now check for consistent hits in the s-z plane
        // First expand z ranges by hit z uncertainty
        double z1min = hit1.z() - dz1;
        double z1max = hit1.z() + dz1;
        double z2min = hit2.z() - dz2;
        double z2max = hit2.z() + dz2;

        // Expand s ranges by hit r uncertainty (r ~ s for r << R_curvature)
        s1min = Math.max(0., s1min - dr1);
        s1max = s1max + dr1;
        s2min = Math.max(0., s2min - dr2);
        s2max = s2max + dr2;

        // Check the z0 limits using the min/max path lengths
        return checkz0(s1min, s1max, z1min, z1max, s2min, s2max, z2min, z2max);

    }

    @Override
    public boolean ThreePointHelixCheck(SeedCandidate seed) {
        if (super.getSkipChecks())
            return true;

        List<HelicalTrackHit> hits = seed.getHits();
        HelicalTrackHit hit1 = hits.get(0);
        HelicalTrackHit hit2 = hits.get(1);
        HelicalTrackHit hit3 = hits.get(2);

        //  Setup for a 3 point circle fit
        double p[][] = new double[3][2];
        double[] pos;
        double z[] = new double[3];
        double dztot = 0.;

        // construct p and z arrays based on hit position x order
        // function for dztot(hit)

        //  While not terribly elegant, code for speed
        //  Use calls that give uncorrected position and error
        //  Get the relevant variables for hit 1

        pos = hit1.getPosition();
        p[0][0] = pos[0];
        p[0][1] = pos[1];
        z[0] = pos[2];

        //  Get the relevant variables for hit 2
        pos = hit2.getPosition();
        p[1][0] = pos[0];
        p[1][1] = pos[1];
        z[1] = pos[2];

        //  Get the relevant variables for hit 3
        pos = hit3.getPosition();
        p[2][0] = pos[0];
        p[2][1] = pos[1];
        z[2] = pos[2];

        //  do the circle checks first
        if (!TwoPointCircleCheck(hit1, hit3, null))
            return false;
        if (!TwoPointCircleCheck(hit2, hit3, null))
            return false;

        //  Do the 3 point circle fit and check for success
        boolean success = _cfit3.fit(p[0], p[1], p[2]);
        if (!success)
            return false;

        //  Retrieve the circle parameters
        CircleFit circle = _cfit3.getFit();
        double xc = circle.x0();
        double yc = circle.y0();
        double rc = Math.sqrt(xc * xc + yc * yc);
        double rcurv = circle.radius();

        // min pT cut
        if (rcurv < super.getRMin())
            return false;

        //  Find the point of closest approach
        double x0 = xc * (1. - rcurv / rc);
        double y0 = yc * (1. - rcurv / rc);

        //  Find the x-y arc lengths to the hits and the smallest arc length
        double phi0 = Math.atan2(y0 - yc, x0 - xc);
        double[] dphi = new double[3];
        double dphimin = 999.;

        for (int i = 0; i < 3; i++) {
            //  Find the angle between the hits and the DCA under the assumption that |dphi| < pi
            dphi[i] = Math.atan2(p[i][1] - yc, p[i][0] - xc) - phi0;
            if (dphi[i] > Math.PI)
                dphi[i] -= twopi;
            if (dphi[i] < -Math.PI)
                dphi[i] += twopi;
            if (Math.abs(dphi[i]) < Math.abs(dphimin))
                dphimin = dphi[i];
        }

        //  Use the hit closest to the DCA to determine the circle "direction"
        boolean cw = dphimin < 0.;

        //  Find the arc lengths to the hits
        double[] s = new double[3];
        for (int i = 0; i < 3; i++) {

            //  Arc set to be positive if they have the same sign as dphimin
            if (cw)
                s[i] = -dphi[i] * rcurv;
            else
                s[i] = dphi[i] * rcurv;

            //  Treat the case where a point has dphi opposite in sign to dphimin as an incoming looper hit
            if (s[i] < 0.)
                s[i] += twopi * rcurv;
        }

        // find corrected hit positions
        CorrectHitPosition(hit1, seed);
        CorrectHitPosition(hit2, seed);
        CorrectHitPosition(hit3, seed);
        double zCorr[] = new double[3];
        zCorr[0] = hit1.getCorrectedPosition().z();
        zCorr[1] = hit2.getCorrectedPosition().z();
        zCorr[2] = hit3.getCorrectedPosition().z();
        ((HelicalTrackCross) hit1).resetTrackDirection();
        ((HelicalTrackCross) hit2).resetTrackDirection();
        ((HelicalTrackCross) hit3).resetTrackDirection();

        //  Order the arc lengths and z info by increasing arc length
        for (int i = 0; i < 2; i++) {
            for (int j = i + 1; j < 3; j++) {
                if (s[j] < s[i]) {
                    double temp = s[i];
                    s[i] = s[j];
                    s[j] = temp;

                    temp = z[i];
                    z[i] = z[j];
                    z[j] = temp;
                    temp = zCorr[i];
                    zCorr[i] = zCorr[j];
                    zCorr[j] = temp;
                }
            }
        }

        //  Predict the middle z
        double slope = (z[2] - z[0]) / (s[2] - s[0]);
        double z0 = z[0] - s[0] * slope;
        double zpred = z0 + s[1] * slope;

        // find hit errors
        double dz2 = zCorr[1] - z[1];
        double dz1 = zCorr[0] - z[0];
        double dz3 = zCorr[2] - z[2];
        double d12 = p[1][0] - p[0][0];
        double d13 = p[2][0] - p[0][0];
        double d23 = p[2][0] - p[1][0];
        dztot = dz2 * dz2 + (dz1 * d23 / d13) * (dz1 * d23 / d13) + (dz3 * d12 / d13) * (dz3 * d12 / d13);

        //  Add multiple scattering error here
        double pEstimate = estimateMomentum(slope, rcurv);
        double mserr = calculateMSerror(p[0][0], p[1][0], p[2][0], pEstimate);
        dztot += (mserr * mserr);
        dztot = _nsigErr * Math.sqrt(dztot);

        // comparison of middle z to prediction including error
        if (Math.abs(zpred - z[1]) > dztot)
            return false;

        //  Passed all checks - success!
        return true;
    }
}