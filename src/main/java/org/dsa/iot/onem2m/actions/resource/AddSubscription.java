package org.dsa.iot.onem2m.actions.resource;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.actions.table.Row;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.util.handler.Handler;
import org.dsa.iot.onem2m.server.BaseCSE;

/**
 * Created by canwu on 1/26/16.
 */
public class AddSubscription implements Handler<ActionResult> {

    // default notification type:  wholeresource
    private final BaseCSE cse;

    private AddSubscription(BaseCSE cse) {
        this.cse = cse;
    }
    @Override
    public void handle(ActionResult event) {
        Node node = event.getNode().getParent();


        Node valNode = node.getChild("val");
        String ret = null;
        //Cnt cnt = new Cnt();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"m2m:sub\":{");
        int state = 0;
        if (event.getParameter("NotificationURI") != null) {
            String nu = event.getParameter("NotificationURI", ValueType.ARRAY).getString();
            //todo cnt.setlbl();  ???
            System.out.println("nu: " + nu);
            sb.append("\"nu\":" + nu);
            state++;
        }
        if (event.getParameter("SubscriberURI") != null) {
            if (state != 0) {
                sb.append(",");
            }
            String su = event.getParameter("SubscriberURI", ValueType.STRING).getString();
            sb.append("\"su\":" + su );
            state++;
        }
        if (event.getParameter("Labels") != null) {
            if (state != 0) {
                sb.append(",");
            }
            String lbl = event.getParameter("Labels", ValueType.ARRAY).getString();
            sb.append("\"lbl\":" + lbl);
            state++;
        }
        if (event.getParameter("ExpirationTime") != null) {
            if (state != 0) {
                sb.append(",");
            }
            String expirationTime = event.getParameter("ExpirationTime", ValueType.STRING).getString();
            sb.append("\"et\":" + "\"" + expirationTime + "\"");
            state++;
        }
        if (event.getParameter("Name") != null) {
            if (state != 0) {
                sb.append(",");
            }
            String name = event.getParameter("Name", ValueType.STRING).getString();
            sb.append("\"rn\":" + "\"" + name + "\"");
            state++;
        }
        if (event.getParameter("NotificationEventCriteria") != null) {
            if (state != 0) {
                sb.append(",");
            }
            String enc = event.getParameter("NotificationEventCriteria", ValueType.STRING).getString();
            sb.append("\"enc\":" +  enc );
            state++;
        }

        // set notification content type to URI
        sb.append(",\"nct\":" + 3 );

        sb.append("}}");
        String subspayload = sb.toString();
        System.out.println("Subscription Json String: " + subspayload);
        String TargetContainerURI = "";
        if (valNode != null) {
            TargetContainerURI = valNode.getValue().toString();
        } else if (node.getChild("ty").getValue().toString().compareTo("5") == 0) {
            TargetContainerURI = node.getChild("rn").getValue().toString();
        }

        ret = cse.createSubsciption(TargetContainerURI, subspayload);
        cse.discoverThisUri(TargetContainerURI);

        if (ret == null) {
            ret = "Failed to add Subscription";
        }

        event.getTable().addRow(Row.make(new Value(ret)));
    }

    public static Action make(BaseCSE cse) {
        Action act = new Action(Permission.WRITE, new AddSubscription(cse));
        {
            Parameter p = new Parameter("Name", ValueType.STRING);
            p.setPlaceHolder("type a name here");
            act.addParameter(p);
        }
        {
            Parameter p = new Parameter("NotificationEventCriteria", ValueType.STRING);
            p.setPlaceHolder("type a Object String here");
            act.addParameter(p);
        }
        {
            Parameter p = new Parameter("NotificationURI", ValueType.STRING);
            p.setPlaceHolder("type a List here");
            act.addParameter(p);
        }
        {
            Parameter p = new Parameter("SubscriberURI", ValueType.STRING);
            p.setPlaceHolder("type a String here");
            act.addParameter(p);
        }
        {
            Parameter p = new Parameter("Labels", ValueType.STRING);
            p.setPlaceHolder("type a String List here");
            act.addParameter(p);
        }
        {
            Parameter p = new Parameter("ExpirationTime", ValueType.STRING);
            p.setPlaceHolder("type a String here");
            act.addParameter(p);
        }
        {
            Parameter p = new Parameter("Response", ValueType.STRING);
            act.addResult(p);
        }
        return act;
    }
}
