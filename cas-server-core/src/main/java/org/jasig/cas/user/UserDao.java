package org.jasig.cas.user;

/**
 * @author luopeng
 *         Created on 2015/4/10.
 */
public interface UserDao {

    boolean updatePasswd(String newpass,String username,String oldpass);

}
