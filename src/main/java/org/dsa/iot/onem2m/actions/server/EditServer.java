package org.dsa.iot.onem2m.actions.server;

import org.dsa.iot.commons.ParameterizedAction;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;

import java.util.Map;

/**
 * @author Samuel Grenier
 */
public class EditServer extends ParameterizedAction {

    private EditServer() {
        super(Permission.WRITE);
    }

    @Override
    public void handle(ActionResult actRes, Map<String, Value> params) {
        Value host = params.get("Host");
        Value port = params.get("Port");

        Node node = actRes.getNode().getParent();
        node.setRoConfig("host", host);
        node.setRoConfig("port", port);
    }

    public static Action make(String host, int port) {
        ParameterizedAction act = new EditServer();
        {
            ParameterInfo p = new ParameterInfo("Host", ValueType.STRING);
            p.setOptional(false);
            p.setPersistent(true);
            p.setDefaultValue(new Value(host));
            act.addParameter(p);
        }
        {
            ParameterInfo p = new ParameterInfo("Port", ValueType.NUMBER);
            p.setOptional(false);
            p.setPersistent(true);
            p.setDefaultValue(new Value(port));
            act.addParameter(p);
        }
        return act;
    }
}
