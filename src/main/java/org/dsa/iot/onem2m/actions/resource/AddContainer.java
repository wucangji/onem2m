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
import org.vertx.java.core.Handler;

/**
 * Created by canwu on 9/14/15.
 */
public class AddContainer implements Handler<ActionResult> {

    private final BaseCSE cse;

    private AddContainer(BaseCSE cse) {
        this.cse = cse;
    }
    @Override
    public void handle(ActionResult event) {
        Node node = event.getNode().getParent();


        Node valNode = node.getChild("val");
        String ret = null;
        if (valNode != null) {
            String TargetContainerURI = valNode.getValue().toString();
            String Name = event.getParameter("Name", ValueType.STRING).getString();
            ret = cse.createContainerwithName(TargetContainerURI, Name);
        } else if (node.getChild("ty").getValue().toString().compareTo("5") == 0) {
            String TargetContainerURI = node.getChild("rn").getValue().toString();
            String Name = event.getParameter("Name", ValueType.STRING).getString();
            ret = cse.createContainerwithName(TargetContainerURI, Name);
        }

        event.getTable().addRow(Row.make(new Value(ret)));
    }

    public static Action make(BaseCSE cse) {
        Action act = new Action(Permission.WRITE, new AddContainer(cse));
        {
            Parameter p = new Parameter("Name", ValueType.STRING);
            p.setPlaceHolder("type a name here");
            act.addParameter(p);
        }
        {
            Parameter p = new Parameter("Response", ValueType.STRING);
            act.addResult(p);
        }
        return act;
    }
}
