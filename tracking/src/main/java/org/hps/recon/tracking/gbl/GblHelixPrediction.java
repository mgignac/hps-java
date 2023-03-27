package org.hps.recon.tracking.gbl;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import org.hps.recon.tracking.gbl.matrix.Matrix;
import org.hps.recon.tracking.gbl.matrix.Vector;

public class GblHelixPrediction {
    
    public interface GblHelixPredictionInterface extends Library {
        
        GblHelixPredictionInterface INSTANCE = (GblHelixPredictionInterface) Native.loadLibrary("GBL", GblHelixPredictionInterface.class);
        
        Pointer GblHelixPredictionCtor(double sArc, double[] aPred,
                                       double [] tDir, double [] uDir, double [] vDir,
                                       double [] nDir, double [] aPos);
        
        void GblHelixPrediction_delete(Pointer self);
        double GblHelixPrediction_getArcLength(Pointer self);
        void GblHelixPrediction_getMeasPred(Pointer self, double [] prediction);
        void GblHelixPrediction_getPosition(Pointer self, double[] position);
        void GblHelixPrediction_getDirection(Pointer self,double[] direction);
        
        double GblHelixPrediction_getCosIncidence(Pointer self);
        
        //array for the curvilinear directions (2x3 matrix)
        void GblHelixPrediction_getCurvilinearDirs(Pointer self, double [] curvilinear);
        
    }
    
    private Pointer self;
    
    //aPred is a 2-vector
    //tDir is a 3-vector
    //uDir is a 3-vector
    //vDir is a 3-vector
    //nDir is a 3-vector
    //aPos is a 3-vector 
    public GblHelixPrediction(double sArc, double[] aPred,
                              double [] tDir, double [] uDir, double [] vDir, 
                              double [] nDir, double [] aPos) {
        
        self = GblHelixPredictionInterface.INSTANCE.GblHelixPredictionCtor(sArc, aPred,
                                                                           tDir, uDir, vDir,
                                                                           nDir,aPos);
    }
    
    public GblHelixPrediction(Pointer ptr) {
        self = ptr;
    }

    public void delete() {
        GblHelixPredictionInterface.INSTANCE.GblHelixPrediction_delete(self);
    }

    public GblHelixPrediction() {
        
        double sArc = 0;
        double [] aPred = new double[2];
        double [] tDir = new double[2];
        double [] uDir = new double[2];
        double [] vDir = new double[2];
        double [] nDir = new double[2];
        double [] aPos = new double[2];
        
        self = GblHelixPredictionInterface.INSTANCE.GblHelixPredictionCtor(sArc, aPred,
                                                                           tDir, uDir, vDir,
                                                                           nDir,aPos);
        
    }

    public GblHelixPrediction(double sArc, Vector v_aPred, Vector v_tDir, Vector v_uDir, Vector v_vDir, Vector v_nDir, Vector v_aPos) {
        
        double[] aPred = v_aPred.getColumnPackedCopy();
        double[] tDir  =  v_tDir.getColumnPackedCopy();
        double[] uDir  =  v_uDir.getColumnPackedCopy();
        double[] vDir  =  v_vDir.getColumnPackedCopy();
        double[] nDir  =  v_nDir.getColumnPackedCopy();
        double[] aPos  =  v_aPos.getColumnPackedCopy();
        
        
        self = GblHelixPredictionInterface.INSTANCE.GblHelixPredictionCtor(sArc, aPred,
                                                                           tDir, uDir, vDir,
                                                                           nDir, aPos);
    }

    public double getArcLength() {
        return GblHelixPredictionInterface.INSTANCE.GblHelixPrediction_getArcLength(self);
    }
    
    public void getMeasPred(double [] prediction) {
        GblHelixPredictionInterface.INSTANCE.GblHelixPrediction_getMeasPred(self,prediction);
    }
    
    public void getPosition(double [] position) {
        GblHelixPredictionInterface.INSTANCE.GblHelixPrediction_getPosition(self, position);
    }
    
    public void getDirection(double [] direction) {
        GblHelixPredictionInterface.INSTANCE.GblHelixPrediction_getDirection(self, direction);
    }
    
    public double getCosIncidence() {
        return GblHelixPredictionInterface.INSTANCE.GblHelixPrediction_getCosIncidence(self);
    }
    
    public Matrix getCurvilinearDirs() {
        
        Matrix curvilinearDirs = new Matrix(2,3);
        double[] dirArray = new double[6];
        
        GblHelixPredictionInterface.INSTANCE.GblHelixPrediction_getCurvilinearDirs(self, dirArray);
        
        curvilinearDirs.set(0,0,dirArray[0]);
        curvilinearDirs.set(0,1,dirArray[1]);
        curvilinearDirs.set(0,2,dirArray[2]);
        curvilinearDirs.set(1,0,dirArray[3]);
        curvilinearDirs.set(1,1,dirArray[4]);
        curvilinearDirs.set(1,2,dirArray[5]);

        return curvilinearDirs;
    }
    
    public Pointer getPtr() {
        return self;
    }
    
    public void setPtr(Pointer pointer) {
        self = pointer;
    }
    
}

