package org.dsa.iot.onem2m.actions.cse;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.onem2m.server.BaseCSE;
import org.vertx.java.core.Handler;

/**
 * Created by canwu on 9/10/15.
 */
public class DiscoverCSEwithParameter implements Handler<ActionResult> {
    @Override
    public void handle(ActionResult event) {
        Node node = event.getNode().getParent();
        BaseCSE cse = node.getMetaData();
        final String parameter = event.getParameter("Parameter", ValueType.STRING).getString();
        cse.customeDiscover(parameter);
    }

    public static Action make() {
        Action act = new Action(Permission.WRITE, new DiscoverCSEwithParameter());

        {
            Parameter p = new Parameter("Parameter", ValueType.STRING);
            p.setPlaceHolder("type a full command.eg.fu=1&rcn=5");
            act.addParameter(p);
        }

        return act;
    }
}
