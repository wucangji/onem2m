package org.dsa.iot.onem2m.server;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeBuilder;
import org.dsa.iot.dslink.util.Objects;
import org.dsa.iot.onem2m.actions.cse.AddCSE;
import org.dsa.iot.onem2m.actions.server.DeleteServer;
import org.dsa.iot.onem2m.actions.server.EditServer;
import org.opendaylight.iotdm.client.Exchange;
import org.opendaylight.iotdm.primitive.RequestPrimitive;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Samuel Grenier
 */
public class OneM2MServer {

    private List<String> paths = new ArrayList<>();
    private final Node parent;

    private OneM2MServer(Node parent) {
        if (parent == null) {
            throw new NullPointerException("parent");
        }
        this.parent = parent;
        setupPolling();
    }

    private void setupPolling() {
        Objects.getDaemonThreadPool().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                // pollling logic for onem2m
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public void subscribe(String path) {
        paths.add(path);
    }

    public void unsubcribe(String path) {
        paths.remove(path);
    }

    public void addCSE(String cse) {
        NodeBuilder b = parent.createChild(cse);
        BaseCSE.init(b.build());
    }

    protected Exchange createExchange(RequestPrimitive primitive) {
        Exchange exchange = new Exchange();
        exchange.setHost(getHost());
        exchange.setPort(getPort());
        exchange.setRequestPrimitive(primitive);
        return exchange;
    }

    private String getHost() {
        return parent.getRoConfig("host").getString();
    }

    private String getPort() {
        return parent.getRoConfig("port").toString();
    }

    private void init() {
        {
            NodeBuilder b = parent.createChild("addCSE");
            b.setDisplayName("Add CSE");
            b.setSerializable(false);
            b.setAction(AddCSE.make());
            b.build();
        }

        {
            NodeBuilder b = parent.createChild("editServer");
            b.setDisplayName("Edit Server");
            b.setSerializable(false);

            int port = Integer.parseInt(getPort());
            b.setAction(EditServer.make(getHost(), port));
            b.build();
        }

        {
            NodeBuilder b = parent.createChild("deleteServer");
            b.setDisplayName("Delete Server");
            b.setSerializable(false);
            b.setAction(DeleteServer.make());
            b.build();
        }

        {
            Map<String, Node> children = parent.getChildren();
            for (Node node : children.values()) {
                if (node.getAction() == null) {
                    BaseCSE.init(node);
                }
            }
        }
    }

    public static void init(Node parent) {
        OneM2MServer server = new OneM2MServer(parent);
        parent.setMetaData(server);
        server.init();
    }
}
