package org.example.remoting.transport.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.example.compress.Compress;
import org.example.enums.CompressTypeEnum;
import org.example.enums.SerializationTypeEnum;
import org.example.remoting.constants.RpcConstants;
import org.example.remoting.dto.RpcMessage;
import org.example.remoting.dto.RpcRequest;
import org.example.remoting.dto.RpcResponse;
import org.example.serialize.Serializer;
import org.example.spi.ExtensionLoader;

import java.util.Arrays;

public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {
    public RpcMessageDecoder() {
        // lengthFieldOffset: magic code is 4B, and version is 1B, and then full length. so value is 5
        // lengthFieldLength: full length is 4B. so value is 4
        // lengthAdjustment: full length include all data and read 9 bytes before, so the left length is (fullLength-9). so values is -9
        // initialBytesToStrip: we will check magic code and version manually, so do not strip any bytes. so values is 0
        this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        // 如果父类解码后仍是原始字节数据，说明需要进一步处理
        // 父类可能已直接解码为其他自定义的对象
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH){
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }finally {
                    frame.release();
                }
            }
        }
        return decoded;
    }

    private Object decodeFrame(ByteBuf in){

        checkMagicNumber(in);
        checkVersion(in);
        int fullLength = in.readInt();
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();
        RpcMessage rpcMessage = RpcMessage.builder()
                .messageType(messageType)
                .codecType(codecType)
                .requestId(requestId)
                .build();
        if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
//            System.out.println("RpcMessageDecoder decodeFrame heartbeat request");
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
//            System.out.println("RpcMessageDecoder decodeFrame heartbeat response");
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }
        System.out.println("RpcMessageDecoder decodeFrame messageType:" + messageType);
//        System.out.println("CompressType:" + compressType);
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if (bodyLength > 0) {
            byte[] bs = new byte[bodyLength];
            in.readBytes(bs);
            // decompress
            String compressName = CompressTypeEnum.getName(compressType);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                    .getExtension(compressName);
            bs = compress.decompress(bs);

            // un-codec
            String codecName = SerializationTypeEnum.getName(codecType);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                    .getExtension(codecName);
            if (messageType == RpcConstants.REQUEST_TYPE){
                RpcRequest tmpValue = serializer.deserialize(bs, RpcRequest.class);
                System.out.println("RpcMessageDecoder decodeFrame tmpValue:" + tmpValue);
                rpcMessage.setData(tmpValue);
            }else {
                RpcResponse tmpValue = serializer.deserialize(bs, RpcResponse.class);
                rpcMessage.setData(tmpValue);
            }
        }
        return rpcMessage;
    }

    private void checkVersion(ByteBuf in) {
        // read the version and compare
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
    }

    private void checkMagicNumber(ByteBuf in) {
        // read the first 4 bit, which is the magic number, and compare
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }
    }
}
