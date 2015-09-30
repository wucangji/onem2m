package org.dsa.iot.onem2m.actions.cse;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.onem2m.server.BaseCSE;
import org.dsa.iot.dslink.util.handler.Handler;

/**
 * @author Samuel Grenier
 */
public class DiscoverCSE implements Handler<ActionResult> {
    @Override
    public void handle(ActionResult event) {
        Node node = event.getNode().getParent();
        BaseCSE cse = node.getMetaData();
        cse.discoverRoot();
    }

    public static Action make() {
        return new Action(Permission.READ, new DiscoverCSE());
    }
}
