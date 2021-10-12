package jaxing.rpc.common.obj;


import jaxing.rpc.common.config.Constant;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public class RpcService implements Serializable {
    //接口名
    private String interfaceName;
    private String version;
    //目标类名
    private String targetName;

    public String makeName(){
        return new StringBuilder(interfaceName).append(Constant.FLAG).append(version).toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != this){return false;}
        if (obj instanceof RpcService){
            return Objects.equals(((RpcService) obj).getInterfaceName(), this.interfaceName) &&
                    Objects.equals(((RpcService) obj).getVersion(), this.version);
        }else{
            return false;
        }
    }

    @Override
    public String toString() {
        return interfaceName + "@" + version;
    }
}
