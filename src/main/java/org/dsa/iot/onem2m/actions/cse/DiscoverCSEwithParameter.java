package org.dsa.iot.onem2m.actions.cse;

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
 * Created by canwu on 9/10/15.
 */
public class DiscoverCSEwithParameter implements Handler<ActionResult> {
    @Override
    public void handle(ActionResult event) {
        Node node = event.getNode().getParent();
        BaseCSE cse = node.getMetaData();


        Value parameter = event.getParameter("Parameter");

        StringBuilder sb = new StringBuilder();

        if (parameter != null) {
            if (parameter.getType().compare(ValueType.STRING)) {
                sb.append("?");
                sb.append(parameter.toString());
            }
        }

        //Can we print out the error information to the customer/dglux?
        cse.discoverRoot();
        String cseName = node.getName();
        String lastCon = cse.getResponseJsonString(cseName + sb.toString());
        event.getTable().addRow(Row.make(new Value(lastCon)));
    }

    public static Action make() {
        Action act = new Action(Permission.WRITE, new DiscoverCSEwithParameter());

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
