package org.jasig.cas.user;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.handler.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @author luopeng
 *         Created on 2015/4/10.
 */
public class UserDaoImpl implements UserDao {

    private DataSource dataSource;

    private PasswordEncoder passwordEncoder;

    private String sql;

    private JdbcTemplate jdbcTemplate;

    private Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public boolean updatePasswd(String newpass,String username, String oldpass) {

        if(StringUtils.isBlank(sql)){
            logger.error("sql must be not null !");
            return false;
        }

        int affectedRows = this.jdbcTemplate.update(sql,passwordEncoder.encode(newpass),username,passwordEncoder.encode(oldpass));

        return affectedRows > 0;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
}
