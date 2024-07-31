package org.rabbani.util;

import java.io.IOException;
import java.io.InputStream;

public class IOUtils {

    public static byte[] readFully(InputStream in,int length) throws IOException {
        byte[] buffer = new byte[length];
        int red;
        int offset = 0;
        while(offset < length && (red = in.read(buffer,offset,length)) != -1){
            offset += red;
            length -= red;
        }
        return buffer;
    }
}
