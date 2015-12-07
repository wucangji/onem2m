package org.dsa.iot.onem2m.actions.resource;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.actions.table.Row;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.onem2m.server.BaseCSE;
import org.dsa.iot.dslink.util.handler.Handler;

/**
 * Created by canwu on 12/7/15.
 */
public class DiscoverwithParameter implements Handler<ActionResult>{


    private final BaseCSE cse;

    private DiscoverwithParameter(BaseCSE cse) {
        this.cse = cse;
    }
    @Override
    public void handle(ActionResult event) {

        Node node = event.getNode().getParent();

        Node valNode = node.getChild("val");
        String ret = null;

        Value parameter = event.getParameter("Parameter");

        StringBuilder sb = new StringBuilder();

        if (parameter != null) {
            if (parameter.getType().compare(ValueType.STRING)) {
                sb.append("?");
                sb.append(parameter.toString());
            }
        }

        if (valNode != null) {
            String TargetResourceURI = valNode.getValue().toString();
            ret = cse.getResponseJsonString(TargetResourceURI + sb.toString());
        } else {
            ret = "Failed to get the URI";
        }
        event.getTable().addRow(Row.make(new Value(ret)));



    }

    public static Action make(BaseCSE cse) {
        Action act = new Action(Permission.WRITE, new DiscoverwithParameter(cse));
        {
            Parameter p = new Parameter("Parameter", ValueType.STRING);
            p.setPlaceHolder("use & to connect parameter");
            act.addParameter(p);
        }
        {
            Parameter p = new Parameter("Response", ValueType.STRING);
            act.addResult(p);
        }
        return act;
    }
}
