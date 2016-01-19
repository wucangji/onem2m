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

import java.util.Map;

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
        String TargetContainerURI = valNode.getValue().toString();
        String ret = null;
        ret = cse.deleteResource(TargetContainerURI);
        Node parent = node.getParent();
        String parentPath = TargetContainerURI.substring(0, TargetContainerURI.lastIndexOf("/"));
        Map<String, Node> children = parent.getChildren();
        if (children != null) {
            for (Node node1 : children.values()) {
                if (node1.getAction() == null && !(node1.getName().equalsIgnoreCase("val") || node1.getName().equalsIgnoreCase("typ") || node1.getName().equalsIgnoreCase("nm"))) {
                    parent.removeChild(node1);
                }
            }
        }
        cse.discoverThisUri(parentPath);
        event.getTable().addRow(Row.make(new Value(ret)));
    }

    public static Action make(BaseCSE cse) {
        Action act = new Action(Permission.WRITE, new DeleteResource(cse));
        {
            Parameter p = new Parameter("Confirm", ValueType.STRING);
            p.setDefaultValue(new Value("Confirm you want to delete it!"));
            p.setDescription("Click Invoke to confirm deletion");
            act.addParameter(p);
        }
        {
            Parameter p = new Parameter("Response", ValueType.STRING);
            act.addResult(p);
        }
        return act;
    }
}
