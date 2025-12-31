package com.photon.storage.utils;

import java.io.File;
import java.io.IOException;

public class VideoCompressor {

    public static File compressVideo(File inputFile, File outputFile) throws IOException, InterruptedException {
        // Use FFmpeg command (requires FFmpeg installed and accessible via system path)
        String[] command = {
                "ffmpeg", "-i", inputFile.getAbsolutePath(),
                "-vcodec", "libx264", "-crf", "28", // Compression settings
                outputFile.getAbsolutePath()
        };

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();
        process.waitFor();

        if (outputFile.exists()) {
            return outputFile;
        } else {
            throw new IOException("Video compression failed.");
        }
    }
}