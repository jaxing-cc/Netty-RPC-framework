package jaxing.rpc.common.serializer;
import jaxing.rpc.common.utils.KryoUtil;

public class KryoSerializer implements Serializer{

    @Override
    public <T> byte[] serialize(T obj) {
        return KryoUtil.writeObjectToByteArray(obj);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return KryoUtil.readObjectFromByteArray(bytes,clazz);
    }
}
