package web.url;

import biz.file.FileEditor;
import biz.url.UrlReader;
import common.ConfigCenter;
import common.MyConfig;
import common.PersonConfig;
import common.tools.UrlTools;
import dao.entity.RequestInfo;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:czy88840616@gmail.com">czy</a>
 * @since 2010-10-2 13:33:26
 */
public class UrlExecutor {

    private FileEditor fileEditor;

    private ConfigCenter configCenter;

    private UrlReader urlReader;

    public void setFileEditor(FileEditor fileEditor) {
        this.fileEditor = fileEditor;
    }

    public void setConfigCenter(ConfigCenter configCenter) {
        this.configCenter = configCenter;
    }

    public void setUrlReader(UrlReader urlReader) {
        this.urlReader = urlReader;
    }

    /**
     * Ϊdebugģʽ���⴦��url���󣬲���cache
     *
     *
     * @param requestInfo
     * @param response
     * @param personConfig
     * @author zhangting
     * @since 10-10-29 ����9:51
     */
    public void doDebugUrlRule(RequestInfo requestInfo, HttpServletResponse response, PersonConfig personConfig) {
        String filePath = requestInfo.getFilePath();
        String realUrl = requestInfo.getRealUrl();
        String curMappingPath = null;
        
        if (validateLocalCombo(requestInfo, response, personConfig)) {
            return;
        }
        if(personConfig.getUserDO().getMappingPath() != null && !"".equals(personConfig.getUserDO().getMappingPath())) {
            // ����ӳ�䲻�߷�����assetsĿ¼
            String[] mappingPaths = personConfig.getUserDO().getMappingPath().split(";");
            // ȡ�õ�ǰ��ӳ��·��
            for (String mappingPath : mappingPaths) {
                if(requestInfo.getFilePath().startsWith(mappingPath)){
                    curMappingPath = mappingPath;
                    break;
                }
            }
        }
        if(personConfig.isEnableLocalMapping() && curMappingPath != null) {
            // ��ip�滻Ϊ�ͻ��˵ģ����ҽ�Ŀ¼ӳ���
            realUrl = realUrl.replaceAll(configCenter.getUcoolProxyIp(), requestInfo.getClientAddr() + ":" + configCenter.getUcoolProxyClientPort());
            realUrl = realUrl.replaceAll(curMappingPath, "");
            requestInfo.setRealUrl(realUrl);
            String fullUrl = requestInfo.getFullUrl();
            fullUrl = fullUrl.replaceAll(configCenter.getUcoolProxyIp(), requestInfo.getClientAddr() + ":" + configCenter.getUcoolProxyClientPort());
            fullUrl = fullUrl.replaceAll(curMappingPath, "");
            requestInfo.setFullUrl(fullUrl);
            //ֱ������ͻ���
            if (!readUrlFile(requestInfo, response)) {
                // ͼƬ�����ظ�����
                if (!requestInfo.getType().equals("assets")) {
                    return;
                }
                if (personConfig.isUcoolAssetsDebug()) {
                    //debug mode���������-min��Դ�ļ�a.js�����������a.source.js������������ﴦ��
                    //����������Ǿ�˵�����϶�û�и��ļ�����ʹ����ѹ�����ļ�Ҳû���⣬ֻҪ��֤�����ܵ�����cache
                    requestInfo.setFilePath(filePath.replace(".source", ""));
                    requestInfo.setRealUrl(realUrl.replace(".source", ""));
                    doDebugUrlRuleCopy(requestInfo, response, personConfig);
                } else {
                    //���ı��ϣ��������ʧ���ˣ�������ȡ��
                    requestInfo.setRealUrl(requestInfo.getFullUrl());
                    readUrlFile(requestInfo, response);
                }
            }
        } else {
            if (findAssetsFile(filePath, personConfig)) {
                try {
                    requestInfo.setRealUrl(requestInfo.getFilePath());
                    this.urlReader.pushStream(requestInfo, response, loadExistFileStream(filePath, personConfig));
                } catch (IOException e) {
                    //���������쳣�������п��ܻ���ʧ�ܣ�����ȡ�����ļ�
                    System.out.println("file has exception" + e);
                }
            } else {
                if (!readUrlFile(requestInfo, response)) {
                    // ͼƬ�����ظ�����
                    if (!requestInfo.getType().equals("assets")) {
                        return;
                    }
                    if (personConfig.isUcoolAssetsDebug()) {
                        //debug mode���������-min��Դ�ļ�a.js�����������a.source.js������������ﴦ��
                        //����������Ǿ�˵�����϶�û�и��ļ�����ʹ����ѹ�����ļ�Ҳû���⣬ֻҪ��֤�����ܵ�����cache
                        requestInfo.setFilePath(filePath.replace(".source", ""));
                        requestInfo.setRealUrl(realUrl.replace(".source", ""));
                        doDebugUrlRuleCopy(requestInfo, response, personConfig);
                    } else {
                        //���ı��ϣ��������ʧ���ˣ�������ȡ��
                        requestInfo.setRealUrl(requestInfo.getFullUrl());
                        readUrlFile(requestInfo, response);
                    }
                }
            }
        }
    }


