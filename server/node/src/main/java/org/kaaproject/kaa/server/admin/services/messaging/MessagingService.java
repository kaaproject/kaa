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

import org.kaaproject.kaa.server.admin.services.dao.PropertiesFacade;
import org.kaaproject.kaa.server.admin.services.entity.gen.GeneralProperties;
import org.kaaproject.kaa.server.admin.services.entity.gen.SmtpMailProperties;
import org.kaaproject.kaa.server.admin.shared.util.UrlParams;
import org.kaaproject.kaa.server.admin.shared.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Repository;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

@Repository("messagingService")
public class MessagingService {

    private static final Logger LOG = LoggerFactory.getLogger(MessagingService.class);

    @Autowired
    private PropertiesFacade propertiesFacade;

    @Autowired
    private MessageSource messages;

    private JavaMailSenderImpl kaaMessagingMailSender;

    private ExecutorService sendPool;

    private int sendPoolSize;

    private int sendTimeout;

    private String appBaseUrl;

    private String appName;

    private String mailFrom;

    public void setSendPoolSize(int sendPoolSize) {
        this.sendPoolSize = sendPoolSize;
    }

    public void setSendTimeout(int sendTimeout) {
        this.sendTimeout = sendTimeout;
    }

    public MessagingService() {
    }

    public void init() {
        String sendName = "send-message-call-runner-%d";
        sendPool = Executors.newFixedThreadPool(sendPoolSize, new ThreadFactoryBuilder().setNameFormat(sendName).build());
        configureMailSender();
    }

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

    public void configureMailSender() {
        SmtpMailProperties smtpMailProperties = propertiesFacade.getSpecificProperties(SmtpMailProperties.class);
        Properties javaMailProperties = toJavaMailProperties(smtpMailProperties);
        kaaMessagingMailSender = new JavaMailSenderImpl();
        kaaMessagingMailSender.setHost(smtpMailProperties.getSmtpHost());
        kaaMessagingMailSender.setPort(smtpMailProperties.getSmtpPort());
        kaaMessagingMailSender.setUsername(smtpMailProperties.getUsername());
        kaaMessagingMailSender.setPassword(smtpMailProperties.getPassword());
        kaaMessagingMailSender.setJavaMailProperties(javaMailProperties);
        mailFrom = smtpMailProperties.getMailFrom();
        
        GeneralProperties generalProperties = propertiesFacade.getSpecificProperties(GeneralProperties.class);
        appBaseUrl = generalProperties.getBaseUrl();
        appName = generalProperties.getAppTitle();
    }

    private Properties toJavaMailProperties(SmtpMailProperties smtpMailProperties) {
        Properties javaMailProperties = new Properties();
        String protocol = smtpMailProperties.getSmtpProtocol().toString().toLowerCase();
        javaMailProperties.put("mail.transport.protocol", protocol);
        javaMailProperties.put("mail." + protocol + ".host", smtpMailProperties.getSmtpHost());
        javaMailProperties.put("mail." + protocol + ".port", String.valueOf(smtpMailProperties.getSmtpPort()));
        javaMailProperties.put("mail." + protocol + ".timeout", String.valueOf(smtpMailProperties.getTimeout()));
        javaMailProperties.put("mail." + protocol + ".auth", String.valueOf(!Utils.isEmpty(smtpMailProperties.getUsername())));
        javaMailProperties.put("mail." + protocol + ".starttls.enable", String.valueOf(smtpMailProperties.getEnableTls()));
        javaMailProperties.put("mail.debug", "true");
        return javaMailProperties;
    }

    public void sendTempPassword(final String username, final String password, final String email) throws Exception {
        String subject = messages.getMessage("tempPasswordMailMessageSubject", new Object[]{appName}, Locale.ENGLISH);
        String text = messages.getMessage("tempPasswordMailMessageBody", new Object[]{appBaseUrl, appName, username, password}, Locale.ENGLISH);
        MimeMessage mimeMsg = kaaMessagingMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, "UTF-8");
        helper.setFrom(mailFrom);
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(text, true);
        kaaMessagingMailSender.send(helper.getMimeMessage());
    }

    public void sendPasswordResetLink(final String passwordResetHash, final String username, final String email) {
        try {
            callAsync(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Map<String,String> paramsMap = new HashMap<>();
                    paramsMap.put(UrlParams.RESET_PASSWORD, passwordResetHash);
                    String params = "#" + UrlParams.generateParamsUrl(paramsMap);
                    String subject =  messages.getMessage("resetPasswordMailMessageSubject", new Object[]{appName}, Locale.ENGLISH);
                    String text = messages.getMessage("resetPasswordMailMessageBody", new Object[]{username, appName, appBaseUrl+params}, Locale.ENGLISH);
                    MimeMessage mimeMsg = kaaMessagingMailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, "UTF-8");
                    try {
                        helper.setFrom(mailFrom);
                        helper.setTo(email);
                        helper.setSubject(subject);
                        helper.setText(text, true);
                        kaaMessagingMailSender.send(helper.getMimeMessage());
                    } catch (MessagingException e) {
                        LOG.error("Unexpected error while sendPasswordResetLinkMail", e);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            LOG.error("Unexpected error while sendPasswordResetLinkMail", e);
        }
    }

    public void sendPasswordAfterReset(final String username, final String password, final String email) {
        try {
            callAsync(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    String subject =  messages.getMessage("passwordWasResetMailMessageSubject", new Object[]{appName}, Locale.ENGLISH);
                    String text = messages.getMessage("passwordWasResetMailMessageBody", new Object[]{username, appBaseUrl, appName, password}, Locale.ENGLISH);
                    MimeMessage mimeMsg = kaaMessagingMailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, "UTF-8");
                    try {
                        helper.setFrom(mailFrom);
                        helper.setTo(email);
                        helper.setSubject(subject);
                        helper.setText(text, true);
                        kaaMessagingMailSender.send(helper.getMimeMessage());
                    } catch (MessagingException e) {
                        LOG.error("Unexpected error while sendPasswordAfterResetMail", e);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            LOG.error("Unexpected error while sendPasswordAfterResetMail", e);
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
        } catch (TimeoutException eT) {
            future.cancel(true);
            throw new IOException("Callable timed out after " + sendTimeout
                    + " sec", eT);
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
