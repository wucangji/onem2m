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
import org.opendaylight.iotdm.primitive.Cnt;
import org.dsa.iot.dslink.util.handler.Handler;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Created by canwu on 9/14/15.
 */
public class AddContainer implements Handler<ActionResult> {

    private final BaseCSE cse;

    private AddContainer(BaseCSE cse) {
        this.cse = cse;
    }
    @Override
    public void handle(ActionResult event) {
        Node node = event.getNode().getParent();


        Node valNode = node.getChild("val");
        String ret = null;
        //Cnt cnt = new Cnt();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"m2m:cnt\":{");
        int state = 0;
        if (event.getParameter("MaxNrOfInstances") != null) {
            String mnivalue = event.getParameter("MaxNrOfInstances", ValueType.NUMBER).toString();
            if (state != 0) {
                sb.append(",");
            }
            sb.append("\"mni\":" + mnivalue );
            state++;
            //cnt.setMni(BigInteger.valueOf(mnivalue));
        }
        if (event.getParameter("MaxByteSize") != null) {
            if (state != 0) {
                sb.append(",");
            }
            String mbsvalue = event.getParameter("MaxByteSize", ValueType.NUMBER).getNumber().toString();
            sb.append("\"mbs\":" + mbsvalue );
            state++;
            //cnt.setMbs(BigInteger.valueOf(mbsvalue));
        }
        if (event.getParameter("Labels") != null) {
            if (state != 0) {
                sb.append(",");
            }
            String lbl = event.getParameter("Labels", ValueType.ARRAY).getString();
            //sb.append(",\"apn\":" + "\"" + apnvalue + "\"");
            //todo cnt.setlbl();  ???
            System.out.println("lbl: " + lbl);
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
            //cnt.setEt(expirationTime);
        }
        if (event.getParameter("Name") != null) {
            if (state != 0) {
                sb.append(",");
            }
            String name = event.getParameter("Name", ValueType.STRING).getString();
            sb.append("\"rn\":" + "\"" + name + "\"");
            state++;
        }
        sb.append("}}");
        String cntpayload = sb.toString();
//        ObjectMapper mapper = new ObjectMapper();
//        String JsonString = "";
//        try {
//            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
//            JsonString = mapper.writeValueAsString(cnt);
//
//        }catch (JsonParseException e) {
//            e.printStackTrace();
//        } catch (JsonMappingException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        String cntpayload = "{\"m2m:cnt\":" + JsonString + "}";
        System.out.println("Container Json String: " + cntpayload);
        String TargetContainerURI = "";
        if (valNode != null) {
            TargetContainerURI = valNode.getValue().toString();
        } else if (node.getChild("ty").getValue().toString().compareTo("5") == 0) {
            TargetContainerURI = node.getChild("rn").getValue().toString();
//            String Name = event.getParameter("Name", ValueType.STRING).getString();
//            ret = cse.createContainerwithName(TargetContainerURI, Name, cntpayload);
//            cse.discoverThisUri(TargetContainerURI);
        }

        ret = cse.createContainer(TargetContainerURI, cntpayload);
        cse.discoverThisUri(TargetContainerURI);

        if (ret == null) {
            ret = "Failed to add Container";
        }

        event.getTable().addRow(Row.make(new Value(ret)));
    }

    public static Action make(BaseCSE cse) {
        Action act = new Action(Permission.WRITE, new AddContainer(cse));
        {
            Parameter p = new Parameter("Name", ValueType.STRING);
            p.setPlaceHolder("type a name here");
            act.addParameter(p);
        }
        {
            Parameter p = new Parameter("MaxNrOfInstances", ValueType.STRING);
            p.setPlaceHolder("type a Integer here");
            act.addParameter(p);
        }
        {
            Parameter p = new Parameter("MaxByteSize", ValueType.STRING);
            p.setPlaceHolder("type a Integer here");
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
