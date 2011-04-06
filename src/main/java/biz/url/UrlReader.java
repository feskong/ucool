package biz.url;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:czy88840616@gmail.com">czy</a>
 * @since 11-4-5 ����10:31
 */
public class UrlReader {
    /**
     * �ֽڶ�ȡ���������ӱ��룬��������Լ��ж�
     * 
     * @param outputStream
     * @param inputStream
     * @param fileUrl
     * @param skipCommet
     * @return
     * @throws IOException
     */
    public boolean pushStream(ServletOutputStream outputStream, InputStream inputStream, String fileUrl, boolean skipCommet) throws IOException {
        int n;
        byte[] firstline = new byte[50];
        if((n = inputStream.read(firstline)) >= 0) {
            String first = new String(firstline, 0, n);
            if(first.equals("/*not found*/")) {
                inputStream.close();
//                outputStream.close();
                return false;
            } else {
                if (!skipCommet) {
                    outputStream.println("/*ucool filePath=" + fileUrl + "*/");
                }
                outputStream.write(firstline, 0, n);
            }
        }

        byte[] buf = new byte[1024 * 64];
        while ((n = inputStream.read(buf)) >= 0) {
            outputStream.write(buf, 0, n);
        }
        inputStream.close();
        outputStream.flush();
        return true;
    }
}
