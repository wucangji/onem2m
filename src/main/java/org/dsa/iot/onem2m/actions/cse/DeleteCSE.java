package org.dsa.iot.onem2m.actions.cse;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.util.handler.Handler;

/**
 * @author Samuel Grenier
 */
public class DeleteCSE implements Handler<ActionResult> {

    private DeleteCSE() {
    }

    @Override
    public void handle(ActionResult event) {
        Node node = event.getNode().getParent();
        node.getParent().removeChild(node);
    }

    public static Action make() {
        return new Action(Permission.WRITE, new DeleteCSE());
    }
}
