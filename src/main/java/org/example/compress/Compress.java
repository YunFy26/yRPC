package org.example.compress;

import org.example.spi.SPI;

@SPI
public interface Compress {

    byte[] compress(byte[] bytes);

    byte[] decompress(byte[] bytes);
}
