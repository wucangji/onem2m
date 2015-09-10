package org.dsa.iot.onem2m.actions.resource;

import com.fasterxml.jackson.databind.deser.Deserializers;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeBuilder;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.actions.table.Row;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.onem2m.server.BaseCSE;
import org.dsa.iot.onem2m.server.OneM2MServer;
import org.opendaylight.iotdm.client.Exchange;
import org.opendaylight.iotdm.constant.OneM2M;
import org.opendaylight.iotdm.primitive.RequestPrimitive;
import org.vertx.java.core.Handler;
/**
 * Created by canwu on 9/8/15.
 */
public class AddContentInstance implements Handler<ActionResult>{

    private final BaseCSE cse;

    private AddContentInstance(BaseCSE cse) {
        this.cse = cse;
    }
    @Override
    public void handle(ActionResult event) {
        Node node = event.getNode().getParent();


        Node valNode = node.getChild("val");
        String ret = null;
        if (valNode != null) {
            String TargetContainerURI = valNode.getValue().toString();
            String con = event.getParameter("Con", ValueType.STRING).getString();
            ret = cse.createContentInstanceWithCon(TargetContainerURI, con);
        }

        event.getTable().addRow(Row.make(new Value(ret)));
    }

    public static Action make(BaseCSE cse) {
        Action act = new Action(Permission.WRITE, new AddContentInstance(cse));
        {
            Parameter p = new Parameter("Con", ValueType.STRING);
            p.setPlaceHolder("type a content here");
            act.addParameter(p);
        }
        {
            Parameter p = new Parameter("Response", ValueType.STRING);
            act.addResult(p);
        }
        return act;
    }



}