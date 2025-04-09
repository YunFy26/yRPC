package org.example.serialize;

import org.example.spi.SPI;

@SPI
public interface Serializer {

    byte[] serialize(Object obj);

    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
