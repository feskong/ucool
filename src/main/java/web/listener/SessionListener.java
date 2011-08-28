package web.listener;

import dao.entity.UserDO;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import web.handler.impl.PersonConfigHandler;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: czy-thinkpad
 * Date: 11-5-27
 * Time: ����5:14
 * To change this template use File | Settings | File Templates.
 */
public class SessionListener implements HttpSessionListener {
    @Override
    public void sessionCreated(HttpSessionEvent event) {
//        System.out.println("session create:" + event.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(event.getSession().getServletContext());
        PersonConfigHandler personConfigHandler = (PersonConfigHandler) context.getBean("personConfigHandler");
        Map<String, UserDO> userCache = personConfigHandler.getUserCache();
        Map<String, UserDO> ipCache = personConfigHandler.getIpCache();
        String guid = (String) session.getAttribute(session.getId());
        if(guid != null) {
            ipCache.remove(userCache.get(guid).getHostName());
            userCache.remove(guid);
        }
    }
}
