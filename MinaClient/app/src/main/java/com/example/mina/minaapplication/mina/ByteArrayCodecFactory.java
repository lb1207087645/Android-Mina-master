package com.example.mina.minaapplication.mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import java.nio.charset.Charset;

/**
 * 自定义解编码器工厂
 * Created by linbin on 2018/2/10.
 */

public class ByteArrayCodecFactory implements ProtocolCodecFactory {

    private ByteArrayDecoder decoder;
    private ByteArrayEncoder encoder;
    public ByteArrayCodecFactory() {
        this(Charset.defaultCharset());
    }
    public ByteArrayCodecFactory(Charset charSet) {
        encoder = new ByteArrayEncoder(charSet);
        decoder = new ByteArrayDecoder(charSet);
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return decoder;
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return encoder;
    }


}
