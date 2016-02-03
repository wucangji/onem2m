package org.dsa.iot.onem2m.server;
import org.dsa.iot.dslink.node.actions.table.Row;
import org.dsa.iot.onem2m.actions.cse.DiscoverCSEwithParameter;
import org.dsa.iot.onem2m.actions.resource.*;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeBuilder;
import org.dsa.iot.dslink.node.NodeManager;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.node.value.ValueUtils;
import org.dsa.iot.dslink.util.Objects;
import org.dsa.iot.onem2m.actions.cse.DeleteCSE;
import org.dsa.iot.onem2m.actions.cse.DiscoverCSE;
import org.opendaylight.iotdm.client.Exchange;
import org.opendaylight.iotdm.client.impl.Http;
import org.opendaylight.iotdm.constant.OneM2M;
import org.opendaylight.iotdm.primitive.*;
import org.dsa.iot.dslink.util.handler.Handler;
//import org.vertx.java.core.json.JsonArray;
//import org.vertx.java.core.json.JsonObject;
import org.dsa.iot.dslink.util.json.JsonArray;
import org.dsa.iot.dslink.util.json.JsonObject;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Samuel Grenier
 */
public class BaseCSE {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseCSE.class);
    private final OneM2MServer server;
    private final Node parent; // parent is the CSENode

    private BaseCSE(OneM2MServer server, Node parent) {
        if (server == null) {
            throw new NullPointerException("server");
        } else if (parent == null) {
            throw new NullPointerException("parent");
        }
        this.server = server;
        this.parent = parent;
    }

    private void init() {
        {
            NodeBuilder b = parent.createChild("deleteCSE");
            b.setDisplayName("Delete CSE");
            b.setSerializable(false);
            b.setAction(DeleteCSE.make());
            b.build();
        }
        {
            NodeBuilder b = parent.createChild("discover");
            b.setDisplayName("OneM2M Sync Up");
            b.setSerializable(false);
            b.setAction(DiscoverCSE.make());
            b.build();
        }
        {
            NodeBuilder b = parent.createChild("discover2");
            b.setDisplayName("Discover");
            b.setSerializable(false);
            b.setAction(DiscoverCSEwithParameter.make());
            b.build();
        }
        {
            NodeBuilder b = parent.createChild("AddContainer");
            b.setDisplayName("Add Container");
            b.setSerializable(false);
            b.setAction(AddContainer.make(this));
            b.build();
        }
        {
            NodeBuilder b = parent.createChild("addAE");
            b.setDisplayName("Add an AE");
            b.setSerializable(false);
            b.setAction(AddAE.make(this));
            b.build();
        }
        // Perform an initial discovery
        //createMemContainer(parent.getName());
        discoverRoot();
        //addSystemMemoryToCSE(); // if use multithread, then will have address already use problem.
    }


    private void addMemConInToCnt() {
        while (true) {
            // how to write a live process?
            String parentContainer = parent.getName() + "/SystemMemoryLeft";
            OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            String restMem = operatingSystemMXBean.getFreePhysicalMemorySize() / 1000000 + " MB";
            System.out.println("Rest Memory" + " = " + restMem);

            createContentInstanceWithCon(parentContainer, restMem);
        }
    }

    public void discoverRoot() {
        Objects.getDaemonThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                Map<String, Node> children = parent.getChildren();
                if (children != null) {
                    for (Node node : children.values()) {
                        if (node.getAction() == null) {
                            parent.removeChild(node);
                        }
                    }
                }
                buildTree(parent.getName() + "?fu=1&rcn=5");
            }
        });
    }

    public void discoverThisUri(final String path) {
        Objects.getDaemonThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                if (path.contains("?")) {
                    buildTree(path);
                } else {
                    buildTree(path + "?fu=1&rcn=5");
                }
            }
        });
    }


    private void buildTree(String to) {
        RequestPrimitive primitive = new RequestPrimitive();
        primitive.setOperation(OneM2M.Operation.RETRIEVE.value());
        primitive.setFrom("dslink");
        primitive.setTo(to);
        primitive.setRequestIdentifier("12345");

        Exchange exchange = server.createExchange(primitive);
        handleResponse(send(exchange));
    }

    public String getResponseJsonString(String to) {
        RequestPrimitive primitive = new RequestPrimitive();
        primitive.setOperation(OneM2M.Operation.RETRIEVE.value());
        primitive.setFrom("dslink");
        primitive.setTo(to);
        primitive.setRequestIdentifier("12345");

        Exchange exchange = server.createExchange(primitive);
        send(exchange);
        JsonObject json = new JsonObject(exchange.getClient().toString());
        Object object = json.get("responsePayload");
        return object.toString();
    }

    private void buildTreeForThisNode(String to, Node node) {
        RequestPrimitive primitive = new RequestPrimitive();
        primitive.setOperation(OneM2M.Operation.RETRIEVE.value());
        primitive.setFrom("dslink");
        primitive.setTo(to);
        primitive.setRequestIdentifier("12345");

        Exchange exchange = server.createExchange(primitive);
        handleResponseForThisNode(send(exchange), node);
    }
    private synchronized String send(Exchange exchange) {
        Http http = new Http();
        http.start();
        http.send(exchange);
        http.stop();
        System.out.println(exchange.getClient().toString()); // todo: add some check
        return exchange.getClient().toString();
    }

    private void handleResponseForThisNode(String response, Node node) {
        JsonObject json = new JsonObject(response);
        Object object = json.get("responsePayload");
        //System.out.println("response this node:" + object.toString());
        if (object instanceof JsonObject) {
            JsonObject responseJson = (JsonObject) object;
            //System.out.println("FiledName:" + responseJson.getFieldNames().toString());
            Object[] array = responseJson.getMap().keySet().toArray();
            if (array != null) {
                String key = (String)array[0];
                Object realPayload = responseJson.get(key);
                handlePayloadForThisNode((JsonObject)realPayload, node);
            }
        } else {
            String clazz = object.getClass().getName();
            throw new RuntimeException("Unsupported instance: " + clazz);
        }
    }

    private void handleResponse(String response) {
        JsonObject json = new JsonObject(response);
        Object object = json.get("responsePayload");
        if (object instanceof JsonObject) {
            handlePayload((JsonObject) object);
        } else if (object instanceof JsonArray) {
            for (Object obj : (JsonArray) object) {
                if (!(obj instanceof JsonObject)) {
                    continue;
                }
                JsonObject payload = (JsonObject) obj;
                handlePayload(payload);
            }
        } else {
            String clazz = object.getClass().getName();
            throw new RuntimeException("Unsupported instance: " + clazz);
        }
    }

    private void handlePayloadForThisNode(final JsonObject payload, Node node) {
        if (payload == null) {
            return;
        }
        //todo: may not contain rn, for example, error
        String rn = payload.get("rn");

        System.out.println("rn1:" + rn);
        if (rn == null) {
            return;
        }

        handleTreeFields(payload, node);

    }

    private void handlePayload(final JsonObject payload) {
        if (payload == null) {
            return;
        }
        String rn = payload.get("val");
        if (rn == null) {
            JsonObject baseJson = payload.get("m2m:cb"); // todo modify csb to cb
            handleTreeFields(baseJson, parent);
            //createFunctionForCSE(parent, payload);
            return;
        }
        rn = rn.substring(parent.getName().length() + 1);
        System.out.println("rn:" + rn);
        if (rn.isEmpty()) {
            handleTreeFields(payload, parent);
            return;
        }

        String[] split = NodeManager.splitPath(rn);
        NodeBuilder b = parent.createFakeBuilder();
        Node node;
        for (String s : split) {
            node = b.build();
            b = node.createChild(s);
            b.setSerializable(false);
            b.getListener().setOnListHandler(new Handler<Node>() {

                // if it is a parent node, will this listener be triggered? how to get the same result as others?
                @Override
                public void handle(Node event) {
                    //System.out.println("Listed: " + event.getPath());
                    Node node;
                    try {
                        if (event == null) {
                            LOGGER.error("Event is null");
                            return;
                        }
                        Node parent = event.getParent();
                        node = parent.getChild(event.getName());
                        if (node != null) {
                            node = node.getChild("val");
                        } else {
                            LOGGER.error("Own node is null");
                        }
                        //System.out.println("val01:" + node);
                        if (node == null) {
                            node = event.getParent().getChild("val");
                            //System.out.println("val02:" + node);
                            if (node == null) {
                                return;
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("277handle", e);
                        return;
                    }
                    final Node rnNode = node;
                    Objects.getDaemonThreadPool().execute(new Runnable() {
                        // when will this be triggered?
                        @Override
                        public void run() {
                            rnNode.clearChildren();
                            buildTreeForThisNode(rnNode.getValue().toString(), rnNode);
                        }
                    });
                }
            });
        }
        node = b.build();
        handleTreeFields(payload, node);
        int ty = payload.get("typ");
        if (ty == 3) {
            // if this resource is a container.
            createFunctionForContainer(node,payload);
        } else if (ty == 2) {
            // if this resource is an AE
            createFunctionForAE(node, payload);
        } else if (ty == 4) {
            String cinURI = payload.get("val");
            String parentURI = cinURI.substring(0, cinURI.lastIndexOf("/"));
            createLatestNode(node.getParent(), parentURI);
        } else if (ty == 23) {
            createFunctionForSubscription(node, payload);
        }
    }

    public void createFunctionForCSE(Node node, final JsonObject payload) {

        NodeBuilder b = node.createFakeBuilder();// what does this node builder do ?ß

        {
            b = node.createChild("AddContainer");
            b.setDisplayName("Add Container");
            b.setSerializable(false);
            b.setAction(AddContainer.make(this));
            b.build();
        }
        {
            b = node.createChild("addAE");
            b.setDisplayName("Add an AE");
            b.setSerializable(false);
            b.setAction(AddContainer.make(this));
            b.build();
        }
        {
            b = node.createChild("AddSubscription");
            b.setDisplayName("Add Subscription");
            b.setSerializable(false);
            b.setAction(AddSubscription.make(this));
            b.build();
        }
    }

    public void createFunctionForContainer(Node node, final JsonObject payload) {
        // if this resource is a container.
        NodeBuilder b = node.createFakeBuilder();// what does this node builder do ?
        b = node.createChild("GetLatest");
        b.setDisplayName("Get Latest");
        b.setAction(new Action(Permission.READ, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                String latestURI = payload.get("val") + "/latest";
                System.out.println("latestURI" + latestURI);
//                Node latestNode = event.getNode().getParent().getChild("val");
//                latestNode.clearChildren();
                NodeBuilder latest = event.getNode().getParent().createChild("Latest ContentInstance");
                latest.setSerializable(false);
                latest.setValueType(ValueType.DYNAMIC);
                Node latestNode = latest.build();
                buildTreeForThisNode(latestURI, latestNode);
                // todo: remove this build can remove the update of the Container node.
                System.out.println("lst : " + latestNode.getChild("con").getValue().toString());
                //String lastCon = latestNode.getChild("con").getValue().toString();
                String lastCon = getResponseJsonString(latestURI);
                event.getTable().addRow(Row.make(new Value(lastCon)));
            }
        }).addResult(new org.dsa.iot.dslink.node.actions.Parameter("Latest Cin", ValueType.STRING)));
        b.build();

        {
            b = node.createChild("GetSelf");
            b.setDisplayName("Get Self");
            b.setAction(new Action(Permission.READ, new Handler<ActionResult>() {
                @Override
                public void handle(ActionResult event) {
                    String selfURI = payload.get("val");
                    Node selfNode = event.getNode().getParent().getChild("val");
                    selfNode.clearChildren();
                    buildTreeForThisNode(selfURI, selfNode);
                }
            }));
            b.build();
        }
        {
            b = node.createChild("AddContainer");
            b.setDisplayName("Add Container");
            b.setSerializable(false);
            b.setAction(AddContainer.make(this));
            b.build();
        }
        {
            b = node.createChild("AddContentInstance");
            b.setDisplayName("Add ContentInstance");
            b.setSerializable(false);
            b.setAction(AddContentInstance.make(this));
            b.build();
        }
        {
            b = node.createChild("AddSubscription");
            b.setDisplayName("Add Subscription");
            b.setSerializable(false);
            b.setAction(AddSubscription.make(this));
            b.build();
        }
        {
            b = node.createChild("DeleteSelf");
            b.setDisplayName("Delete Self");
            b.setSerializable(false);
            b.setAction(DeleteResource.make(this));
            b.build();
        }
        {
            b = node.createChild("Discover");
            b.setDisplayName("Discover");
            b.setSerializable(false);
            b.setAction(DiscoverwithParameter.make(this));
            b.build();
        }

        {
            b = node.createChild("Onem2mSyncUp");
            b.setDisplayName("OneM2M_Sync_Up");
            b.setSerializable(false);
            b.setAction(new Action(Permission.READ, new Handler<ActionResult>() {
                @Override
                public void handle(ActionResult event) {
                    Node parent = event.getNode().getParent();
                    cleanTheNodeChild(parent);
                    String selfURI = payload.get("val");
                    discoverThisUri(selfURI + "?fu=1&rcn=5");
                }
            }));
            b.build();
        }
    }


    public void createFunctionForAE(Node node, final JsonObject payload) {
        // if this resource is a container.
        NodeBuilder b = node.createFakeBuilder();// what does this node builder do ?

        b = node.createChild("Get Self");
        b.setAction(new Action(Permission.READ, new Handler<ActionResult>() {
            @Override
            public void handle(ActionResult event) {
                String latestURI = payload.get("val");
                System.out.println("latestURI" + latestURI);
                Node selfNode = event.getNode().getParent().getChild("val");
                selfNode.clearChildren();
                buildTreeForThisNode(latestURI, selfNode);
            }
        }));
        b.build();

        {
            b = node.createChild("AddContainer");
            b.setDisplayName("Add Container");
            b.setSerializable(false);
            b.setAction(AddContainer.make(this));
            b.build();
        }
        {
            b = node.createChild("AddSubscription");
            b.setDisplayName("Add Subscription");
            b.setSerializable(false);
            b.setAction(AddSubscription.make(this));
            b.build();
        }
        {
            b = node.createChild("DeleteSelf");
            b.setDisplayName("Delete Self");
            b.setSerializable(false);
            b.setAction(DeleteResource.make(this));
            b.build();
        }
        {
            b = node.createChild("Discover");
            b.setDisplayName("Discover");
            b.setSerializable(false);
            b.setAction(DiscoverwithParameter.make(this));
            b.build();
        }
        {
            b = node.createChild("Onem2mSyncUp");
            b.setDisplayName("OneM2M_Sync_Up");
            b.setSerializable(false);
            b.setAction(new Action(Permission.READ, new Handler<ActionResult>() {
                @Override
                public void handle(ActionResult event) {
                    Node parent = event.getNode().getParent();
                    cleanTheNodeChild(parent);
                    String selfURI = payload.get("val");
                    discoverThisUri(selfURI + "?fu=1&rcn=5");
                }
            }));
            b.build();
        }
    }



    public void createFunctionForSubscription(Node node, final JsonObject payload) {
        // if this resource is a subscription.
        NodeBuilder b = node.createFakeBuilder();
        {
            b = node.createChild("GetSelf");
            b.setDisplayName("Get Self");
            b.setAction(new Action(Permission.READ, new Handler<ActionResult>() {
                @Override
                public void handle(ActionResult event) {
                    String selfURI = payload.get("val");
                    Node selfNode = event.getNode().getParent().getChild("val");
                    selfNode.clearChildren();
                    buildTreeForThisNode(selfURI, selfNode);
                }
            }));
            b.build();
        }
        {
            b = node.createChild("DeleteSelf");
            b.setDisplayName("Delete Self");
            b.setSerializable(false);
            b.setAction(DeleteResource.make(this));
            b.build();
        }
        {
            b = node.createChild("Discover");
            b.setDisplayName("Discover");
            b.setSerializable(false);
            b.setAction(DiscoverwithParameter.make(this));
            b.build();
        }

        {
            b = node.createChild("Onem2mSyncUp");
            b.setDisplayName("OneM2M_Sync_Up");
            b.setSerializable(false);
            b.setAction(new Action(Permission.READ, new Handler<ActionResult>() {
                @Override
                public void handle(ActionResult event) {
                    Node parent = event.getNode().getParent();
                    cleanTheNodeChild(parent);
                    String selfURI = payload.get("val");
                    discoverThisUri(selfURI + "?fu=1&rcn=5");
                }
            }));
            b.build();
        }
    }


    private void handleTreeFields(final JsonObject obj,
                                  final Node node) {
        if (obj == null) {
            return;
        }

        //node.clearChildren();
        Map<String, Object> map = obj.getMap();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String name = entry.getKey();
            // todo: map name to full name
            Value value = ValueUtils.toValue(entry.getValue());
//            if (name.equalsIgnoreCase("con")) {
            if (value.toString().contains("{")) {
                JsonObject conjson= new JsonObject(value.toString());
                NodeBuilder nameNode = node.createChild(name);
                nameNode.setSerializable(false);
                nameNode.setValueType(ValueType.DYNAMIC);
                Node nNode = nameNode.build();
                handleTreeFields(conjson, nNode);
            }
            Node n = node.getChild(name);
            NodeBuilder b;
            if (n == null) {
                b = node.createChild(name);
                b.setSerializable(false);
                b.setValueType(ValueType.DYNAMIC);
            } else {
                b = n.createFakeBuilder();
            }
            n = b.build();
            n.setValueType(ValueType.DYNAMIC);
            n.setValue(value);
        }
    }

    public static void init(Node parent) {
        OneM2MServer server = parent.getParent().getMetaData();
        BaseCSE cse = new BaseCSE(server, parent);
        parent.setMetaData(cse);
        cse.init();
    }


    public String createContentInstanceWithCon (String to, String con) {
        RequestPrimitive primitive = new RequestPrimitive();
        primitive.setOperation(OneM2M.Operation.CREATE.value());
        primitive.setFrom("dslink");
        primitive.setTo(to);
        primitive.setRequestIdentifier("12345");
        primitive.setStringpayload(con);

        Exchange exchange = server.createExchange(primitive);
        //handleResponse(send(exchange));   // can we see the reponse in a seperate place, not in some node?
        // How to see them in the metrics?

        Http http=new Http();
        http.start();
        http.setContentType(OneM2M.ResourceType.CONTENT_INSTANCE.value());
        http.send(exchange);
        //http.cleanContentType(); // This clean step is import, otherwise the ty=5 will added to all the other operation
        http.stop();

        System.out.println(exchange.toString());
        return exchange.getResponsePrimitive().getResponseStatusCode().toString();
    }

    public String createContainer (String to, String containerpayload) {
        RequestPrimitive primitive = new RequestPrimitive();
        primitive.setOperation(OneM2M.Operation.CREATE.value());
        primitive.setFrom("dslink");
        primitive.setTo(to);
        primitive.setRequestIdentifier("12345");
        primitive.setStringpayload(containerpayload);

        Exchange exchange = server.createExchange(primitive);
        //handleResponse(send(exchange));   // can we see the reponse in a seperate place, not in some node?
        // How to see them in the metrics?

        Http http=new Http();
        http.start();
        http.setContentType(OneM2M.ResourceType.CONTAINER.value());
        http.send(exchange);
        //http.cleanContentType(); // This clean step is import, otherwise the ty=5 will added to all the other operation
        http.stop();

        System.out.println(exchange.toString());

        return exchange.getResponsePrimitive().getResponseStatusCode().toString();
    }



    public String createAE (String to, String aepayload) {
        RequestPrimitive primitive = new RequestPrimitive();
        primitive.setOperation(OneM2M.Operation.CREATE.value());
        primitive.setFrom("dslink");
        primitive.setTo(to);
        primitive.setRequestIdentifier("12345");
        primitive.setStringpayload(aepayload);

        Exchange exchange = server.createExchange(primitive);
        //handleResponse(send(exchange));   // can we see the reponse in a seperate place, not in some node?
        // How to see them in the metrics?

        Http http=new Http();
        http.start();
        http.setContentType(OneM2M.ResourceType.AE.value());
        http.send(exchange);
        //http.cleanContentType(); // This clean step is import, otherwise the ty=5 will added to all the other operation
        http.stop();

        System.out.println(exchange.toString());
        return exchange.getResponsePrimitive().getResponseStatusCode().toString();
    }


    public String createSubsciption (String to, String subspayload) {
        RequestPrimitive primitive = new RequestPrimitive();
        primitive.setOperation(OneM2M.Operation.CREATE.value());
        primitive.setFrom("dslink");
        primitive.setTo(to);
        primitive.setRequestIdentifier("12345");
        primitive.setStringpayload(subspayload);

        Exchange exchange = server.createExchange(primitive);
        //handleResponse(send(exchange));   // can we see the reponse in a seperate place, not in some node?
        // How to see them in the metrics?

        Http http=new Http();
        http.start();
        http.setContentType(OneM2M.ResourceType.SUBSCRIPTION.value());
        http.send(exchange);
        //http.cleanContentType(); // This clean step is import, otherwise the ty=5 will added to all the other operation
        http.stop();

        System.out.println(exchange.toString());

        return exchange.getResponsePrimitive().getResponseStatusCode().toString();
    }


    public String deleteResource (String to) {
        RequestPrimitive primitive = new RequestPrimitive();
        primitive.setOperation(OneM2M.Operation.DELETE.value());
        primitive.setFrom("dslink");
        primitive.setTo(to);
        primitive.setRequestIdentifier("12345");

        Exchange exchange = server.createExchange(primitive);
        //handleResponse(send(exchange));   // can we see the reponse in a seperate place, not in some node?
        // How to see them in the metrics?

        Http http=new Http();
        http.start();
        http.send(exchange);
        //http.cleanContentType(); // This clean step is import, otherwise the ty=5 will added to all the other operation
        http.stop();

        System.out.println(exchange.toString());
        return exchange.getResponsePrimitive().getResponseStatusCode().toString();
    }

    public void cleanTheNodeChild(Node parent) {

        Map<String, Node> children = parent.getChildren();
        if (children != null) {
            for (Node node1 : children.values()) {
                if (node1.getAction() == null && !(node1.getName().equalsIgnoreCase("val") || node1.getName().equalsIgnoreCase("typ") || node1.getName().equalsIgnoreCase("nm"))) {
                    parent.removeChild(node1);
                }
            }
        }

    }

    public void createLatestNode(Node node, String containerURI) {

        String latestURI = containerURI + "/latest";
        NodeBuilder latest = node.createChild("LatestContentInstance");
        latest.setSerializable(false);
        //latest.setValueType(ValueType.DYNAMIC);
        Node latestNode = latest.build();
        buildTreeForThisNode(latestURI, latestNode);
    }
}
