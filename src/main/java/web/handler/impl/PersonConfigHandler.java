package web.handler.impl;

import common.ConfigCenter;
import common.PersonConfig;
import dao.UserDAO;
import dao.entity.UserDO;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:czy88840616@gmail.com">czy</a>
 * @since 10-12-10 ����10:58
 */
public class PersonConfigHandler {
    private UserDAO userDAO;

    private ConfigCenter configCenter;

    public void setConfigCenter(ConfigCenter configCenter) {
        this.configCenter = configCenter;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }


    /**
     * Method doHandler ...
     *
     * @param request of type HttpServletRequest
     * @return PersonConfig
     * @throws IOException when
     * @throws ServletException when
     */
    public PersonConfig doHandler(HttpServletRequest request)
            throws IOException, ServletException {
        String remoteHost = request.getRemoteHost();
        String querySring = request.getQueryString();
        String pcname = null;
        if(querySring != null && querySring.indexOf("pcname") != -1) {
            Matcher matc = Pattern.compile("(?<=pcname=)[^?&]+").matcher(querySring);

            if (matc.find()) {
                pcname = matc.group();
            }
        }

        //����combo���������ʱ�������ֻ������������
        if (pcname != null) {
            remoteHost = pcname.toString();
        } else if (remoteHost == null){
            remoteHost = request.getRemoteAddr();
        }
        UserDO personInfo = this.userDAO.getPersonInfo(remoteHost);
        PersonConfig personConfig = new PersonConfig();
        personConfig.setConfigCenter(configCenter);
        if (personInfo != null) {
            personConfig.setUserDO(personInfo);
        } else {
            personConfig.setUserDO(new UserDO());
            personConfig.getUserDO().setHostName(remoteHost);
            //û�����ݿ��ѯ�����ݣ��϶�������
            personConfig.setNewUser(true);
        }
        return personConfig;
    }

}
