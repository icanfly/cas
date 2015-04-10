<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<jsp:directive.include file="../includes/top.jsp" />

<%--<c:if test="${not pageContext.request.secure}">
    <div id="msg" class="errors">
        <h2><spring:message code="screen.nonsecure.title" /></h2>
        <p><spring:message code="screen.nonsecure.message" /></p>
    </div>
</c:if>--%>

<div id="cookiesDisabled" class="errors" style="display:none;">
    <h2><spring:message code="screen.cookies.disabled.title" /></h2>
    <p><spring:message code="screen.cookies.disabled.message" /></p>
</div>

<%--<c:if test="${not empty registeredService}">
    <c:set var="registeredServiceLogo" value="images/webapp.png"/>
    <c:if test="${not empty registeredService.logo}">
        <c:set var="registeredServiceLogo" value="${registeredService.logo}"/>
    </c:if>

    <div id="serviceui" class="serviceinfo">
        <table>
            <tr>
                <td><img src="${registeredServiceLogo}"></td>
                <td id="servicedesc">
                    <h1>${fn:escapeXml(registeredService.name)}</h1>
                    <p>${fn:escapeXml(registeredService.description)}</p>
                </td>
            </tr>
        </table>
    </div>
    <p/>
</c:if>--%>

<div class="box" id="login">
    <form method="post" id="fm1" action="${request.contextPath}/mgt/user/update_passwd">

        <c:if test="${code != null}">
            <c:choose>
                <c:when test="${code eq 'ok'}">
                    <div id="msg" class="success" style="background-color: rgb(221, 255, 170);">密码修改成功</div>
                </c:when>
                <c:when test="${code eq 'failure'}">
                    <div id="msg" class="errors" class="errors" style="background-color: rgb(255, 238, 221);">密码修改失败</div>
                </c:when>
                <c:when test="${code eq 'confirm_passwd_mismatch'}">
                    <div id="msg" class="errors" class="errors" style="background-color: rgb(255, 238, 221);">两次密码输入不一致</div>
                </c:when>
                <c:when test="${code eq 'passwd_miss'}">
                    <div id="msg" class="errors" class="errors" style="background-color: rgb(255, 238, 221);">原密码不正确</div>
                </c:when>
                <c:when test="${code eq 'captcha_error'}">
                    <div id="msg" class="errors" class="errors" style="background-color: rgb(255, 238, 221);">验证码不正确</div>
                </c:when>
            </c:choose>
        </c:if>


        <h3>请进行密码修改.</h3>

        <section class="row">
            <label for="oldPasswd">旧密码:</label>
            <input type="password" id="oldPasswd" name="oldPasswd" />
        </section>

        <section class="row">
            <label for="newPasswd">新密码:</label>
            <input type="password" id="newPasswd" name="newPasswd" />
        </section>

        <section class="row">
            <label for="confirmNewPasswd">确认新密码:</label>
            <input type="password" id="confirmNewPasswd" name="confirmNewPasswd" />
        </section>

        <section class="row">
            <label for="captcha"><spring:message code="screen.welcome.label.captcha"/></label>
            <input type="text" id="captcha" size="6" name="captcha" autocomplete="off"/>
            <img src="${request.contextPath}/captcha.jpg" style="vertical-align:middle;"
                 onclick="this.src='${request.contextPath}/captcha.jpg?rnd=' + Math.random();"/>
        </section>

        <section class="row btn-row">
            <input class="btn-submit" name="submit" accesskey="l" value="提交" tabindex="4" type="submit" />
            <input class="btn-reset" name="reset" accesskey="c" value="重置" tabindex="5" type="reset" />
            <a href="${request.contextPath}/logout">退出登录</a>
        </section>
    </form>
</div>

<div id="sidebar">
    <div class="sidebar-content">
        <p><spring:message code="screen.welcome.security" /></p>

        <div id="list-languages">
            <%final String queryString = request.getQueryString() == null ? "" : request.getQueryString().replaceAll("&locale=([A-Za-z][A-Za-z]_)?[A-Za-z][A-Za-z]|^locale=([A-Za-z][A-Za-z]_)?[A-Za-z][A-Za-z]", "");%>
            <c:set var='query' value='<%=queryString%>' />
            <c:set var="xquery" value="${fn:escapeXml(query)}" />

            <h3>Languages:</h3>

            <c:choose>
                <c:when test="${not empty requestScope['isMobile'] and not empty mobileCss}">
                    <form method="get" action="login?${xquery}">
                        <select name="locale">
                            <option value="en">English</option>
                            <option value="es">Spanish</option>
                            <option value="fr">French</option>
                            <option value="ru">Russian</option>
                            <option value="nl">Nederlands</option>
                            <option value="sv">Svenska</option>
                            <option value="it">Italiano</option>
                            <option value="ur">Urdu</option>
                            <option value="zh_CN">Chinese (Simplified)</option>
                            <option value="zh_TW">Chinese (Traditional)</option>
                            <option value="de">Deutsch</option>
                            <option value="ja">Japanese</option>
                            <option value="hr">Croatian</option>
                            <option value="cs">Czech</option>
                            <option value="sl">Slovenian</option>
                            <option value="pl">Polish</option>
                            <option value="ca">Catalan</option>
                            <option value="mk">Macedonian</option>
                            <option value="fa">Farsi</option>
                            <option value="ar">Arabic</option>
                            <option value="pt_PT">Portuguese</option>
                            <option value="pt_BR">Portuguese (Brazil)</option>
                        </select>
                        <input type="submit" value="Switch">
                    </form>
                </c:when>
                <c:otherwise>
                    <c:set var="loginUrl" value="login?${xquery}${not empty xquery ? '&' : ''}locale=" />
                    <ul>
                        <li class="first"><a href="${loginUrl}en">English</a></li>
                        <li><a href="${loginUrl}es">Spanish</a></li>
                        <li><a href="${loginUrl}fr">French</a></li>
                        <li><a href="${loginUrl}ru">Russian</a></li>
                        <li><a href="${loginUrl}nl">Nederlands</a></li>
                        <li><a href="${loginUrl}sv">Svenska</a></li>
                        <li><a href="${loginUrl}it">Italiano</a></li>
                        <li><a href="${loginUrl}ur">Urdu</a></li>
                        <li><a href="${loginUrl}zh_CN">Chinese (Simplified)</a></li>
                        <li><a href="${loginUrl}zh_TW">Chinese (Traditional)</a></li>
                        <li><a href="${loginUrl}de">Deutsch</a></li>
                        <li><a href="${loginUrl}ja">Japanese</a></li>
                        <li><a href="${loginUrl}hr">Croatian</a></li>
                        <li><a href="${loginUrl}cs">Czech</a></li>
                        <li><a href="${loginUrl}sl">Slovenian</a></li>
                        <li><a href="${loginUrl}ca">Catalan</a></li>
                        <li><a href="${loginUrl}mk">Macedonian</a></li>
                        <li><a href="${loginUrl}fa">Farsi</a></li>
                        <li><a href="${loginUrl}ar">Arabic</a></li>
                        <li><a href="${loginUrl}pt_PT">Portuguese</a></li>
                        <li><a href="${loginUrl}pt_BR">Portuguese (Brazil)</a></li>
                        <li class="last"><a href="${loginUrl}pl">Polish</a></li>
                    </ul>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<jsp:directive.include file="../includes/bottom.jsp" />
