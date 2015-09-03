package org.dsa.iot.onem2m.actions.server;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.vertx.java.core.Handler;

/**
 * @author Samuel Grenier
 */
public class DeleteServer implements Handler<ActionResult> {

    @Override
    public void handle(ActionResult event) {
        Node node = event.getNode().getParent();
        node.getParent().removeChild(node);
    }

    public static Action make() {
        return new Action(Permission.WRITE, new DeleteServer());
    }
}
