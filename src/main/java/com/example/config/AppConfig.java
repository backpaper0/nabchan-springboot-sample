package com.example.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.util.ReflectionUtils;

import com.example.form.Domain;

import nablarch.common.handler.DbConnectionManagementHandler;
import nablarch.common.handler.TransactionManagementHandler;
import nablarch.common.web.session.SessionManager;
import nablarch.common.web.session.SessionStore;
import nablarch.common.web.session.SessionStoreHandler;
import nablarch.common.web.session.store.DbStore;
import nablarch.core.cache.BasicStaticDataCache;
import nablarch.core.cache.StaticDataCache;
import nablarch.core.date.BasicSystemTimeProvider;
import nablarch.core.date.SystemTimeProvider;
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
import nablarch.core.validation.FormValidationDefinition;
import nablarch.core.validation.FormValidationDefinitionLoader;
import nablarch.core.validation.ValidationManager;
import nablarch.core.validation.convertor.StringConvertor;
import nablarch.core.validation.domain.DomainValidationHelper;
import nablarch.core.validation.domain.DomainValidator;
import nablarch.core.validation.validator.LengthValidator;
import nablarch.core.validation.validator.RequiredValidator;
import nablarch.fw.Handler;
import nablarch.fw.handler.GlobalErrorHandler;
import nablarch.fw.handler.RequestPathJavaPackageMapping;
import nablarch.fw.web.handler.HttpCharacterEncodingHandler;
import nablarch.fw.web.handler.HttpResponseHandler;
import nablarch.fw.web.handler.SecureHandler;
import nablarch.fw.web.servlet.WebFrontController;

@Configuration
public class AppConfig implements ApplicationContextAware, InitializingBean {

    private final DataSource dataSource;
    private final RequestPathJavaPackageMapping mapping;

    private ApplicationContext applicationContext;

    public AppConfig(final DataSource dataSource,
            final RequestPathJavaPackageMapping mapping) {
        this.dataSource = dataSource;
        this.mapping = mapping;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public FilterRegistrationBean<WebFrontController> webFrontController() {
        final WebFrontController wfc = new WebFrontController();
        final Collection<Handler<?, ?>> handlers = new ArrayList<>();
        handlers.add(new HttpCharacterEncodingHandler());
        handlers.add(new GlobalErrorHandler());
        handlers.add(new SecureHandler());
        handlers.add(new HttpResponseHandler());
        handlers.add(sessionStoreHandler());
        handlers.add(dbConnectionManagementHandler());
        handlers.add(transactionManagementHandler());
        handlers.add(mapping);
        wfc.setHandlerQueue(handlers);

        final FilterRegistrationBean<WebFrontController> bean = new FilterRegistrationBean<>();
        bean.setFilter(wfc);
        bean.setUrlPatterns(Collections.singleton("/action/*"));
        return bean;
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

    @Bean
    public SystemTimeProvider systemTimeProvider() {
        return new BasicSystemTimeProvider();
    }

    @Bean
    @ConfigurationProperties(prefix = "nabchan.validation.required")
    public RequiredValidator requiredValidator() {
        return new RequiredValidator();
    }

    @Bean
    @ConfigurationProperties(prefix = "nabchan.validation.length")
    public LengthValidator lengthValidator() {
        return new LengthValidator();
    }

    @Bean
    public DomainValidationHelper domainValidationHelper() {
        final DomainValidationHelper helper = new DomainValidationHelper();
        helper.setDomainAnnotation(Domain.class.getName());

        //WORKAROUND
        //setDomainAnnotation内部でClass.forNameをしているが
        //devtoolsのRestartClassLoaderではなくアプリケーションのクラスローダーが
        //使用されてしまい、異なる2つのClassインスタンスが出来てしまう。
        //その影響で正しくバリデーションが行えないため、
        //2つの異なるClassインスタンスが出来てしまった場合は
        //リフレクションで強制的にセットし直す。
        if (helper.getDomainAnnotation() != Domain.class) {
            final Field field = ReflectionUtils.findField(DomainValidationHelper.class,
                    "domainAnnotation");
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, helper, Domain.class);
        }
        return helper;
    }

    @Bean
    public DomainValidator domainValidator() {
        final DomainValidator validator = new DomainValidator();
        validator.setDomainValidationHelper(domainValidationHelper());
        validator.setValidators(Arrays.asList(
                requiredValidator(),
                lengthValidator()));
        return validator;
    }

    @Bean
    public StaticDataCache<FormValidationDefinition> formDefinitionCache() {
        final BasicStaticDataCache<FormValidationDefinition> formDefinitionCache = new BasicStaticDataCache<>();
        formDefinitionCache.setLoader(new FormValidationDefinitionLoader());
        return formDefinitionCache;
    }

    @Bean
    public StringConvertor stringConvertor() {
        final StringConvertor converter = new StringConvertor();
        converter.setExtendedStringConvertors(Collections.emptyList());
        return converter;
    }

    @Bean
    public ValidationManager validationManager() {
        final ValidationManager vm = new ValidationManager();
        vm.setValidators(Arrays.asList(
                requiredValidator(),
                lengthValidator(),
                domainValidator()));
        vm.setConvertors(Arrays.asList(
                stringConvertor()));
        vm.setDomainValidationHelper(domainValidationHelper());
        vm.setFormDefinitionCache(formDefinitionCache());
        return vm;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SystemRepository.load(new ObjectLoader() {
            @Override
            public Map<String, Object> load() {
                return applicationContext.getBeansOfType(Object.class);
            }
        });

        applicationContext.getBeansOfType(Initializable.class)
                .values()
                .forEach(Initializable::initialize);
    }
}
