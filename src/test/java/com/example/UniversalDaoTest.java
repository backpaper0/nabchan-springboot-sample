package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.persistence.Entity;

import org.junit.jupiter.api.Test;

import nablarch.common.dao.UniversalDao;
import nablarch.core.db.DbExecutionContext;
import nablarch.core.db.connection.BasicDbConnection;
import nablarch.core.db.connection.DbConnectionContext;
import nablarch.core.db.dialect.H2Dialect;
import nablarch.core.db.statement.BasicSqlLoader;
import nablarch.core.db.statement.BasicSqlParameterParserFactory;
import nablarch.core.db.statement.BasicStatementFactory;

class UniversalDaoTest {

    @Test
    void test() throws Exception {
        try (Connection con0 = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
                "sa",
                "secret")) {

            try (PreparedStatement pst = con0.prepareStatement(
                    "CREATE TABLE hoge (id IDENTITY, foo VARCHAR(100), bar VARCHAR(100), baz INT)")) {
                pst.execute();
            }

            final BasicDbConnection con = new BasicDbConnection(con0);
            final DbExecutionContext context = new DbExecutionContext(con, new H2Dialect(), "test");
            con.setContext(context);
            final BasicStatementFactory factory = new BasicStatementFactory();
            final BasicSqlLoader sqlLoader = new BasicSqlLoader();
            factory.setSqlLoader(sqlLoader);
            final BasicSqlParameterParserFactory sqlParameterParserFactory = new BasicSqlParameterParserFactory();
            factory.setSqlParameterParserFactory(sqlParameterParserFactory);
            con.setFactory(factory);
            DbConnectionContext.setConnection(con);
            UniversalDao.findAllBySqlFile(Hoge.class, "hoge", new Hoge(null, null, 0));
        }
    }

    @Entity
    public static class Hoge {

        private String foo;
        private String bar;
        private Integer baz;

        public Hoge() {
        }

        public Hoge(final String foo, final String bar, final Integer baz) {
            this.foo = foo;
            this.bar = bar;
            this.baz = baz;
        }

        public String getFoo() {
            return foo;
        }

        public void setFoo(final String foo) {
            this.foo = foo;
        }

        public String getBar() {
            return bar;
        }

        public void setBar(final String bar) {
            this.bar = bar;
        }

        public Integer getBaz() {
            return baz;
        }

        public void setBaz(final Integer baz) {
            this.baz = baz;
        }
    }
}
