package com.yangc.shiro.auth;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.web.filter.authc.AuthenticationFilter;

import com.yangc.bean.ResultBean;
import com.yangc.utils.json.JsonUtils;

public class MyAuthenticationFilter extends AuthenticationFilter {

	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
		boolean isAuthenticated = this.getSubject(request, response).isAuthenticated();
		boolean isLoginRequest = this.isLoginRequest(request, response) || this.pathsMatch("/", request);
		if (!isAuthenticated && isLoginRequest) {
			return true;
		} else if (isAuthenticated && !isLoginRequest) {
			return true;
		}
		return false;
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		boolean isAuthenticated = this.getSubject(request, response).isAuthenticated();
		if (isAuthenticated) {
			this.issueSuccessRedirect(request, response);
		} else {
			HttpServletRequest req = (HttpServletRequest) request;
			HttpServletResponse resp = (HttpServletResponse) response;
			String header = req.getHeader("X-Requested-With");
			// 异步
			if (StringUtils.isNotBlank(header) && header.equals("X-Requested-With")) {
				resp.setContentType("application/json;charset=UTF-8");
				PrintWriter pw = resp.getWriter();
				pw.write(JsonUtils.toJson(new ResultBean(false, "页面超时, 请刷新页面!")));
				pw.flush();
				pw.close();
			}
			// 同步
			else {
				this.redirectToLogin(request, response);
			}
		}
		return false;
	}

}
