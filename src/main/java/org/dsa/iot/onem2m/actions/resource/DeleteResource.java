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
 * Created by canwu on 9/16/15.
 */
public class DeleteResource implements Handler<ActionResult> {

    private final BaseCSE cse;

    private DeleteResource(BaseCSE cse) {
        this.cse = cse;
    }
    @Override
    public void handle(ActionResult event) {
        Node node = event.getNode().getParent();

        Node valNode = node.getChild("val");
        String ret = null;
        if (valNode != null) {
            String TargetContainerURI = valNode.getValue().toString();
            ret = cse.deleteResource(TargetContainerURI);
        }

        event.getTable().addRow(Row.make(new Value(ret)));
    }

    public static Action make(BaseCSE cse) {
        Action act = new Action(Permission.WRITE, new DeleteResource(cse));
        {
            Parameter p = new Parameter("Confirm", ValueType.STRING);
            p.setDefaultValue(new Value("Confirm you want to delete it!"));  // how to add a confirm button?
            act.addParameter(p);
        }
        {
            Parameter p = new Parameter("Response", ValueType.STRING);
            act.addResult(p);
        }
        return act;
    }
}
