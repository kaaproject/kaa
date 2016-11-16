/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.admin.services.messaging;

import com.google.common.net.UrlEscapers;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.kaaproject.kaa.server.admin.services.dao.PropertiesFacade;
import org.kaaproject.kaa.server.admin.services.entity.gen.GeneralProperties;
import org.kaaproject.kaa.server.admin.services.entity.gen.SmtpMailProperties;
import org.kaaproject.kaa.server.admin.shared.util.UrlParams;
import org.kaaproject.kaa.server.admin.shared.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service("messagingService")
public class MessagingServiceImpl implements MessagingService {

  private static final Logger LOG = LoggerFactory.getLogger(MessagingServiceImpl.class);

  @Autowired
  private PropertiesFacade propertiesFacade;

  @Autowired
  private MessageSource messages;

  private JavaMailSenderImpl kaaMessagingMailSender;

  private ExecutorService sendPool;

  private int sendPoolSize;

  private int sendTimeout;

  @Value("#{'http://' + properties[transport_public_interface] + ':' + properties[admin_port]}")
  private String appBaseUrl;

  private String appName;

  private String mailFrom;

  public MessagingServiceImpl() {
  }

  public void setSendPoolSize(int sendPoolSize) {
    this.sendPoolSize = sendPoolSize;
  }

  public void setSendTimeout(int sendTimeout) {
    this.sendTimeout = sendTimeout;
  }

  /**
   * Initialize messaging service. Call after bean creation (bean init-method).
   */
  public void init() {
    String sendName = "send-message-call-runner-%d";
    sendPool = Executors.newFixedThreadPool(
        sendPoolSize, new ThreadFactoryBuilder().setNameFormat(sendName).build());
    configureMailSender();
  }

  /**
   * Bean destroy-method. Call before bean destroying.
   */
  public void destroy() {
    if (sendPool != null) {
      sendPool.shutdown();
      try {
        while (sendPool.isTerminated() == false) {
          sendPool.awaitTermination(sendTimeout, TimeUnit.SECONDS);
        }
      } catch (InterruptedException ex) {
        LOG.warn("shutdown interrupted on {}", sendPool, ex);
      }
    }
  }

  @Override
  public void configureMailSender() {
    SmtpMailProperties smtpMailProperties = propertiesFacade.getSpecificProperties(
        SmtpMailProperties.class);
    kaaMessagingMailSender = new JavaMailSenderImpl();
    kaaMessagingMailSender.setHost(smtpMailProperties.getSmtpHost());
    kaaMessagingMailSender.setPort(smtpMailProperties.getSmtpPort());
    kaaMessagingMailSender.setUsername(smtpMailProperties.getUsername());
    kaaMessagingMailSender.setPassword(smtpMailProperties.getPassword());
    Properties javaMailProperties = toJavaMailProperties(smtpMailProperties);
    kaaMessagingMailSender.setJavaMailProperties(javaMailProperties);
    mailFrom = smtpMailProperties.getMailFrom();

    GeneralProperties generalProperties = propertiesFacade.getSpecificProperties(
        GeneralProperties.class);
    // appBaseUrl = generalProperties.getBaseUrl(); getBaseUrl return always localhost:8080 and
    // retrieve this data from DB instead of kaa-node property file ; TODO(KAA-1619) refactor GeneralProperties
    appName = generalProperties.getAppTitle();
  }

  private Properties toJavaMailProperties(SmtpMailProperties smtpMailProperties) {
    Properties javaMailProperties = new Properties();
    String protocol = smtpMailProperties.getSmtpProtocol().toString().toLowerCase();
    javaMailProperties.put("mail.transport.protocol", protocol);
    javaMailProperties.put("mail."
        + protocol + ".host", smtpMailProperties.getSmtpHost());
    javaMailProperties.put("mail."
        + protocol + ".port", String.valueOf(smtpMailProperties.getSmtpPort()));
    javaMailProperties.put("mail."
        + protocol + ".timeout", String.valueOf(smtpMailProperties.getTimeout()));
    javaMailProperties.put("mail."
        + protocol + ".auth", String.valueOf(!Utils.isEmpty(smtpMailProperties.getUsername())));
    javaMailProperties.put("mail."
        + protocol + ".starttls.enable", String.valueOf(smtpMailProperties.getEnableTls()));
    javaMailProperties.put("mail.debug", "true");
    return javaMailProperties;
  }

