package org.dsa.iot.onem2m;

import org.dsa.iot.dslink.DSLink;
import org.dsa.iot.dslink.DSLinkFactory;
import org.dsa.iot.dslink.DSLinkHandler;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeBuilder;
import org.dsa.iot.dslink.util.Objects;
import org.dsa.iot.onem2m.actions.server.AddServer;
import org.dsa.iot.onem2m.server.OneM2MServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Main extends DSLinkHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    @Override
    public boolean isResponder() {
        return true;
    }

    @Override
    public void onResponderInitialized(DSLink link) {
        LOGGER.info("Initialized");

        Node root = link.getNodeManager().getSuperRoot();

        {
            NodeBuilder b = root.createChild("addServer");
            b.setDisplayName("Add Server");
            b.setSerializable(false);
            b.setAction(AddServer.make());
            b.build();
        }

        Map<String, Node> children = root.getChildren();
        if (children != null) {
            for (final Node node : children.values()) {
                if (node.getAction() == null) {
                    Objects.getDaemonThreadPool().execute(new Runnable() {
                        @Override
                        public void run() {
                            OneM2MServer.init(node);
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onResponderConnected(DSLink link) {
        LOGGER.info("Connected");
    }

    public static void main(String[] args) {
        DSLinkFactory.start(args, new Main());
    }
}
