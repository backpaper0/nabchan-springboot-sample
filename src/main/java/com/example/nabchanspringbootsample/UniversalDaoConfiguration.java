package com.example.nabchanspringbootsample;

import java.sql.SQLException;
import java.util.Collections;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.web.context.annotation.RequestScope;

import nablarch.common.dao.BasicDaoContextFactory;
import nablarch.common.dao.DaoContextFactory;
import nablarch.common.dao.StandardSqlBuilder;
import nablarch.core.db.DbExecutionContext;
import nablarch.core.db.connection.BasicDbConnection;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.db.dialect.Dialect;
import nablarch.core.db.dialect.H2Dialect;
import nablarch.core.db.statement.BasicStatementFactory;
import nablarch.core.repository.SystemRepository;

@Configuration
public class UniversalDaoConfiguration implements InitializingBean {

    private final TransactionAwareDataSourceProxy dataSource;

    public UniversalDaoConfiguration(final DataSource dataSource) {
        this.dataSource = new TransactionAwareDataSourceProxy(dataSource);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SystemRepository.load(() -> {
            final DaoContextFactory factory = daoContextFactory();
            return Collections.singletonMap("daoContextFactory", factory);
        });
    }

    @Bean
    @RequestScope
    public DaoContextFactory daoContextFactory() {
        try {
            final BasicDaoContextFactory factory = new BasicDaoContextFactory();
            final BasicDbConnection con = new BasicDbConnection(dataSource.getConnection());
            con.setContext(new DbExecutionContext(con, dialect(), "dataSource"));
            con.setFactory(new BasicStatementFactory());
            DbConnectionContext.setConnection(con);
            factory.setDbConnection(con);
            factory.setSqlBuilder(new StandardSqlBuilder());
            return factory;
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @ConditionalOnMissingBean(Dialect.class)
    @Bean
    public Dialect dialect() {
        return new H2Dialect();
    }
}
