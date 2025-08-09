//for identifying sticker colours

package com.rubiksolver.vision;

import com.rubiksolver.model.Cube;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects Rubik's cube colors from camera images using OpenCV
 */
public class ColorDetector {

    private static final Logger logger = LoggerFactory.getLogger(ColorDetector.class);

    // HSV color ranges for different cube colors
    private static final Scalar WHITE_LOWER = new Scalar(0, 0, 200);
    private static final Scalar WHITE_UPPER = new Scalar(180, 30, 255);

    private static final Scalar YELLOW_LOWER = new Scalar(20, 100, 100);
    private static final Scalar YELLOW_UPPER = new Scalar(30, 255, 255);

    private static final Scalar RED_LOWER1 = new Scalar(0, 120, 70);
    private static final Scalar RED_UPPER1 = new Scalar(10, 255, 255);
    private static final Scalar RED_LOWER2 = new Scalar(170, 120, 70);
    private static final Scalar RED_UPPER2 = new Scalar(180, 255, 255);

    private static final Scalar ORANGE_LOWER = new Scalar(11, 120, 70);
    private static final Scalar ORANGE_UPPER = new Scalar(25, 255, 255);

    private static final Scalar GREEN_LOWER = new Scalar(50, 100, 100);
    private static final Scalar GREEN_UPPER = new Scalar(70, 255, 255);

    private static final Scalar BLUE_LOWER = new Scalar(100, 150, 0);
    private static final Scalar BLUE_UPPER = new Scalar(120, 255, 255);

    private Mat hsvImage;
    private Mat mask;

    public ColorDetector() {
        hsvImage = new Mat();
        mask = new Mat();
    }

    /**
     * Detects colors from a 3x3 grid of squares in the image
     * @param image Input image containing a Rubik's cube face
     * @return 3x3 array of detected colors
     */
    public Cube.Color[][] detectColorsFromFace(Mat image) {
        if (image == null || image.empty()) {
            logger.error("Input image is null or empty");
            return null;
        }

        // Convert to HSV for better color detection
        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);

        Cube.Color[][] colors = new Cube.Color[3][3];

        // Divide image into 3x3 grid
        int height = image.rows();
        int width = image.cols();
        int cellHeight = height / 3;
        int cellWidth = width / 3;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                // Extract region of interest (ROI)
                Rect roi = new Rect(
                        col * cellWidth,
                        row * cellHeight,
                        cellWidth,
                        cellHeight
                );

                Mat cellRegion = new Mat(hsvImage, roi);
                colors[row][col] = detectDominantColor(cellRegion);
            }
        }

        return colors;
    }

    /**
     * Detects the dominant color in a given region
     */
    private Cube.Color detectDominantColor(Mat region) {
        // Calculate color scores for each possible cube color
        double[] scores = new double[6];

        scores[0] = calculateColorScore(region, WHITE_LOWER, WHITE_UPPER);
        scores[1] = calculateColorScore(region, YELLOW_LOWER, YELLOW_UPPER);
        scores[2] = calculateColorScore(region, RED_LOWER1, RED_UPPER1) +
                calculateColorScore(region, RED_LOWER2, RED_UPPER2);
        scores[3] = calculateColorScore(region, ORANGE_LOWER, ORANGE_UPPER);
        scores[4] = calculateColorScore(region, GREEN_LOWER, GREEN_UPPER);
        scores[5] = calculateColorScore(region, BLUE_LOWER, BLUE_UPPER);

        // Find the color with the highest score
        int maxIndex = 0;
        double maxScore = scores[0];

        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > maxScore) {
                maxScore = scores[i];
                maxIndex = i;
            }
        }

        // Map index to color
        Cube.Color[] colors = {
                Cube.Color.WHITE, Cube.Color.YELLOW, Cube.Color.RED,
                Cube.Color.ORANGE, Cube.Color.GREEN, Cube.Color.BLUE
        };

        logger.debug("Detected color: {} with score: {}", colors[maxIndex], maxScore);
        return colors[maxIndex];
    }

    /**
     * Calculates a score for how much of the region matches the given color range
     */
    private double calculateColorScore(Mat region, Scalar lowerBound, Scalar upperBound) {
        Core.inRange(region, lowerBound, upperBound, mask);
        return Core.sumElems(mask).val[0] / (region.rows() * region.cols() * 255.0);
    }

    /**
     * Preprocesses the image to improve color detection
     */
    public Mat preprocessImage(Mat input) {
        Mat processed = new Mat();

        // Apply Gaussian blur to reduce noise
        Imgproc.GaussianBlur(input, processed, new Size(5, 5), 0);

        // Enhance contrast
        processed.convertTo(processed, -1, 1.2, 10);

        return processed;
    }

    /**
     * Finds and extracts cube face regions from the image
     */
    public List<Mat> findCubeFaces(Mat image) {
        List<Mat> faces = new ArrayList<>();
        Mat gray = new Mat();
        Mat edges = new Mat();

        // Convert to grayscale
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        // Edge detection
        Imgproc.Canny(gray, edges, 50, 150);

        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Filter contours that could be cube faces (squares)
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area > 1000) { // Minimum area threshold
                MatOfPoint2f contour2f = new MatOfPoint2f();
                contour.convertTo(contour2f, CvType.CV_32FC2);

                MatOfPoint2f approx = new MatOfPoint2f();
                double epsilon = 0.02 * Imgproc.arcLength(contour2f, true);
                Imgproc.approxPolyDP(contour2f, approx, epsilon, true);

                // Check if it's roughly square (4 corners)
                if (approx.total() == 4) {
                    Rect boundingRect = Imgproc.boundingRect(contour);
                    double aspectRatio = (double) boundingRect.width / boundingRect.height;

                    // Check if aspect ratio is close to 1 (square)
                    if (aspectRatio > 0.8 && aspectRatio < 1.2) {
                        Mat face = new Mat(image, boundingRect);
                        faces.add(face);
                    }
                }
            }
        }

        return faces;
    }

    /**
     * Calibrates color detection based on a solved cube
     */
    public void calibrateColors(Mat solvedCubeImage) {
        logger.info("Calibrating color detection...");
        // Implementation for color calibration
        // This would analyze a solved cube to adjust color ranges
    }

    /**
     * Validates detected colors for consistency
     */
    public boolean validateDetectedColors(Cube.Color[][] colors) {
        // Check if we have exactly 9 colors (one for each square)
        if (colors == null || colors.length != 3 || colors[0].length != 3) {
            return false;
        }

        // Check if center color is consistent (basic validation)
        Cube.Color centerColor = colors[1][1];
        if (centerColor == null) {
            return false;
        }

        // Could add more sophisticated validation rules here
        return true;
    }

    /**
     * Cleanup resources
     */
    public void release() {
        if (hsvImage != null) hsvImage.release();
        if (mask != null) mask.release();
    }
}