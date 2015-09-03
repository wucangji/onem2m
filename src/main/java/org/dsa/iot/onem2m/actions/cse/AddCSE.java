package org.dsa.iot.onem2m.actions.cse;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.onem2m.server.OneM2MServer;
import org.vertx.java.core.Handler;

/**
 * @author Samuel Grenier
 */
public class AddCSE implements Handler<ActionResult> {

    private AddCSE() {
    }

    @Override
    public void handle(ActionResult event) {
        Node node = event.getNode().getParent();
        OneM2MServer server = node.getMetaData();

        String cse = event.getParameter("Name", ValueType.STRING).getString();
        server.addCSE(cse);
    }

    public static Action make() {
        Action act = new Action(Permission.WRITE, new AddCSE());
        {
            Parameter p = new Parameter("Name", ValueType.STRING);
            p.setPlaceHolder("ODL-oneM2M-Cse");
            act.addParameter(p);
        }
        return act;
    }
}
