package org.example.compress.gzip;

import org.example.compress.Compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipCompress implements Compress {

    private static final int BUFFER_SIZE = 1024 * 4;

    @Override
    public byte[] compress(byte[] bytes) {
        if (bytes == null){
            // TODO: 异常处理
            throw new RuntimeException("...");
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(bytes);
            gzip.flush();
            gzip.finish();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        if (bytes == null){
            // TODO: 异常处理
            throw new NullPointerException("...");
        }
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            GZIPInputStream gunzip = new GZIPInputStream(in);
            byte[] buffer = new byte[BUFFER_SIZE];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int n;
            while ((n = gunzip.read(buffer)) > -1){
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
