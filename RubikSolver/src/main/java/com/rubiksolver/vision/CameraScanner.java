//for openCV image capture

package com.rubiksolver.vision;

import com.rubiksolver.model.Cube;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Handles camera operations and cube face scanning
 */
public class CameraScanner {

    private static final Logger logger = LoggerFactory.getLogger(CameraScanner.class);

    private VideoCapture camera;
    private ColorDetector colorDetector;
    private ExecutorService executor;
    private boolean isScanning = false;
    private Consumer<Mat> frameCallback;

    public CameraScanner() {
        this.colorDetector = new ColorDetector();
        this.executor = Executors.newSingleThreadExecutor();
        initializeCamera();
    }

    /**
     * Initialize camera connection
     */
    private void initializeCamera() {
        try {
            camera = new VideoCapture(0); // Default camera

            if (!camera.isOpened()) {
                logger.error("Failed to open camera");
                return;
            }

            // Set camera properties for better quality
            camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
            camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
            camera.set(Videoio.CAP_PROP_FPS, 30);

            logger.info("Camera initialized successfully");

        } catch (Exception e) {
            logger.error("Error initializing camera", e);
        }
    }

    /**
     * Start continuous frame capture
     */
    public void startPreview(Consumer<Mat> frameCallback) {
        this.frameCallback = frameCallback;

        if (camera == null || !camera.isOpened()) {
            logger.error("Camera not available");
            return;
        }

        isScanning = true;
        executor.submit(this::captureFrames);
    }

    /**
     * Stop frame capture
     */
    public void stopPreview() {
        isScanning = false;
    }

    /**
     * Capture frames continuously
     */
    private void captureFrames() {
        Mat frame = new Mat();

        while (isScanning && camera.isOpened()) {
            try {
                if (camera.read(frame) && !frame.empty()) {
                    // Preprocess frame
                    Mat processedFrame = colorDetector.preprocessImage(frame);

                    // Send frame to callback if available
                    if (frameCallback != null) {
                        frameCallback.accept(processedFrame.clone());
                    }

                    // Small delay to prevent excessive CPU usage
                    Thread.sleep(33); // ~30 FPS
                }
            } catch (Exception e) {
                logger.error("Error capturing frame", e);
                break;
            }
        }

        frame.release();
    }

    /**
     * Capture a single frame for face detection
     */
    public CompletableFuture<Mat> captureFrame() {
        return CompletableFuture.supplyAsync(() -> {
            if (camera == null || !camera.isOpened()) {
                logger.error("Camera not available for frame capture");
                return null;
            }

            Mat frame = new Mat();
            if (camera.read(frame) && !frame.empty()) {
                return colorDetector.preprocessImage(frame);
            }

            frame.release();
            return null;
        }, executor);
    }

/**
 * Scan a cube face and detect colors
 */