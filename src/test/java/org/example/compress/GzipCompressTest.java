package org.example.compress;

import org.example.enums.CompressTypeEnum;
import org.example.spi.ExtensionLoader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class GzipCompressTest {

    @Test
    public void testGzipCompress() {
        // 测试 GZIP 压缩与解压逻辑
        byte[] data = "Test Data".getBytes();
        Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                .getExtension(CompressTypeEnum.GZIP.getName());
        byte[] compressedData = compress.compress(data);
        System.out.println(Arrays.toString(compressedData));
    }
}