    public void doDebugUrlRuleCopy(RequestInfo requestInfo, HttpServletResponse response, PersonConfig personConfig) {
        if(personConfig.isEnableLocalMapping() && requestInfo.getFilePath().startsWith(personConfig.getUserDO().getMappingPath())) {
            requestInfo.setRealUrl(requestInfo.getFullUrl());
            readUrlFile(requestInfo, response);
        } else {
            if (validateLocalCombo(requestInfo, response, personConfig)) {
                return;
            }
            if (findAssetsFile(requestInfo.getFilePath(), personConfig)) {
                try {
                    requestInfo.setRealUrl(requestInfo.getFilePath());
                    this.urlReader.pushStream(requestInfo, response, loadExistFileStream(requestInfo.getFilePath(), personConfig));
                } catch (IOException e) {
                    //���������쳣�������п��ܻ���ʧ�ܣ�����ȡ�����ļ�
                    System.out.println("file has exception" + e);
                }
            } else {
                //���ı��ϣ��������ʧ���ˣ�������ȡ��
                requestInfo.setRealUrl(requestInfo.getFullUrl());
                readUrlFile(requestInfo, response);
            }
        }
    }

    public void doDebugUrlRuleForPng(RequestInfo requestInfo, ServletOutputStream out, PersonConfig personConfig) {
        String filePath = requestInfo.getFilePath();
        String realUrl = requestInfo.getRealUrl();

        if (findAssetsFile(filePath, personConfig)) {
            try {
                this.urlReader.pushStream(out, loadExistFileStream(filePath, personConfig), filePath, !requestInfo.getType().equals("assets"));
            } catch (IOException e) {
                //���������쳣�������п��ܻ���ʧ�ܣ�����ȡ�����ļ�
                System.out.println("file has exception" +  e);
            }
        } else {
            readUrlFileForPng(requestInfo, realUrl, out);
        }
    }

    /**
     * ���ݵ�ǰ�û������û�ȡ�ļ�����
     *
     * @param requestInfo
     * @param personConfig
     * @return
     */
    @Deprecated
    private String getConfigEncoding(RequestInfo requestInfo, PersonConfig personConfig) {
        //read properties
        Properties p = new Properties();
        StringBuilder sb = new StringBuilder();
        sb.append(configCenter.getWebRoot()).append(configCenter.getUcoolAssetsRoot()).append(personConfig.getUserRootDir()).append(MyConfig.PERSON_CONFIG_NAME);
        try {
            File comboFile = new File(sb.toString());
            if(comboFile.exists() && comboFile.canRead()) {
                FileReader fileReader = new FileReader(comboFile);
                p.load(fileReader);
                fileReader.close();
            }
        } catch (IOException e) {
        }
        if(!p.isEmpty()) {
            String whileList = p.getProperty(MyConfig.ENCODING_CORRECT_WHITE_LIST);
            if(whileList != null && !whileList.isEmpty()) {
                String[] gbkLists = whileList.split(",");
                for (String gbkList : gbkLists) {
                    if (requestInfo.getFilePath().indexOf(gbkList) != -1) {
                        return "gbk";
                    }
                }
            }
            String utfFiles = p.getProperty(MyConfig.ENCODING_CORRECT);
            if(utfFiles != null && !utfFiles.isEmpty()) {
                String[] utfLists = utfFiles.split(",");
                for (String utfList : utfLists) {
                    if (requestInfo.getFilePath().indexOf(utfList) != -1) {
                        return "utf-8";
                    }
                }
            }
        }
        return "gbk";
    }

