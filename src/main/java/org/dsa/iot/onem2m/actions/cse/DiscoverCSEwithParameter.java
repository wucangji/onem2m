package org.dsa.iot.onem2m.actions.cse;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.onem2m.server.BaseCSE;
import org.dsa.iot.dslink.util.handler.Handler;

/**
 * Created by canwu on 9/10/15.
 */
public class DiscoverCSEwithParameter implements Handler<ActionResult> {
    @Override
    public void handle(ActionResult event) {
        Node node = event.getNode().getParent();
        BaseCSE cse = node.getMetaData();


        Value label = event.getParameter("Label");
        Value limit = event.getParameter("Limit");
        StringBuilder sb = new StringBuilder();
        if (label != null) {
            if(label.getType().compare(ValueType.STRING)) {
                sb.append("&lbl=");
                sb.append(label.toString());
            }
        }
        if (limit != null) {
            if (limit.getType().compare(ValueType.NUMBER)) {
                sb.append("&lim=");
                sb.append(limit.toString());
            }
        }

        //Can we print out the error information to the customer/dglux?
        cse.customeDiscover(sb.toString());
    }

    public static Action make() {
        Action act = new Action(Permission.WRITE, new DiscoverCSEwithParameter());

        {
            Parameter p = new Parameter("Limit", ValueType.NUMBER);
            p.setPlaceHolder("type a number here");
            act.addParameter(p);
        }
        {
            Parameter p = new Parameter("Label", ValueType.STRING);
            p.setPlaceHolder("type a string here");
            act.addParameter(p);
        }
        return act;
    }
}
