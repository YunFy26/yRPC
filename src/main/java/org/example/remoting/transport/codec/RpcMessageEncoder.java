package org.example.remoting.transport.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.example.compress.Compress;
import org.example.enums.SerializationTypeEnum;
import org.example.remoting.constants.RpcConstants;
import org.example.remoting.dto.RpcMessage;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import org.example.enums.CompressTypeEnum;
import org.example.serialize.Serializer;
import org.example.spi.ExtensionLoader;

/**
 * <p>
 * custom protocol encoder
 * <p>
 * <pre>
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B codec（序列化类型）    1B compress（压缩类型）    4B  requestId（请求Id）
 * body（object类型数据）
 * </pre>
 *
 */
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf out) throws Exception{

        try {
            out.writeBytes(RpcConstants.MAGIC_NUMBER);
            out.writeByte(RpcConstants.VERSION);
            // 消息长度
            out.writerIndex(out.writerIndex() + 4);
            // 消息类型
            byte messageType = rpcMessage.getMessageType();
            out.writeByte(messageType);
            // 序列化类型
            out.writeByte(rpcMessage.getCodecType());
            // 压缩类型
            out.writeByte(CompressTypeEnum.GZIP.getCode());
            // requestId
            out.writeInt(ATOMIC_INTEGER.getAndIncrement());

            byte[] bodyBytes = null;
            int fullLength = RpcConstants.HEAD_LENGTH;
            if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE){
                // codec
//                System.out.println("序列化前：" + rpcMessage.getData());
                String codecType = SerializationTypeEnum.getName(rpcMessage.getCodecType());
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                        .getExtension(codecType);
                bodyBytes = serializer.serialize(rpcMessage.getData());
//                System.out.println("序列化后：" + Arrays.toString(bodyBytes));
                // compress
                String compressType = CompressTypeEnum.getName(rpcMessage.getCompressType());
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                        .getExtension(compressType);
                bodyBytes = compress.compress(bodyBytes);
//                System.out.println("压缩后：" + Arrays.toString(bodyBytes));
                // 计算总长度
                fullLength += bodyBytes.length;
            }
            if (bodyBytes != null) {
                out.writeBytes(bodyBytes);
            }
            // 当前写指针索引
            int writeIndex = out.writerIndex();
            // 写指针回退
            out.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + RpcConstants.VERSION);
            // 写入总长度
            out.writeInt(fullLength);
            // 恢复写指针索引
            out.writerIndex(writeIndex);
        } catch (Exception e) {
//            log.error("Encode request error");
            System.out.println("Encode request error");
        }
    }
}
