/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.team399.y2013.robot.Systems.Imaging;

import com.sun.squawk.util.MathUtils;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.image.BinaryImage;
import edu.wpi.first.wpilibj.image.ColorImage;
import edu.wpi.first.wpilibj.image.NIVisionException;
import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
import java.util.Vector;

/**
 * ImageProcessor class. Provides methods to process images
 * @author Jeremy G., Justin S.
 */
public class ImageProcessor {

    public static final double areaThresh = 1000;
    public static final double rectThresh = 50;
    public static final double aspectThresh = 0;
    public static final int cameraHeight = 240;
    public static final int cameraWidth = 320;
    public static final double cameraOnTurretHeight = 4.958;

    public static double rectangularityScore(double area, double width, double height) {
        // A perfect rectangle will have a score of 100
        double rectangularityScore = area;
        rectangularityScore /= width;
        rectangularityScore /= height;
        rectangularityScore *= 100;
        return rectangularityScore;
    }

    public static double aspectRatioScore(double width, double height) {
        double aspectRatioScore = width / height;
        aspectRatioScore /= 1.33; // normalize
        aspectRatioScore = Math.abs(1.0 - aspectRatioScore); //Teepee at 1
        aspectRatioScore = 100.0 * (1.0 - aspectRatioScore);
        if (aspectRatioScore > 100) {
            aspectRatioScore = 100;
        } else if (aspectRatioScore < 0) {
            aspectRatioScore = 0;
        }
        return aspectRatioScore;
    }

    public static Target[] processImage(ColorImage image, HSLThreshold thresh) {
        //System.out.println("[EAGLE-EYE] Image processor started...");
        if (image == null) {
            System.out.println("[IMG-PROC] Image null, processor ended");
            return null;   //If the image is null, return a null target
        }
        BinaryImage masked;
        BinaryImage hulled;
        Vector rects = new Vector();
        rects.ensureCapacity(4);
        ParticleAnalysisReport[] all;

        try {
            masked = image.thresholdHSL(thresh.HueLow, thresh.HueHigh,
                    thresh.SatLow, thresh.SatHigh,
                    thresh.LumLow, thresh.LumHigh);
            hulled = masked.convexHull(true);
            all = hulled.getOrderedParticleAnalysisReports(6);  //Get sorted particle report. sorted in order of size
            image.free();
            hulled.free();
            masked.free();
        } catch (NIVisionException e) {
            System.out.println("[IMG-PROC] NI Vision exception, processor ended");
            return null;
        }


        for (int i = 0; i < all.length; i++) {               //Store the targets that pass the size limit
            if (all[i].particleArea > areaThresh) {
                double rectangularityScore =
                        rectangularityScore(all[i].particleArea,
                        all[i].boundingRectWidth,
                        all[i].boundingRectHeight);
                double aspectRatioScore =
                        aspectRatioScore(all[i].boundingRectWidth,
                        all[i].boundingRectHeight);

                if (rectangularityScore > rectThresh
                        && aspectRatioScore > aspectThresh) {
                    Target current = new Target(all[i]);
                    rects.addElement(current);          //Target found, return it 
                }
            }

        }
        rects.trimToSize();
        Target[] targets = new Target[rects.size()];
        rects.copyInto(targets);
        if (targets.length == 0) {
            //System.out.println("[IMG-PROC] No targets found, processor ended");
            return null;
        }
        return targets;
    }

    /**
     * Target class. provides properties to store data on a target
     */
    public static class Target {

        /**
         * The target's X axis value in the image
         */
        public int x = 0;
        /**
         * The target's Y axis value in the image
         */
        public int y = 0;
        /**
         * The target's width
         */
        public int width = 0;
        /**
         * The target's height
         */
        public int height = 0;
        /**
         * The target's calculated straight line distance from the camera lens
         */
        public double distance = 0;
        /**
         * The target's area in the image
         */
        public double area = 0;

        /**
         * Creates a target based on a particle analysis report
         * @param par 
         */
        public Target(ParticleAnalysisReport par) {
            try {
                x = par.center_mass_x;
                y = par.center_mass_y;
                width = par.boundingRectWidth;
                height = par.boundingRectHeight;
                area = par.particleArea;
                distance = calculateDistance();
            } catch (Exception e) {
                x = 0;
                y = 0;
                width = 0;
                height = 0;
                area = 0;
                distance = 0;
            }
        }
        
        public Target(int x, int y, int distance) {
            this.x = x;
            this.y = y;
            this.distance = distance;
        }

        /**
         * Creates a string representation of the target
         * @return a string with various bits of target info
         */
        public String toString() {
            int feet = (int) distance;
            int inches = (int) ((distance - feet) * 12);
            return " X: " + x + " Y: " + y + " H: " + height + " W: "
                    + width + " Distance: " + feet + " " + inches + " Time: " + System.currentTimeMillis();//" Area: " + area;
        }

        /**
         * returns the target's offset above the center
         * @return 
         */
        public int getPixelsAboveCenter() {
            return x - (120);
        }
        private final double cameraHalfTanAngle = Math.tan(42.0 * Math.PI / 360.0);

        /**
         * Attempts to calculate distance from the target.
         * Do not use this. could be inaccurate and buggy
         * @return 
         */
        public double calculateDistance() {
            double answer = cameraWidth / (width * cameraHalfTanAngle);
            return answer;
        }
    }
}