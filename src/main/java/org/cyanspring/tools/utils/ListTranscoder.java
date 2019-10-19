package org.cyanspring.tools.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ListTranscoder<M extends Serializable> {
    protected Log logger = LogFactory.getLog(getClass());

    @SuppressWarnings("unchecked")
    public List<M> deserialize(byte[] in) {
        List<M> list = new ArrayList<M>();
        ByteArrayInputStream bis = null;
        ObjectInputStream is = null;
        try {
            if (in != null) {
                bis = new ByteArrayInputStream(in);
                is = new ObjectInputStream(bis);
                while (true) {
                    M m = (M) is.readObject();
                    if (m == null) {
                        break;
                    }
                    list.add(m);
                }
            }
        } catch (IOException e) {
            logger.error(String.format("Caught IOException decoding %d bytes of data", in == null ? 0 : in.length) + e);
        } catch (ClassNotFoundException e) {
            logger.error(String.format("Caught CNFE decoding %d bytes of data", in == null ? 0 : in.length) + e);
        } finally {
            close(is);
            close(bis);
        }
        return list;
    }

    protected void close(InputStream bis) {
        if (bis != null) {
            try {
                bis.close();
            } catch (Exception err) {
                logger.error(err.getMessage());
            }
        }
    }

    protected void close(OutputStream bis) {
        if (bis != null) {
            try {
                bis.close();
            } catch (Exception err) {
                logger.error(err.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public byte[] serialize(List<M> values) {
        byte[] results = null;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream os = null;
        try {
            bos = new ByteArrayOutputStream();
            os = new ObjectOutputStream(bos);
            //os.writeObject(m);
            for (M m : values) {
                os.writeObject(m);
            }
            results = bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            close(os);
            close(bos);
        }
        return results;
    }
}