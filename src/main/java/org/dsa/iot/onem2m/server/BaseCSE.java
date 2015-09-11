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
import org.opendaylight.iotdm.primitive.Container;
import org.opendaylight.iotdm.primitive.ContentInstance;
import org.opendaylight.iotdm.primitive.PrimitiveContent;
import org.opendaylight.iotdm.primitive.RequestPrimitive;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.util.Map;

/**
 * @author Samuel Grenier
 */
public class BaseCSE {

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
            b.setDisplayName("Discover");
            b.setSerializable(false);
            b.setAction(DiscoverCSE.make());
            b.build();
        }
        {
            NodeBuilder b = parent.createChild("discover2");
            b.setDisplayName("Custom Discover");
            b.setSerializable(false);
            b.setAction(DiscoverCSEwithParameter.make());
            b.build();
        }
        // Perform an initial discovery

        //createMemContainer(parent.getName());
        discover();
        //addSystemMemoryToCSE(); // if use multithread, then will have address already use problem.
    }

    public void addSystemMemoryToCSE() {
        Objects.getDaemonThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                // Create the container to store the memory information
                createMemContainer(parent.getName());
                addMemConInToCnt();
            }
        });
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

    public void discover() {
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

    public void customeDiscover(final String parameter) {
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
                //final String para = parameter;
                buildTree(parent.getName() + "?fu=1&rcn=5" + parameter);
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
        //System.out.println(exchange.toString());
        return exchange.getClient().toString();
    }

    private void handleResponseForThisNode(String response, Node node) {
        JsonObject json = new JsonObject(response);
        Object object = json.getField("responsePayload");
        if (object instanceof JsonObject) {
            handlePayloadForThisNode((JsonObject) object, node);
        } else {
            String clazz = object.getClass().getName();
            throw new RuntimeException("Unsupported instance: " + clazz);
        }
    }

    private void handleResponse(String response) {
        JsonObject json = new JsonObject(response);
        Object object = json.getField("responsePayload");
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
        String rn = payload.getString("rn");

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
        String rn = payload.getString("val");
        if (rn == null) {
            //rn = payload.getString("pi") + "/" + payload.getString("rn");
            handleTreeFields(payload, parent);
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
                    System.out.println("Listed: " + event.getPath());
                    Node node = event.getChild("val");
                    //System.out.println("val01:" + node);
                    if (node == null) {
                        node = event.getParent().getChild("val");
                        //System.out.println("val02:" + node);
                        if (node == null) {
                            return;
                        }
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
        int ty = payload.getNumber("typ").intValue();
        if (ty == 3) {
            // if this resource is a container.
            b = node.createChild("Get Latest");
            b.setAction(new Action(Permission.READ, new Handler<ActionResult>() {
                @Override
                public void handle(ActionResult event) {
                    String latestURI = payload.getString("val") + "/latest";
                    System.out.println("latestURI" + latestURI);
                    Node latestNode = event.getNode().getParent().getChild("val");
                    latestNode.clearChildren();
                    buildTreeForThisNode(latestURI, latestNode);
                    System.out.println("lst : " + latestNode.getChild("con").getValue().toString());
                    String lastCon = latestNode.getChild("con").getValue().toString();
                    event.getTable().addRow(Row.make(new Value(lastCon)));
                }
            }).addResult(new org.dsa.iot.dslink.node.actions.Parameter("Latest", ValueType.STRING)));
            b.build();

            b = node.createChild("Get Self");
            b.setAction(new Action(Permission.READ, new Handler<ActionResult>() {
                @Override
                public void handle(ActionResult event) {
                    String latestURI = payload.getString("val");
                    System.out.println("latestURI" + latestURI);
                    Node selfNode = event.getNode().getParent().getChild("val");
                    selfNode.clearChildren();
                    buildTreeForThisNode(latestURI, selfNode);
                }
            }));
            b.build();

            {
                b = node.createChild("Add ContentInstance");
                b.setDisplayName("Add ContentInstance");
                b.setSerializable(false);
                b.setAction(AddContentInstance.make(this));
                b.build();
            }

        }
    }

    private void handleTreeFields(final JsonObject obj,
                                  final Node node) {
        if (obj == null) {
            return;
        }

        //node.clearChildren();
        Map<String, Object> map = obj.toMap();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String name = entry.getKey();
            Value value = ValueUtils.toValue(entry.getValue());

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
        ContentInstance conIn = new ContentInstance();
        conIn.setContent(con);

        primitive.setPrimitiveContent(new PrimitiveContent());
        primitive.getPrimitiveContent().getAny().add(conIn);

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

    public String createMemContainer (String to) {
        RequestPrimitive primitive = new RequestPrimitive();
        primitive.setOperation(OneM2M.Operation.CREATE.value());
        primitive.setFrom("dslink");
        primitive.setTo(to);
        primitive.setRequestIdentifier("12345");
        primitive.setName("SystemMemoryLeft");

        Container cnt = new Container();
        cnt.setOntologyRef("good");

        primitive.setPrimitiveContent(new PrimitiveContent());
        primitive.getPrimitiveContent().getAny().add(cnt);

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

}
