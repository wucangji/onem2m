package org.dsa.iot.onem2m.actions.resource;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.actions.table.Row;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.onem2m.server.BaseCSE;
import org.opendaylight.iotdm.primitive.AE;
import org.vertx.java.core.Handler;

/**
 * Created by canwu on 9/14/15.
 */
public class AddAE  implements Handler<ActionResult> {

    private final BaseCSE cse;

    private AddAE(BaseCSE cse) {
        this.cse = cse;
    }
    @Override
    /**
     * Only CSE has the right to ad AE,
     */
    public void handle(ActionResult event) {
        Node node = event.getNode().getParent();


        Node valNode = node.getChild("ty");
        String ret = null;
        AE ae = new AE();
        ae.setAppID(event.getParameter("App-ID", ValueType.STRING).getString());

        if (event.getParameter("AppName") != null) {
            ae.setAppName(event.getParameter("AppName", ValueType.STRING).getString());
        }
        if (event.getParameter("OntologyRef") != null) {
            ae.setOntologyRef(event.getParameter("OntologyRef", ValueType.STRING).getString());
        }
        if (event.getParameter("NodeLink") != null) {
            ae.setNodeLink(event.getParameter("NodeLink", ValueType.STRING).getString());
        }
        if (valNode.getValue().toString().compareTo("5") == 0) {
            String TargetContainerURI = node.getChild("rn").getValue().toString();
            String Name = event.getParameter("Name", ValueType.STRING).getString();
            ret = cse.createAEwithName(TargetContainerURI, Name, ae);
        }

        event.getTable().addRow(Row.make(new Value(ret)));
    }

    public static Action make(BaseCSE cse) {
        Action act = new Action(Permission.WRITE, new AddAE(cse));
        {
            Parameter p = new Parameter("Name", ValueType.STRING);
            p.setPlaceHolder("type a name here");
            act.addParameter(p);
        }
        {
            Parameter p = new Parameter("App-ID", ValueType.STRING);
            p.setPlaceHolder("type a String here");
            act.addParameter(p);
        }
        {
            Parameter p = new Parameter("AppName", ValueType.STRING);
            p.setPlaceHolder("type a String here");
            act.addParameter(p);
        }
        {
            Parameter p = new Parameter("OntologyRef", ValueType.STRING);
            p.setPlaceHolder("type a String here");
            act.addParameter(p);
        }
        {
            Parameter p = new Parameter("NodeLink", ValueType.STRING);
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
