package azkaban.restli;

import java.util.UUID;
import javax.servlet.ServletException;
import org.apache.log4j.Logger;

import azkaban.restli.user.User;
import azkaban.user.UserManager;
import azkaban.user.UserManagerException;
import azkaban.webapp.AzkabanWebServer;
import azkaban.webapp.session.Session;

import com.linkedin.restli.server.annotations.Action;
import com.linkedin.restli.server.annotations.ActionParam;
import com.linkedin.restli.server.annotations.RestLiActions;
import com.linkedin.restli.server.resources.ResourceContextHolder;


@RestLiActions(name = "user", namespace = "azkaban.restli")
public class UserManagerResource extends ResourceContextHolder {
	private static final Logger logger = Logger.getLogger(UserManagerResource.class);
	
	public AzkabanWebServer getAzkaban() {
		return AzkabanWebServer.getInstance();
	}
	
	@Action(name = "login")
	public String login(
			@ActionParam("username") String username,
			@ActionParam("password") String password)
			throws UserManagerException, ServletException {
		String ip = (String)this.getContext().getRawRequestContext().getLocalAttr("REMOTE_ADDR");
		logger.info("Attempting to login for " + username + " from ip '" + ip + "'");
		
		Session session = createSession(username, password, ip);
		
		logger.info("Session id " + session.getSessionId() + " created for user '" + username + "' and ip " + ip);
		return session.getSessionId();
	}

	@Action(name = "getUserFromSessionId")
	public User getUserFromSessionId(@ActionParam("sessionId") String sessionId) {
		String ip = (String)this.getContext().getRawRequestContext().getLocalAttr("REMOTE_ADDR");
		Session session = getSessionFromSessionId(sessionId, ip);
		azkaban.user.User azUser = session.getUser();

		// Fill out the restli object with properties from the Azkaban user
		User user = new User();
		user.setUserId(azUser.getUserId());
		user.setEmail(azUser.getEmail());
		return user;
	}

	private Session createSession(String username, String password, String ip)
			throws UserManagerException, ServletException {
		UserManager manager = getAzkaban().getUserManager();
		azkaban.user.User user = manager.getUser(username, password);

		String randomUID = UUID.randomUUID().toString();
		Session session = new Session(randomUID, user, ip);
		getAzkaban().getSessionCache().addSession(session);
		
		return session;
	}

	private Session getSessionFromSessionId(String sessionId, String remoteIp) {
		if (sessionId == null) {
			return null;
		}

		Session session = getAzkaban().getSessionCache().getSession(sessionId);
		// Check if the IP's are equal. If not, we invalidate the sesson.
		if (session == null || !remoteIp.equals(session.getIp())) {
			return null;
		}

		return session;
	}
}