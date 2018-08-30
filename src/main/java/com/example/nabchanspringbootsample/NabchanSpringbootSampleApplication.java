package com.example.nabchanspringbootsample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import nablarch.common.handler.DbConnectionManagementHandler;
import nablarch.common.handler.TransactionManagementHandler;
import nablarch.common.web.session.SessionManager;
import nablarch.common.web.session.SessionStore;
import nablarch.common.web.session.SessionStoreHandler;
import nablarch.common.web.session.store.DbStore;
import nablarch.core.date.BasicSystemTimeProvider;
import nablarch.core.db.connection.BasicDbConnectionFactoryForDataSource;
import nablarch.core.db.connection.ConnectionFactory;
import nablarch.core.db.dialect.H2Dialect;
import nablarch.core.db.statement.BasicStatementFactory;
import nablarch.core.db.transaction.JdbcTransactionFactory;
import nablarch.core.db.transaction.SimpleDbTransactionManager;
import nablarch.core.repository.ObjectLoader;
import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.initialization.Initializable;
import nablarch.core.transaction.TransactionFactory;
import nablarch.fw.Handler;
import nablarch.fw.handler.GlobalErrorHandler;
import nablarch.fw.handler.RequestPathJavaPackageMapping;
import nablarch.fw.web.handler.HttpCharacterEncodingHandler;
import nablarch.fw.web.handler.HttpResponseHandler;
import nablarch.fw.web.handler.SecureHandler;
import nablarch.fw.web.servlet.WebFrontController;

@SpringBootApplication
public class NabchanSpringbootSampleApplication
        implements ApplicationContextAware, InitializingBean {

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
        handlers.add(sessionStoreHandler());
        handlers.add(dbConnectionManagementHandler());
        handlers.add(transactionManagementHandler());
        handlers.add(requestPathJavaPackageMapping());
        wfc.setHandlerQueue(handlers);
        return wfc;
    }

    @Bean
    public SimpleDbTransactionManager dbManager() {
        final SimpleDbTransactionManager dbManager = new SimpleDbTransactionManager();
        dbManager.setConnectionFactory(connectionFactory());
        dbManager.setTransactionFactory(transactionFactory());
        return dbManager;
    }

    @Bean
    public SessionStore sessionStore() {
        //        final HttpSessionStore store = new HttpSessionStore();
        final DbStore store = new DbStore();
        store.setExpires(1L, TimeUnit.DAYS);
        store.setDbManager(dbManager());
        return store;
    }

    @Bean
    public SessionManager sessionManager() {
        final SessionManager manager = new SessionManager();
        manager.setAvailableStores(Collections.singletonList(sessionStore()));
        manager.setDefaultStoreName(sessionStore().getName());
        return manager;
    }

    @Bean
    public SessionStoreHandler sessionStoreHandler() {
        final SessionStoreHandler handler = new SessionStoreHandler();
        handler.setSessionManager(sessionManager());
        return handler;
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
        final DbConnectionManagementHandler handler = new DbConnectionManagementHandler();
        handler.setConnectionFactory(connectionFactory());
        return handler;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        final BasicDbConnectionFactoryForDataSource connectionFactory = new BasicDbConnectionFactoryForDataSource();
        connectionFactory.setDataSource(new TransactionAwareDataSourceProxy(dataSource));
        connectionFactory.setDialect(new H2Dialect());
        connectionFactory.setStatementFactory(new BasicStatementFactory());
        return connectionFactory;
    }

    @Bean
    public TransactionManagementHandler transactionManagementHandler() {
        final TransactionManagementHandler handler = new TransactionManagementHandler();
        handler.setTransactionFactory(transactionFactory());
        return handler;
    }

    @Bean
    public TransactionFactory transactionFactory() {
        return new JdbcTransactionFactory();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final Map<String, Object> objects = new HashMap<>();
        objects.put("sessionManager", sessionManager());
        objects.put("systemTimeProvider", new BasicSystemTimeProvider());

        SystemRepository.load(new ObjectLoader() {
            @Override
            public Map<String, Object> load() {
                return objects;
            }
        });

        applicationContext.getBeansOfType(Initializable.class)
                .values()
                .forEach(Initializable::initialize);
    }
}