  @Override
  public void sendTempPassword(final String username,
                               final String password,
                               final String email) throws Exception {
    String subject = messages.getMessage(
        "tempPasswordMailMessageSubject", new Object[]{appName}, Locale.ENGLISH);
    String text = messages.getMessage(
        "tempPasswordMailMessageBody",
        new Object[]{appBaseUrl, appName, username, password},
        Locale.ENGLISH);
    MimeMessage mimeMsg = kaaMessagingMailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, "UTF-8");
    helper.setFrom(mailFrom);
    helper.setTo(email);
    helper.setSubject(subject);
    helper.setText(text, true);
    kaaMessagingMailSender.send(helper.getMimeMessage());
  }

  @Override
  public void sendPasswordResetLink(final String passwordResetHash,
                                    final String username,
                                    final String email) {
    try {
      callAsync(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          Map<String, String> paramsMap = new HashMap<>();
          paramsMap.put(UrlParams.RESET_PASSWORD, passwordResetHash);
          String params = "#" + generateParamsUrl(paramsMap);
          String subject = messages.getMessage(
              "resetPasswordMailMessageSubject", new Object[]{appName}, Locale.ENGLISH);
          String text = messages.getMessage(
              "resetPasswordMailMessageBody",
              new Object[]{username, appName, appBaseUrl + params},
              Locale.ENGLISH);
          MimeMessage mimeMsg = kaaMessagingMailSender.createMimeMessage();
          MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, "UTF-8");
          try {
            helper.setFrom(mailFrom);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(text, true);
            kaaMessagingMailSender.send(helper.getMimeMessage());
          } catch (MessagingException ex) {
            LOG.error("Unexpected error while sendPasswordResetLinkMail", ex);
          }
          return null;
        }
      });
    } catch (Exception ex) {
      LOG.error("Unexpected error while sendPasswordResetLinkMail", ex);
    }
  }

  /*
  * Use instead of UrlParams#generateParamsUrl() cause it uses com.google.gwt.http.client.URL that stop
  * execution of code on server side.
  * */
  private String generateParamsUrl(Map<String, String> paramsMap) {
    StringBuilder paramsUrl = new StringBuilder();
    for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
      String val = entry.getValue();
      if (paramsUrl.length() > 0) {
        paramsUrl.append("&");
      }
      paramsUrl.append(entry.getKey())
          .append("=")
          .append(UrlEscapers.urlPathSegmentEscaper().escape(val));
    }
    return paramsUrl.toString();
  }



  @Override
  public void sendPasswordAfterReset(final String username,
                                     final String password,
                                     final String email) {
    try {
      callAsync(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          String subject = messages.getMessage(
              "passwordWasResetMailMessageSubject", new Object[]{appName}, Locale.ENGLISH);
          String text = messages.getMessage(
              "passwordWasResetMailMessageBody",
              new Object[]{username, appBaseUrl, appName, password},
              Locale.ENGLISH);
          MimeMessage mimeMsg = kaaMessagingMailSender.createMimeMessage();
          MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, "UTF-8");
          try {
            helper.setFrom(mailFrom);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(text, true);
            kaaMessagingMailSender.send(helper.getMimeMessage());
          } catch (MessagingException ex) {
            LOG.error("Unexpected error while sendPasswordAfterResetMail", ex);
          }
          return null;
        }
      });
    } catch (Exception ex) {
      LOG.error("Unexpected error while sendPasswordAfterResetMail", ex);
    }
  }

  private <T> void callAsync(Callable<T> callable) throws IOException, InterruptedException {
    sendPool.submit(callable);
  }

  @SuppressWarnings("unused")
  private <T> T callWithTimeout(Callable<T> callable) throws IOException, InterruptedException {
    Future<T> future = sendPool.submit(callable);
    try {
      if (sendTimeout > 0) {
        return future.get(sendTimeout, TimeUnit.SECONDS);
      } else {
        return future.get();
      }
    } catch (TimeoutException ex) {
      future.cancel(true);
      throw new IOException("Callable timed out after " + sendTimeout
          + " sec", ex);
    } catch (ExecutionException e1) {
      Throwable cause = e1.getCause();
      if (cause instanceof IOException) {
        throw (IOException) cause;
      } else if (cause instanceof InterruptedException) {
        throw (InterruptedException) cause;
      } else if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      } else if (cause instanceof Error) {
        throw (Error) cause;
      } else {
        throw new RuntimeException(e1);
      }
    } catch (CancellationException ce) {
      LOG.error("Blocked callable interrupted by rotation event", ce);
      throw new InterruptedException(
          "Blocked callable interrupted by rotation event");
    } catch (InterruptedException ex) {
      LOG.warn("Unexpected Exception {}", ex.getMessage(), ex);
      throw ex;
    }
  }
}
