package securitywrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import jsonpojos.Group;
import jsonpojos.Groups;
import jsonpojos.Policies;
import jsonpojos.Policy;
import jsonpojos.User;
import jsonpojos.Users;
import jsonpojos.Monitor;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import utils.JsonUtils;
import clients.OpenAMClient;

@Path("/rest")
public class GetServices {
	
	private OpenAMClient client;
	
	public GetServices() {
		client = new OpenAMClient();
	}
	
	@Path("/user/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser(@PathParam("id") String userId) {
		
		User user;
		String answer;
		int code;
		
		answer = null;
		code = 0;
		user = client.getUser(userId);
		
		try {
			answer = JsonUtils.serializeJson(user);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(user.getAdditionalProperties().containsKey("code")) {
			if(user.getAdditionalProperties().get("code").getClass() == Integer.class) {
				code = (Integer) user.getAdditionalProperties().get("code");
			}
		}
		if(code >= 400 && code < 500) {
			return Response.status(Status.BAD_REQUEST)
				.entity(answer)
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.build();
		}
		else if(code >= 500 && code < 600) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(answer)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.build();
		}
		else {
			return Response.ok()
					.entity(answer)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.build();
		}
		
	}
	
	@Path("/user/{id}/groups")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserGroups(@PathParam("id") String userId) {
		
		String answer;
		
		answer = null;
		
		Groups groups = client.listUserGroups(userId);
		
		try {
			answer = JsonUtils.serializeJson(groups);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return Response.ok()
				.entity(answer)
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.build();
		
	}
	
	@Path("/group/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGroup(@PathParam("id") String groupId)  {
		
		Group group;
		String answer;
		int code;
		
		answer = null;
		code = 0;
		group = client.getGroup(groupId);
		
		try {
			answer = JsonUtils.serializeJson(group);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(group.getAdditionalProperties().containsKey("code")) {
			if(group.getAdditionalProperties().get("code").getClass() == Integer.class) {
				code = (Integer) group.getAdditionalProperties().get("code");
			}
		}
		if(code >= 400 && code < 500) {
			return Response.status(Status.BAD_REQUEST)
				.entity(answer)
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.build();
		}
		else if(code >= 500 && code < 600) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(answer)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.build();
		}
		else {
			return Response.ok()
					.entity(answer)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.build();
		}
		
	}
	
	@Path("/policy/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPolicy(@PathParam("id") String policyId) {
		
		Policy policy;
		String answer;
		int code;
		
		answer = null;
		code = 0;
		policy = client.getPolicy(policyId);
		
		try {
			answer = JsonUtils.serializeJson(policy);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(policy.getAdditionalProperties().containsKey("code")) {
			if(policy.getAdditionalProperties().get("code").getClass() == Integer.class) {
				code = (Integer) policy.getAdditionalProperties().get("code");
			}
		}
		if(code >= 400 && code < 500) {
			return Response.status(Status.BAD_REQUEST)
				.entity(answer)
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.build();
		}
		else if(code >= 500 && code < 600) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(answer)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.build();
		}
		else {
			return Response.ok()
					.entity(answer)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.build();
		}
		
	}
	
	@Path("/users")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUsers() {
		
		Users users;
		String answer;
		int code;
		
		answer = null;
		code = 0;
		users = client.getUsers();
		
		try {
			answer = JsonUtils.serializeJson(users);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(users.getAdditionalProperties().containsKey("code")) {
			if(users.getAdditionalProperties().get("code").getClass() == Integer.class) {
				code = (Integer) users.getAdditionalProperties().get("code");
			}
		}
		if(code >= 400 && code < 500) {
			return Response.status(Status.BAD_REQUEST)
				.entity(answer)
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.build();
		}
		else if(code >= 500 && code < 600) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(answer)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.build();
		}
		else {
			return Response.ok()
					.entity(answer)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.build();
		}
		
	}
	
	@Path("/groups")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGroups() {
		
		Groups groups;
		String answer;
		int code;
		
		answer = null;
		code = 0;
		groups = client.getGroups();
		
		try {
			answer = JsonUtils.serializeJson(groups);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(groups.getAdditionalProperties().containsKey("code")) {
			if(groups.getAdditionalProperties().get("code").getClass() == Integer.class) {
				code = (Integer) groups.getAdditionalProperties().get("code");
			}
		}
		if(code >= 400 && code < 500) {
			return Response.status(Status.BAD_REQUEST)
				.entity(answer)
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.build();
		}
		else if(code >= 500 && code < 600) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(answer)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.build();
		}
		else {
			return Response.ok()
					.entity(answer)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.build();
		}
		
	}
	
	@Path("/policies")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPolicies() {
		
		Policies policies;
		String answer;
		int code;
		
		answer = null;
		code = 0;
		policies = client.getPolicies();
		
		try {
			answer = JsonUtils.serializeJson(policies);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(policies.getAdditionalProperties().containsKey("code")) {
			if(policies.getAdditionalProperties().get("code").getClass() == Integer.class) {
				code = (Integer) policies.getAdditionalProperties().get("code");
			}
		}
		if(code >= 400 && code < 500) {
			return Response.status(Status.BAD_REQUEST)
				.entity(answer)
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.build();
		}
		else if(code >= 500 && code < 600) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(answer)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.build();
		}
		else {
			return Response.ok()
					.entity(answer)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.build();
		}
		
	}
	
	@Path("/stats")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSessions() {
		
		Monitor values = new Monitor();
		String answer, value;
		String oidValue;
		Boolean ok;
		
		answer = value = null;
		ok = true;
		
		
		if(ok) {
			// Active Sessions
			oidValue = ".1.3.6.1.4.1.42.2.230.3.1.1.2.1.11.1.0";
			value = client.getStatValue(oidValue);
			if(value.startsWith("Error")) {
				ok = false;
			}
			else {
				values.setActiveSessions(Long.parseLong(value));
			}
		}
		
		if(ok) {
			// Current Internal Sessions
			oidValue = ".1.3.6.1.4.1.36733.1.2.1.1.1.0";
			value = client.getStatValue(oidValue);
			if(value.startsWith("Error")) {
				ok = false;
			}
			else {
				values.setCurrInternalSessions(Long.parseLong(value));
			}
		}
		
		if(ok) {
			// Current Remote Sessions
			oidValue = ".1.3.6.1.4.1.36733.1.2.1.2.1.0";
			value = client.getStatValue(oidValue);
			if(value.startsWith("Error")) {
				ok = false;
			}
			else {
				values.setCurrRemoteSessions(Long.parseLong(value));
			}
		}

		if(ok) {
			// Cumulative Policy Evaluations (specific resource)
			oidValue = ".1.3.6.1.4.1.36733.1.2.2.1.1.1.0";
			value = client.getStatValue(oidValue);
			if(value.startsWith("Error")) {
				ok = false;
			}
			else {
				values.setCumPolicyEval(Long.parseLong(value));
			}
		}
		
		if(ok) {
			// Average rate of policy evaluations for specific resources
			oidValue = ".1.3.6.1.4.1.36733.1.2.2.1.1.2.0";
			value = client.getStatValue(oidValue);
			if(value.startsWith("Error")) {
				ok = false;
			}
			else {
				values.setAvgPolicyEval(Long.parseLong(value));
			}
		}
		
		if(ok) {
			// Average rate of policy evaluations for a tree of resources (subtree)
			oidValue = ".1.3.6.1.4.1.36733.1.2.2.1.2.1.0";
			value = client.getStatValue(oidValue);
			if(value.startsWith("Error")) {
				ok = false;
			}
			else {
				values.setAvgPolicyEvalTree(Long.parseLong(value));
			}
		}
		
		if(ok) {
			try {
				answer = JsonUtils.serializeJson(values);
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return Response.ok()
					.entity(answer)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.build();
		}
		else {
			answer = value.substring(7);
			return Response.status(Status.BAD_REQUEST)
					.entity(answer)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.build();
		}
		
	}
		
}
