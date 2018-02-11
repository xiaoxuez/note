package storm;

/**
 * Created by xiaoxuez on 2017/9/22.
 */
import com.sun.mail.util.MailSSLSocketFactory;
import org.apache.storm.generated.*;
import org.apache.storm.thrift.TException;
import org.apache.storm.thrift.protocol.TBinaryProtocol;
import org.apache.storm.thrift.transport.TFramedTransport;
import org.apache.storm.thrift.transport.TSocket;
import org.apache.storm.thrift.transport.TTransportException;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * Created by xiaoxuez on 2017/9/22.
 */
public class ThriftTest {

    public static Nimbus.Client getClient(String nimbusHost, int nimbusPort) throws TTransportException {
        TSocket tsocket = new TSocket(nimbusHost, nimbusPort);
        TFramedTransport tTransport = new TFramedTransport(tsocket);
        TBinaryProtocol tBinaryProtocol = new TBinaryProtocol(tTransport);
        Nimbus.Client c = new Nimbus.Client(tBinaryProtocol);
        tTransport.open();
        return c;
    }

    public static void main(String[] args) {
        try {
            Nimbus.Client client = getClient("127.0.0.1", 6667);
            List<TopologySummary> topoSummaryList = client.getClusterInfo().get_topologies();
            for (TopologySummary topologySummary : topoSummaryList) {
                TopologyInfo topologyInfo =client.getTopologyInfo(topologySummary.get_id());
                Set<String> errorKeySet =topologyInfo.get_errors().keySet();
                for (String errorKey : errorKeySet) {
                    List<ErrorInfo> listErrorInfo = topologyInfo.get_errors().get(errorKey);
                    for (ErrorInfo ei : listErrorInfo) {
                        // 发生异常的时间
                        long expTime = (long) ei.get_error_time_secs() * 1000;
                        // 现在的时间
                        long now = System.currentTimeMillis();

                        // 由于获取的是全量的错误堆栈，我们可以设置一个范围来获取指定范围的错误，看情况而定
                        // 如果超过5min，那么就不用记录了，因为5min检查一次
                        System.out.println(new Date(expTime).toString() + " : " + ei.get_error());

                    }
                }
            }
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (AuthorizationException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }

//        sendMail();
    }



//    public static void main(String[] args) {
//        try {
//            Properties props = new Properties();
//            // 开启debug调试
//            props.setProperty("mail.debug", "false");
//            // 发送服务器需要身份验证
//            props.setProperty("mail.smtp.auth", "true");
//            // 设置邮件服务器主机名
//            props.setProperty("mail.host", "smtp.qq.com");
//            props.put("mail.smtp.port", "465");
//            // 发送邮件协议名称
//            props.setProperty("mail.transport.protocol", "smtp");
//            MailSSLSocketFactory sf = new MailSSLSocketFactory();
//            sf.setTrustAllHosts(true);
//            props.put("mail.smtp.ssl.enable", "true");
//            props.put("mail.smtp.ssl.socketFactory", sf);
//            // 设置环境信息
//            Session session = Session.getInstance(props);
//
//            // 创建邮件对象
//            Message msg = new MimeMessage(session);
//            msg.setSubject("zhangxiaoxuez");
//            // 设置邮件内容
//            msg.setContent("amail from zhangxiaoxuez", "text/html;charset=utf-8");
//            // 设置发件人
//            msg.setFrom(new InternetAddress("630872633@qq.com"));
//
//            Transport transport = session.getTransport();
//            // 连接邮件服务器
//            transport.connect("630872633@qq.com", "********");
//            // 发送邮件
//            transport.sendMessage(msg, new Address[]{new InternetAddress("******")});
//            // 关闭连接
//            transport.close();
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }
//        catch (GeneralSecurityException e) {
//            e.printStackTrace();
//        }
//
//    }

}