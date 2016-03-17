package org.dsa.iot.onem2m;

import org.dsa.iot.dslink.node.NodeManager;
import org.dsa.iot.dslink.util.json.JsonObject;

import org.apache.commons.io.IOUtils;
import org.dsa.iot.dslink.DSLink;
import org.dsa.iot.dslink.DSLinkFactory;
import org.dsa.iot.dslink.DSLinkHandler;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeBuilder;
import org.dsa.iot.dslink.util.Objects;
import org.dsa.iot.dslink.util.json.JsonArray;
import org.dsa.iot.onem2m.actions.server.AddServer;
import org.dsa.iot.onem2m.server.BaseCSE;
import org.dsa.iot.onem2m.server.OneM2MServer;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.opendaylight.iotdm.client.impl.Http;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jetty.server.Server;
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
        NodeManager manager = link.getNodeManager();
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
        // initiate the server to receive the notification.
        Server server=new Server(8585);
        server.setHandler(new GetNotify(manager));
        try {
            server.start();
            //server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public class GetNotify extends AbstractHandler{

        NodeManager nodeManager;
        private GetNotify (NodeManager manager) {
            this.nodeManager = manager;
        }
        public void handle(String target,
                           Request baseRequest,
                           HttpServletRequest httprequest,
                           HttpServletResponse httpresponse)
                throws IOException, ServletException
        {

            String method = httprequest.getMethod().toLowerCase();
            if (method.equalsIgnoreCase("post")) {
                // if get notification
                System.out.println("DSlink Get notification!!!");
                httpresponse.getWriter().println("<h1>Get Notification</h1>");
            }

            String cn = IOUtils.toString(baseRequest.getInputStream()).trim();
            //{"nev":{"rep":{"m2m:cin":{"cs":4,"ct":"20160205T214618Z","st":2,"con":"fds3","ty":4,"ri":"9","lt":"20160205T214618Z","pi":"4","rn":"9"}},"om":{"op":1,"or":"dslink"}}}
            //{"nev":{"rep":{"rn":"/InCSE1/lay1/dddddddddd"},
            //         "om":{"op":1,"or":"dslink"}}
            // }
            // todo: cast to json then extract the "rn", get the parent URI, sync
            String parentURI = "";

            JsonObject notifyObject = new JsonObject(cn);
            JsonObject nev = notifyObject.get("nev");
            if (nev != null) {
                JsonObject rep = nev.get("rep");
                if (rep != null) {
                    parentURI = rep.get("rn");
                }
            }


            Node base = nodeManager.getNode("local/" + getCSEName(parentURI), false, true).getNode();

            BaseCSE baseCSE = base.getMetaData();

            baseCSE.discoverThisUri(getParentURI(parentURI));


            httpresponse.setContentType("text/html;charset=utf-8");
            httpresponse.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
            httpresponse.getWriter().println("<h1>Hello World</h1>");
        }



    }


    @Override
    public void onResponderConnected(DSLink link) {
        LOGGER.info("Connected");
    }

    public static void main(String[] args) {

        DSLinkFactory.start(args, new Main());
    }

    public static String getCSEName (String targetURI) {
        targetURI = trimURI(targetURI); // get rid of leading and following "/"
        String hierarchy[] = targetURI.split("/"); // split the URI into its hierarchy of path component strings
        return hierarchy[0];
    }

    /**
     * The URI can be /cseBase/x/y/z/, and this routine turns it into cseBase/x/y/z ie. strip leading and trailing /
     * @param uri the URI of the target
     * @return stripped URI
     */
    public static String trimURI(String uri) {
        uri = uri.trim();
        uri = uri.startsWith("/") ? uri.substring("/".length()) : uri;
        uri = uri.endsWith("/") ? uri.substring(0,uri.length()-1) : uri;
        return uri;
    }

    public String getParentURI(String targetURI) {
        targetURI = trimURI(targetURI); // get rid of leading and following "/"
        String hierarchy[] = targetURI.split("/"); // split the URI into its hierarchy of path component strings
        return targetURI.substring(0, targetURI.lastIndexOf(hierarchy[hierarchy.length-1]));
    }
}
