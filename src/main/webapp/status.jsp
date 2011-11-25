<%@ page contentType="text/html;charset=GBK" language="java" %>
<%@ page import="org.apache.commons.httpclient.HttpClient" language="java" %>
<%@ page import="org.apache.commons.httpclient.methods.GetMethod" %>
<%@ page import="org.apache.commons.httpclient.HttpStatus" %>
<%@ page import="org.springframework.web.context.WebApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="common.ConfigCenter" %>
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="gbk"/>
    <meta http-equiv="Expires" content="0">
    <meta http-equiv="kiben" content="no-cache">
    <title>check ucool status</title></head>
<body>
<%
    WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
    ConfigCenter configCenter = (ConfigCenter) wac.getBean("configCenter");
%>
<ul>
    <li>ucool��̨����״̬(ucool-proxy)��
        <%
            HttpClient client = new HttpClient();
            GetMethod method = new GetMethod("http://" +configCenter.getUcoolProxyIp()+ "/a.js");
            try {
                client.executeMethod(method);
                if (method.getStatusCode() == HttpStatus.SC_OK) {
                    out.println("<span style='font-weight:bold;color:rgb(0, 222, 11);'>alive</span>");
                } else {
                    out.println("<span style='font-weight:bold;color:#000'>dead</span>");
                }
            } catch (Exception e) {
                out.println("<span style='color:#eff'>dead</span>");
            } finally {
                //�ͷ�����
                method.releaseConnection();
            }

        %>
    </li>
    <li>�Ա�assets�ճ�������״̬(10.232.16.2)��
    <%
        method = new GetMethod("http://10.232.16.2/s/kissy/1.2.0/kissy-min.js");
        try {
            client.executeMethod(method);
            if (method.getStatusCode() == HttpStatus.SC_OK) {
                out.println("<span style='font-weight:bold;color:rgb(0, 222, 11);'>alive</span>");
            } else {
                out.println("<span style='font-weight:bold;color:#000'>dead</span>");
            }
        } catch (Exception e) {
            out.println("<span style='color:#eff'>dead</span>");
        } finally {
            //�ͷ�����
            method.releaseConnection();
        }
    %>
    </li>
    <li>�Ա�assetsԤ��������״̬(110.75.14.33)��
    <%
        method = new GetMethod("http://110.75.14.33/s/kissy/1.2.0/kissy-min.js");
        try {
            client.executeMethod(method);
            if (method.getStatusCode() == HttpStatus.SC_OK) {
                out.println("<span style='font-weight:bold;color:rgb(0, 222, 11);'>alive</span>");
            } else {
                out.println("<span style='font-weight:bold;color:#000'>dead</span>");
            }
        } catch (Exception e) {
            out.println("<span style='color:#eff'>dead</span>");
        } finally {
            //�ͷ�����
            method.releaseConnection();
        }
    %>
    </li>
</ul>
</body>
</html>