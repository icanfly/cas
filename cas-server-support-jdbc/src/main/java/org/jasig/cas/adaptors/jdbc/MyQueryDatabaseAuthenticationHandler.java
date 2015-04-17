package org.jasig.cas.adaptors.jdbc;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;
import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * @author luopeng
 *         Created on 2015/4/17.
 */
public class MyQueryDatabaseAuthenticationHandler extends AbstractJdbcUsernamePasswordAuthenticationHandler {

    @NotNull
    private String sql;

    @NotNull
    private String sqlPasswordField = "password";

    @NotNull
    private String sqlStatusField = "status";

    @NotNull
    private String sqlDeletedField = "is_deleted";

    /**
     * {@inheritDoc}
     */
    @Override
    protected final HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential)
            throws GeneralSecurityException, PreventedException {

        final String username = credential.getUsername();
        final String encryptedPassword = this.getPasswordEncoder().encode(credential.getPassword());
        try {
            final Map<String,Object> resultMap = getJdbcTemplate().queryForMap(this.sql,username);

            //if account has been deleted
            if (resultMap.isEmpty() || StringUtils.equals((String)resultMap.get(sqlDeletedField),"T")) {
                throw new AccountNotFoundException("Account has been deleted:"+username);
            }

            //if account has been locked
            if (!StringUtils.equals((String)resultMap.get(sqlStatusField),"VALID")) {
                throw new AccountLockedException("Account has been locked.");
            }

            //if password field not match
            if (!StringUtils.equals((String)resultMap.get(sqlPasswordField),encryptedPassword)) {
                throw new FailedLoginException("Password does not match value on record.");
            }

        } catch (final IncorrectResultSizeDataAccessException e) {
            if (e.getActualSize() == 0) {
                throw new AccountNotFoundException(username + " not found with SQL query");
            } else {
                throw new FailedLoginException("Multiple records found for " + username);
            }
        } catch (final DataAccessException e) {
            throw new PreventedException("SQL exception while executing query for " + username, e);
        }
        return createHandlerResult(credential, this.principalFactory.createPrincipal(username), null);
    }

    /**
     * @param sql The sql to set.
     */
    public void setSql(final String sql) {
        this.sql = sql;
    }

    /**
     * @param sqlPasswordField Specify the fields of query result for password field
     */
    public void setSqlPasswordField(String sqlPasswordField) {
        this.sqlPasswordField = sqlPasswordField;
    }

    /**
     *
     * @param sqlStatusField Specify the fields of query result for status field
     */
    public void setSqlStatusField(String sqlStatusField) {
        this.sqlStatusField = sqlStatusField;
    }

    /**
     *
     * @param sqlDeletedField Specify the fields of query result for is_deleted field
     */
    public void setSqlDeletedField(String sqlDeletedField) {
        this.sqlDeletedField = sqlDeletedField;
    }
}
