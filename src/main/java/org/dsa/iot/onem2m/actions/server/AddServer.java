package org.dsa.iot.onem2m.actions.server;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeBuilder;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.onem2m.server.OneM2MServer;
import org.vertx.java.core.Handler;

/**
 * @author Samuel Grenier
 */
public class AddServer implements Handler<ActionResult> {

    private static final Object LOCK = new Object();

    private AddServer() {
    }

    @Override
    public void handle(ActionResult actRes) {
        String name;
        {
            Value val = actRes.getParameter("Name", ValueType.STRING);
            name = val.getString();
        }
        Value host = actRes.getParameter("Host", ValueType.STRING);
        Value port = actRes.getParameter("Port", ValueType.NUMBER);

        Node parent = actRes.getNode().getParent();
        synchronized (LOCK) {
            if (parent.hasChild(name)) {
                throw new RuntimeException(name + " already exists");
            }
            NodeBuilder builder = parent.createChild(name);
            builder.setRoConfig("host", host);
            builder.setRoConfig("port", port);
            OneM2MServer.init(builder.build());
        }
    }

    public static Action make() {
        Action act = new Action(Permission.WRITE, new AddServer());
        {
            Parameter p = new Parameter("Name", ValueType.STRING);
            p.setPlaceHolder("My OneM2M Server");
            act.addParameter(p);
        }
        {
            Parameter p = new Parameter("Host", ValueType.STRING);
            p.setPlaceHolder("127.0.0.1");
            act.addParameter(p);
        }
        {
            Parameter p = new Parameter("Port", ValueType.NUMBER);
            p.setDefaultValue(new Value(8888));
            act.addParameter(p);
        }
        return act;
    }
}
