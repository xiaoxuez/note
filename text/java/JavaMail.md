## Java Mail

  > JavaMail是SUN提供给开发人员在应用程序中实现邮件发送和接收功能而提供的一套标准开发类库，支持常用的邮件协议，如SMTP、POP3、IMAP，开发人员使用JavaMail编写邮件程序时，无需考虑底层的通信细节(Socket)，JavaMail也提供了能够创建出各种复杂MIME格式的邮件内容的API
 
### 依赖

```
       <dependency>
			<groupId>com.sun.mail</groupId>
			<artifactId>javax.mail</artifactId>
			<version>1.5.5</version>
		</dependency>
``` 


### 实现

```

  try {
            Properties props = new Properties();
            // 开启debug调试
            props.setProperty("mail.debug", "true");
            // 发送服务器需要身份验证
            props.setProperty("mail.smtp.auth", "true");
            // 设置邮件服务器主机名
            props.setProperty("mail.host", "smtp.exmail.qq.com");
            props.put("mail.smtp.port", "465");
            // 发送邮件协议名称
            props.setProperty("mail.transport.protocol", "smtp");
            //ssl协议
            MailSSLSocketFactory sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.socketFactory", sf);
            // 设置环境信息
            Session session = Session.getInstance(props);

            // 创建邮件对象
            Message msg = new MimeMessage(session);
            msg.setSubject("zhangxiaoxuez");
            // 设置邮件内容
            msg.setContent("amail from zhangxiaoxuez", "text/html;charset=utf-8");
            // 设置发件人
            msg.setFrom(new InternetAddress("****@**.com"));

            Transport transport = session.getTransport();
            // 连接邮件服务器
            transport.connect("****@**.com", "****");
            // 发送邮件
            transport.sendMessage(msg, new Address[]{new InternetAddress("****@**.com")});
            // 关闭连接
            transport.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
```


### 踩坑经验

+ 使用qq邮箱， 163邮箱，需要账户开启SMTP服务，且登录密码为客户端授权密码，不是个人密码...
+ mail.host, 163邮箱为smtp.163.com，qq企业邮箱为smtp.exmail.qq.com，qq个人邮箱为smtp.qq.com。发送邮件的host是这样，接收好像使用imap，只实验了发送功能，qq和qq企业都好用，但163实验失败了，原因是被标记为垃圾或者是有非法东西..分分钟就放弃搞了，用qq的吧。
