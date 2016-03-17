package org.dsa.iot.onem2m.server;

import org.apache.commons.io.IOUtils;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeBuilder;
import org.dsa.iot.dslink.util.json.JsonObject;
import org.dsa.iot.onem2m.actions.cse.AddCSE;
import org.dsa.iot.onem2m.actions.server.DeleteServer;
import org.dsa.iot.onem2m.actions.server.EditServer;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.opendaylight.iotdm.client.Exchange;
import org.opendaylight.iotdm.primitive.RequestPrimitive;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Samuel Grenier
 */
public class OneM2MServer {

    private final Node parent;

    private OneM2MServer(Node parent) {
        if (parent == null) {
            throw new NullPointerException("parent");
        }
        this.parent = parent;
    }

    public void addCSE(String cseName) {
        NodeBuilder b = parent.createChild(cseName);
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
