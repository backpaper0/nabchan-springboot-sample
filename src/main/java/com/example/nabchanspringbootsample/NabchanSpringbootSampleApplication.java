package com.example.nabchanspringbootsample;

import java.util.ArrayList;
import java.util.Collection;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import nablarch.common.handler.DbConnectionManagementHandler;
import nablarch.common.handler.TransactionManagementHandler;
import nablarch.core.db.connection.BasicDbConnectionFactoryForDataSource;
import nablarch.core.db.dialect.H2Dialect;
import nablarch.core.db.statement.BasicStatementFactory;
import nablarch.core.db.transaction.JdbcTransactionFactory;
import nablarch.fw.Handler;
import nablarch.fw.handler.GlobalErrorHandler;
import nablarch.fw.handler.RequestPathJavaPackageMapping;
import nablarch.fw.web.handler.HttpCharacterEncodingHandler;
import nablarch.fw.web.handler.HttpResponseHandler;
import nablarch.fw.web.handler.SecureHandler;
import nablarch.fw.web.servlet.WebFrontController;

@SpringBootApplication
public class NabchanSpringbootSampleApplication implements ApplicationContextAware {

    public static void main(final String[] args) {
        SpringApplication.run(NabchanSpringbootSampleApplication.class, args);
    }

    @Autowired
    private DataSource dataSource;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public WebFrontController webFrontController() {
        final WebFrontController wfc = new WebFrontController();
        final Collection<Handler<?, ?>> handlers = new ArrayList<>();
        handlers.add(new HttpCharacterEncodingHandler());
        handlers.add(new GlobalErrorHandler());
        handlers.add(new SecureHandler());
        handlers.add(new HttpResponseHandler());
        handlers.add(dbConnectionManagementHandler());
        handlers.add(transactionManagementHandler());
        handlers.add(requestPathJavaPackageMapping());
        wfc.setHandlerQueue(handlers);
        return wfc;
    }

    @Bean
    public RequestPathJavaPackageMapping requestPathJavaPackageMapping() {
        final SpringRequestPathJavaPackageMapping mapping = new SpringRequestPathJavaPackageMapping();
        mapping.setApplicationContext(applicationContext);
        mapping.setBasePackage("com.example.nabchanspringbootsample.action");
        mapping.setBasePath("/");
        mapping.setClassNameSuffix("Action");
        return mapping;
    }

    @Bean
    public DbConnectionManagementHandler dbConnectionManagementHandler() {
        final BasicDbConnectionFactoryForDataSource connectionFactory = new BasicDbConnectionFactoryForDataSource();
        connectionFactory.setDataSource(new TransactionAwareDataSourceProxy(dataSource));
        connectionFactory.setDialect(new H2Dialect());
        connectionFactory.setStatementFactory(new BasicStatementFactory());

        final DbConnectionManagementHandler handler = new DbConnectionManagementHandler();
        handler.setConnectionFactory(connectionFactory);
        return handler;
    }

    @Bean
    public TransactionManagementHandler transactionManagementHandler() {
        final TransactionManagementHandler handler = new TransactionManagementHandler();
        handler.setTransactionFactory(new JdbcTransactionFactory());
        return handler;
    }
}