    private boolean validateLocalCombo(RequestInfo requestInfo, HttpServletResponse response, PersonConfig personConfig) {
        //���û�Ŀ¼������������Ҫ����combo���ļ�����Ҫ���⴦��
        if(personConfig.isEnableLocalCombo()) {
            //read properties
            Properties p = new Properties();
            StringBuilder sb = new StringBuilder();
            sb.append(configCenter.getWebRoot()).append(configCenter.getUcoolAssetsRoot()).append(personConfig.getUserRootDir()).append(MyConfig.LOCAL_COMBO_CONFIG_NAME);
            try {
                File comboFile = new File(sb.toString());
                if(comboFile.exists() && comboFile.canRead()) {
                    FileReader fileReader = new FileReader(comboFile);
                    p.load(fileReader);
                    fileReader.close();
                }
            } catch (IOException e) {
            }
            if(!p.isEmpty()) {
                //url replace
                boolean matchUrl = false;
                for (Map.Entry<Object, Object> objectObjectEntry : p.entrySet()) {
                    if (((String) objectObjectEntry.getKey()).indexOf(requestInfo.getFilePath()) != -1) {
                        String newUrl = (String) objectObjectEntry.getValue();
                        newUrl = newUrl.replace("{baseUrl}", requestInfo.getServerName());
                        newUrl = "http://" + newUrl + UrlTools.getParam(requestInfo.getRealUrl());
                        //��У�飬����ͬһ�ļ�ѭ������
                        if (newUrl.indexOf((String) objectObjectEntry.getKey()) == -1) {
                            requestInfo.setRealUrl(newUrl);
                            matchUrl = true;
                            break;
                        }
                    }
                }
                if (matchUrl) {
                    String realUrl = requestInfo.getRealUrl();
                    if(realUrl.indexOf("?") != -1) {
                        realUrl += ("?pcname=" + personConfig.getUserDO().getHostName());
                    } else {
                        realUrl += ("&pcname=" + personConfig.getUserDO().getHostName());
                    }
                    requestInfo.setRealUrl(realUrl);
                    requestInfo.setLocalCombo(true);
                    
                    readUrlFile(requestInfo, response);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * ��assetsĿ¼�в��ұ����޸Ĺ����ļ�
     *
     *
     * @param filePath of type String
     * @param personConfig
     * @return boolean
     * @author zhangting
     * @since 2010-8-19 14:49:26
     */
    private boolean findAssetsFile(String filePath, PersonConfig personConfig) {
        if (personConfig.isEnableAssets()) {
            StringBuilder sb = new StringBuilder();
            sb.append(configCenter.getWebRoot()).append(personConfig.getUcoolAssetsRoot()).append(filePath);
            return this.fileEditor.findFile(sb.toString());
        }
        return false;
    }

    /**
     * ���ݱ��뷵���µ��ļ���
     *
     *
     * @param filePath of type String
     * @param encoding of type String
     * @param personConfig
     * @return InputStreamReader
     */
    private InputStreamReader loadExistFileStream(String filePath, String encoding, PersonConfig personConfig) {
        String root = personConfig.getUcoolAssetsRoot();
        StringBuilder sb = new StringBuilder();
        sb.append(configCenter.getWebRoot()).append(root).append(filePath);
        try {
            return this.fileEditor.loadFileStream(sb.toString(), encoding);
        } catch (FileNotFoundException e) {

        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }

    /**
     * �����µ��ļ���
     *
     *
     * @param filePath of type String
     * @param personConfig
     * @return InputStreamReader
     */
    private InputStream loadExistFileStream(String filePath, PersonConfig personConfig) {
        String root = personConfig.getUcoolAssetsRoot();
        StringBuilder sb = new StringBuilder();
        sb.append(configCenter.getWebRoot()).append(root).append(filePath);
        try {
            return new FileInputStream(sb.toString());
        } catch (FileNotFoundException e) {

        }
        return null;
    }

    /**
     * Method readUrlFile ...
     *
     * @param requestInfo
     * @param response
     * @return
     */
    private boolean readUrlFile(RequestInfo requestInfo,  HttpServletResponse response) {
        try {
            URL url = new URL(requestInfo.getRealUrl());
            return this.urlReader.pushStream(requestInfo, response, url.openStream());
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * רΪͼƬ����ķ���
     * @param requestInfo
     * @param fullUrl
     * @param out
     * @return
     */
    private boolean readUrlFileForPng(RequestInfo requestInfo, String fullUrl, ServletOutputStream out) {
        try {
            URL url = new URL(fullUrl);
            return this.urlReader.pushStream(out, url.openStream(), fullUrl, !requestInfo.getType().equals("assets"));
        } catch (Exception e) {
        }
        return false;
    }

}
