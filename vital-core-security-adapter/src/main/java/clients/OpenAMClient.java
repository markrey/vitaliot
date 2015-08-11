package clients;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import jsonpojos.ActionValues__;
import jsonpojos.ActionValues___;
import jsonpojos.Application;
import jsonpojos.Applications;
import jsonpojos.Authenticate;
import jsonpojos.DecisionRequest;
import jsonpojos.Group;
import jsonpojos.GroupModel;
import jsonpojos.GroupModelWithUsers;
import jsonpojos.Groups;
import jsonpojos.LogoutResponse;
import jsonpojos.Policies;
import jsonpojos.Policy;
import jsonpojos.PolicyAuthenticatedModel;
import jsonpojos.PolicyIdentityModel;
import jsonpojos.Result;
import jsonpojos.SubjectAuthenticated;
import jsonpojos.Subject__;
import jsonpojos.Subject___;
import jsonpojos.User;
import jsonpojos.UserModel;
import jsonpojos.Users;
import jsonpojos.Validation;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.core.net.SmtpManager;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import utils.Action;
import utils.ConfigReader;
import utils.JsonUtils;
import utils.SessionUtils;

public class OpenAMClient {

	private HttpClient httpclient;
	private ConfigReader configReader;
	
	private String idpHost;
	private int idpPort;
	private String snmpPort;
	private String userAdmin;
	private String pwdAdmin;
	private String authToken;
	
	public OpenAMClient() {
		httpclient = HttpClients.createDefault();
		configReader = ConfigReader.getInstance();
		
		idpHost = configReader.get(ConfigReader.IDP_HOST);
		idpPort = Integer.parseInt(configReader.get(ConfigReader.IDP_PORT));
		snmpPort = configReader.get(ConfigReader.SNMP_PORT);
		userAdmin = configReader.get(ConfigReader.USER_ADM);
		pwdAdmin = configReader.get(ConfigReader.PWD_ADM);
		authToken = configReader.get(ConfigReader.AUTH_TOKEN);
		
	}
	
	private boolean isTokenValid() {
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/sessions/"+adminAuthToken)
			.setQuery("_action=validate")
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		HttpPost httppost = new HttpPost(uri);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader(authToken, adminAuthToken);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}    
		}
		
		Validation validation = new Validation();
		
		try {
			validation = (Validation) JsonUtils.deserializeJson(respString, Validation.class);
			if (validation.getValid()) {
				return true;
			}
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	private boolean isTokenValid(String token) {
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/sessions/"+token)
			.setQuery("_action=validate")
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		HttpPost httppost = new HttpPost(uri);
		httppost.setHeader("Content-Type", "application/json");
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}    
		}
		
		Validation validation = new Validation();
		
		try {
			validation = (Validation) JsonUtils.deserializeJson(respString, Validation.class);
			if (validation.getValid()) {
				return true;
			}
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public LogoutResponse logout(String token) {
		
		LogoutResponse resp = new LogoutResponse();
		
		if(!isTokenValid(token)) {
			try {
				resp = (LogoutResponse) JsonUtils.deserializeJson("{\"result\":\"Successfully logged out\"}", LogoutResponse.class);
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
			
			return resp;
		}
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath("/idp/json/sessions")
			.setQuery("_action=logout")
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		HttpPost httppost = new HttpPost(uri);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader(authToken, token);
		
		StringEntity strEntity = new StringEntity("{}", HTTP.UTF_8);
		httppost.setEntity(strEntity);

		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    
		}

		try {
			resp = (LogoutResponse) JsonUtils.deserializeJson(respString, LogoutResponse.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return resp;
		
	}
	
	public Authenticate authenticate(String name, String password) {
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath("/idp/json/authenticate")
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		HttpPost httppost = new HttpPost(uri);
		httppost.setHeader("Content-Type", "application/json");
		if(name != null) {
			httppost.setHeader("X-OpenAM-Username", name);
			httppost.setHeader("X-OpenAM-Password", password);
		} else {
			httppost.setHeader("X-OpenAM-Username", userAdmin);
			httppost.setHeader("X-OpenAM-Password", pwdAdmin);
		}		

		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    
		}
		
		Authenticate auth = new Authenticate();
		try {
			auth = (Authenticate) JsonUtils.deserializeJson(respString, Authenticate.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//this.adminAuthToken = auth.getTokenId();
		if(name == null) {
			SessionUtils.setAdminAuthToken(auth.getTokenId());
		}
		
		return auth;
		
	}
	
	public boolean evaluate(String token, ArrayList<String> resources, StringBuilder goingOn, String tokenUser) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		DecisionRequest req = new DecisionRequest();
		SubjectAuthenticated sub = new SubjectAuthenticated();
		sub.setSsoToken(token);
		
		req.setSubject(sub);
		req.setResources(resources);
		
		String newReq = "";
		
		try {
			newReq = JsonUtils.serializeJson(req);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/policies/")
			.setQuery("_action=evaluate")
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		StringEntity strEntity = new StringEntity(newReq, HTTP.UTF_8);
		strEntity.setContentType("application/json");
		
		HttpPost httppost = new HttpPost(uri);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader(authToken, tokenUser);
		httppost.setEntity(strEntity);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}    
		}

		if (respString.contains("\"code\":")) {
			goingOn.append(respString);
		} else {
			goingOn.append("{ \"responses\" : " + respString + " }");
		}
		
		return true;
		
	}
	
	public String getUserIdFromToken(String userToken) {
		
		boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String uid = "";
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/sessions/"+userToken)
			.setQuery("_action=validate")
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		HttpPost httppost = new HttpPost(uri);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader(authToken, adminAuthToken);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}    
		}
		
		Validation validation = new Validation();
		
		try {
			validation = (Validation) JsonUtils.deserializeJson(respString, Validation.class);
			uid = validation.getUid();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		
		return uid;
	}
	
	public Users getUsers(String token) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/users")
			.setQuery("_queryID=*")
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		HttpGet httppost = new HttpGet(uri);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader(authToken, token);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    
		}
		
		Users users = new Users();
		
		try {
			users = (Users) JsonUtils.deserializeJson(respString, Users.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return users;
	}
	
	public Groups getGroups(String token) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/groups")
			.setQuery("_queryID=*")
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		HttpGet httppost = new HttpGet(uri);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader(authToken, token);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    
		}
		
		Groups groups = new Groups();
		
		try {
			groups = (Groups) JsonUtils.deserializeJson(respString, Groups.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return groups;
		
	}
	
	public Applications getApplications(String token) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/applications")
			.setQuery("_queryFilter=true")
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		HttpGet httppost = new HttpGet(uri);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader(authToken, token);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    
		}
		
		Applications applications = new Applications();
		
		try {
			applications = (Applications) JsonUtils.deserializeJson(respString, Applications.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return applications;
	}
	
	public Policies getPolicies(String token) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/policies")
			.setQuery("_queryID=*")
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		HttpGet httppost = new HttpGet(uri);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader(authToken, token);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    
		}
		
		Policies policies = new Policies();
		
		try {
			policies = (Policies) JsonUtils.deserializeJson(respString, Policies.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return policies;
	}
	
	public String getStatValue(String oidValue) {
		
		String answer = null;

		int snmpVersion  = SnmpConstants.version2c;
		String community = "public";

	    // Create TransportMapping and Listen
	    TransportMapping transport = null;
		try {
			transport = new DefaultUdpTransportMapping();
			transport.listen();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    // Create Target Address object
	    CommunityTarget comtarget = new CommunityTarget();
	    comtarget.setCommunity(new OctetString(community));
	    comtarget.setVersion(snmpVersion);
	    comtarget.setAddress(new UdpAddress(idpHost + "/" + snmpPort));
	    comtarget.setRetries(2);
	    comtarget.setTimeout(1000);

	    // Create the PDU object
	    PDU pdu = new PDU();
	    pdu.add(new VariableBinding(new OID(oidValue)));
	    pdu.setType(PDU.GET);
	    pdu.setRequestID(new Integer32(1));

		// Create Snmp object for sending data to Agent
		Snmp snmp = new Snmp(transport);

		ResponseEvent response = null;
		try {
			response = snmp.get(pdu, comtarget);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Process Agent Response
		if(response != null) {
			PDU responsePDU = response.getResponse();
			if(responsePDU != null) {
				int errorStatus = responsePDU.getErrorStatus();
		        int errorIndex = responsePDU.getErrorIndex();
		        String errorStatusText = responsePDU.getErrorStatusText();

		        if(errorStatus == PDU.noError) {
		        	//System.out.println("Snmp Get Response = " + responsePDU.getVariableBindings());
		        	answer = responsePDU.getVariableBindings().firstElement().toString();
		        	String delims = "[=]";
		        	answer = answer.split(delims)[1].substring(1);
		        }
		        else {
		        	//System.out.println("Error: Request Failed");
		        	//System.out.println("Error Status = " + errorStatus);
		        	//System.out.println("Error Index = " + errorIndex);
		        	//System.out.println("Error Status Text = " + errorStatusText);
		        	answer = "Error: " + errorStatusText;
		        }
			}
		    else {
		    	//System.out.println("Error: Response PDU is null");
		    	answer = "Error: Response PDU is null";
		    }
		}
		else {
			//System.out.println("Error: Agent Timeout... ");
			answer = "Error: Agent Timeout... ";
		}
		try {
			snmp.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return answer;
		    
	}
	
	public User getUser(String username, String token) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/users/"+username)
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		HttpGet httppost = new HttpGet(uri);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader(authToken, token);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    
		}
		
		User user = new User();
		
		try {
			user = (User) JsonUtils.deserializeJson(respString, User.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return user;
	}
	
	public Group getGroup(String groupId, String token) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/groups/"+groupId)
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		HttpGet httppost = new HttpGet(uri);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader(authToken, token);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    
		}
		
		Group group = new Group();
		
		try {
			group = (Group) JsonUtils.deserializeJson(respString, Group.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return group;
	}
	
	public Policy getPolicy(String policyId, String token) {
			
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/policies/"+policyId)
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		HttpGet httppost = new HttpGet(uri);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader(authToken, token);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    
		}
		
		Policy policy = new Policy();
		
		try {
			policy = (Policy) JsonUtils.deserializeJson(respString, Policy.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return policy;
	}
	
	public Application getApplication(String applicationId, String token) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/applications/"+applicationId)
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		HttpGet httppost = new HttpGet(uri);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader(authToken, token);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    
		}
		
		Application application = new Application();
		
		try {
			application = (Application) JsonUtils.deserializeJson(respString, Application.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return application;
	}
	
	public boolean createUser(String givenName, String surname, String username, String password, String mail, StringBuilder goingOn, String token) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		if (getUser(username, token).getUsername()!=null) {
			//utente con stesso nome è già presente
			return false;
		}
		
		UserModel userModel = new UserModel();
		
		userModel.setUsername(username);
		userModel.setUserpassword(password);
		userModel.setMail(mail);
		userModel.setAdditionalProperty("givenName", givenName);
		userModel.setAdditionalProperty("sn", surname);
		
		String newUser = "";
		
		try {
			newUser = JsonUtils.serializeJson(userModel);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/users/")
			.setQuery("_action=create")
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		StringEntity strEntity = new StringEntity(newUser, HTTP.UTF_8);
		strEntity.setContentType("application/json");
		
		HttpPost httppost = new HttpPost(uri);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader(authToken, token);
		httppost.setEntity(strEntity);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}    
		}
		
		goingOn.append(respString);
		
		return true;
	}
	
	
	public boolean createGroup(Group group) {
		
		boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();
		
		GroupModelWithUsers groupModelWithUsers = new GroupModelWithUsers();
		groupModelWithUsers.setUsername(group.getUsername());
		groupModelWithUsers.setUniqueMember(group.getUniqueMember());
		
		String newGroup = "";
		
		try {
			newGroup = JsonUtils.serializeJson(groupModelWithUsers);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/groups/")
			.setQuery("_action=create")
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		StringEntity strEntity = new StringEntity(newGroup, HTTP.UTF_8);
		strEntity.setContentType("application/json");
		
		HttpPost httppost = new HttpPost(uri);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader(authToken, adminAuthToken);
		httppost.setEntity(strEntity);
		
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}    
		}
		
		return true;
	}
	
	
	public boolean createGroup(String groupId, StringBuilder goingOn, String token) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		if (getGroup(groupId, token).getUsername()!=null) {
			//un gruppo con lo stesso id è già presente
			return false;
		}
		
		GroupModel groupModel = new GroupModel();
		groupModel.setUsername(groupId);
		
		String newGroup = "";
		
		try {
			newGroup = JsonUtils.serializeJson(groupModel);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/groups/")
			.setQuery("_action=create")
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		StringEntity strEntity = new StringEntity(newGroup, HTTP.UTF_8);
		strEntity.setContentType("application/json");
		
		HttpPost httppost = new HttpPost(uri);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader(authToken, token);
		httppost.setEntity(strEntity);
		
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}    
		}
		
		goingOn.append(respString);
			
		return true;
	}
	
	public boolean createApplication(String applicationName, String description, ArrayList<String> resources, StringBuilder goingOn, String token) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		if (getApplication(applicationName, token).getName() != null) {
			// application already existing, return false
			return false;
		}
		
		Application application = new Application();
		
		application.setName(applicationName);
		application.setDescription(description);
		application.setResources(resources);
		application.setAdditionalProperty("applicationType", "iPlanetAMWebAgentService");
		application.setAdditionalProperty("entitlementCombiner", "DenyOverride");
		
		List<String> subjects = Arrays.asList("AND", "OR", "NOT", "AuthenticatedUsers", "Identity", "JwtClaim");
		List<String> conditions = Arrays.asList(
				"AND",
		        "OR",
		        "NOT",
		        "AMIdentityMembership",
		        "AuthLevel",
		        "AuthScheme",
		        "AuthenticateToRealm",
		        "AuthenticateToService",
		        "IPv4",
		        "IPv6",
		        "LDAPFilter",
		        "LEAuthLevel",
		        "OAuth2Scope",
		        "ResourceEnvIP",
		        "Session",
		        "SessionProperty",
		        "SimpleTime");
		
		application.setSubjects(subjects);
		application.setConditions(conditions);
				
		String newApplication = "";
		try {
			newApplication = JsonUtils.serializeJson(application);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/applications/")
			.setQuery("_action=create")
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		StringEntity strEntity = new StringEntity(newApplication, HTTP.UTF_8);
		strEntity.setContentType("application/json");
		
		HttpPost httppost = new HttpPost(uri);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader(authToken, token);
		httppost.setEntity(strEntity);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
				if (respString.contains(applicationName)) {
					goingOn.append(respString);
					return true;
				}
			} catch (ParseException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}    
		}
		
		goingOn.append(respString);
			
		return false;
	}
	
	public boolean deleteUser(String username, StringBuilder goingOn, String token) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		String currentUserName = getUser(username, token).getUsername();
		
		if (currentUserName == null) {
			//utente non presente
			return false;
		}
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/users/"+username)
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		HttpDelete httpdelete = new HttpDelete(uri);
		httpdelete.setHeader("Content-Type", "application/json");
		httpdelete.setHeader(authToken, token);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
				if (respString.contains("success")) {
					goingOn.append(respString);
					return true;
				}
			} catch (ParseException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		    
		}
		
		goingOn.append(respString);
		
		return false;
	}
	
	public boolean deleteGroup(String groupId, StringBuilder goingOn, String token) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		String currentGroupName = getGroup(groupId, token).getUsername();
		
		if (currentGroupName == null) {
			return false;
		}
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/groups/"+groupId)
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		HttpDelete httpdelete = new HttpDelete(uri);
		httpdelete.setHeader("Content-Type", "application/json");
		httpdelete.setHeader(authToken, token);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
				if (respString.contains("success")) {
					goingOn.append(respString);
					return true;
				}
			} catch (ParseException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		    
		}
		
		goingOn.append(respString);
		
		return false;
	}
	
	public boolean deletePolicy(String policyId, StringBuilder goingOn, String token) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		String currentPolicyName = getPolicy(policyId, token).getName();
		
		if (currentPolicyName == null) {
			return false;
		}
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/policies/"+policyId)
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		HttpDelete httpdelete = new HttpDelete(uri);
		httpdelete.setHeader("Content-Type", "application/json");
		httpdelete.setHeader(authToken, token);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
				if (respString.equals("{}")) {
					goingOn.append(respString);
					return true;
				}
			} catch (ParseException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		    
		}
		
		goingOn.append(respString);
		
		return false;
	}
	
	public boolean deleteApplication(String applicationId, StringBuilder goingOn, String token) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		String currentApplicationName = getApplication(applicationId, token).getName();
		
		if (currentApplicationName == null) {
			return false;
		}
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/applications/"+applicationId)
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		HttpDelete httpdelete = new HttpDelete(uri);
		httpdelete.setHeader("Content-Type", "application/json");
		httpdelete.setHeader(authToken, token);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpdelete);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
				if (respString.equals("{}")) {
					goingOn.append(respString);
					return true;
				}
			} catch (ParseException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		    
		}
		
		goingOn.append(respString);
		
		return false;
	}
	
	public boolean updateUser(String username, String givenName, String surname, String mail, String status, StringBuilder goingOn, String token) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		UserModel userModel = new UserModel();
		
		userModel.setUsername(null); // to be sure it not included in the JSON (username is used in the URL) 
		userModel.setMail(mail);
		userModel.setAdditionalProperty("givenName", givenName);
		userModel.setAdditionalProperty("sn", surname);
		userModel.setAdditionalProperty("inetUserStatus", status);
		
		String newUserInfo = "";
		
		try {
			newUserInfo = JsonUtils.serializeJson(userModel);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/users/"+username)
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		StringEntity strEntity = new StringEntity(newUserInfo, HTTP.UTF_8);
		strEntity.setContentType("application/json");
		
		HttpPut httpput = new HttpPut(uri);
		httpput.setHeader("Content-Type", "application/json");
		httpput.setHeader(authToken, token);
		httpput.setEntity(strEntity);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpput);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}    
		}
		
		goingOn.append(respString);
		
		return true;
	}
	
	/**
	 * Set policy with subject "AuthenticatedUsers", only authenticated users can access
	 * @param policyName
	 * @param actions
	 * @param resources
	 */
	public boolean createAuthenticatedPolicy(String policyName, ArrayList<Action> actions, ArrayList<String> resources, String token) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		if (getPolicy(policyName, token).getName()!=null) {
			//policy con stesso nome già presente, return false
			return false;
		}
		
		PolicyAuthenticatedModel policyAuthenticatedModel = new PolicyAuthenticatedModel();
		
		policyAuthenticatedModel.setName(policyName);
		policyAuthenticatedModel.setActive(true);
		policyAuthenticatedModel.setDescription(policyName+" created from REST.");
		policyAuthenticatedModel.setResources(resources);
		
		ActionValues__ actVal = new ActionValues__();
		
		for (int i = 0; i<actions.size(); i++) {
			Action currentAction = actions.get(i);
			String strAction = currentAction.getAction();
			
			if (strAction.equals("POST")) {
				actVal.setPOST(currentAction.getState());
			} else if (strAction.equals("PATCH")) {
				actVal.setPATCH(currentAction.getState());
			} else if (strAction.equals("GET")) {
				actVal.setGET(currentAction.getState());
			} else if (strAction.equals("DELETE")) {
				actVal.setDELETE(currentAction.getState());
			} else if (strAction.equals("OPTIONS")) {
				actVal.setOPTIONS(currentAction.getState());
			} else if (strAction.equals("PUT")) {
				actVal.setPUT(currentAction.getState());
			} else if (strAction.equals("HEAD")) {
				actVal.setHEAD(currentAction.getState());
			}
				
		}
		
		policyAuthenticatedModel.setActionValues(actVal);
		
		Subject__ sbj = new Subject__();
		sbj.setType("AuthenticatedUsers");
		
		policyAuthenticatedModel.setSubject(sbj);
				
		String newPolicy = "";
		try {
			newPolicy = JsonUtils.serializeJson(policyAuthenticatedModel);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//policy creata, procedi con la chiamata REST per inserirla in OpenAM
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/policies/")
			.setQuery("_action=create")
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		StringEntity strEntity = new StringEntity(newPolicy, HTTP.UTF_8);
		strEntity.setContentType("application/json");
		
		HttpPost httppost = new HttpPost(uri);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader(authToken, token);
		httppost.setEntity(strEntity);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
				if (respString.contains(policyName)) {
					return true;
				}
			} catch (ParseException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}    
		}
		
		return false;
		
	}
	
	/**
	 * Set policy: only the defined users have access 
	 * @param policyName
	 * @param actions
	 * @param resources
	 * @param users
	 * @return
	 */
	public boolean createIdentityUsersPolicy(String policyName, ArrayList<Action> actions, ArrayList<String> resources, ArrayList<String> users, String token) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		if (getPolicy(policyName, token).getName()!=null) {
			//policy con stesso nome già presente, return false
			return false;
		}
		
		//controllo che gli utenti da inserire nella policy siano tutti presenti, altrimento return false
		//altrimenti popolo l'array con gli universalid
		
		ArrayList<String> usersId = new ArrayList<String>();
		
		for (int i=0; i<users.size();i++) {
			String currentUser = users.get(i);
			if (getUser(currentUser, token).getUsername() == null) {
				return false;
			} else {
				usersId.add(getUser(currentUser, token).getUniversalid().get(0)); 
			}
		}
		
		PolicyIdentityModel policyIdentityModel = new PolicyIdentityModel();
		
		policyIdentityModel.setName(policyName);
		policyIdentityModel.setActive(true);
		policyIdentityModel.setDescription(policyName+" creted from REST.");
		policyIdentityModel.setResources(resources);
		
		ActionValues___ actVal = new ActionValues___();
		
		for (int i = 0; i<actions.size(); i++) {
			Action currentAction = actions.get(i);
			String strAction = currentAction.getAction();
			
			if (strAction.equals("POST")) {
				actVal.setPOST(currentAction.getState());
			} else if (strAction.equals("PATCH")) {
				actVal.setPATCH(currentAction.getState());
			} else if (strAction.equals("GET")) {
				actVal.setGET(currentAction.getState());
			} else if (strAction.equals("DELETE")) {
				actVal.setDELETE(currentAction.getState());
			} else if (strAction.equals("OPTIONS")) {
				actVal.setOPTIONS(currentAction.getState());
			} else if (strAction.equals("PUT")) {
				actVal.setPUT(currentAction.getState());
			} else if (strAction.equals("HEAD")) {
				actVal.setHEAD(currentAction.getState());
			}
				
		}
		
		policyIdentityModel.setActionValues(actVal);
		
		Subject___ sbj = new Subject___();
		sbj.setType("Identity");
		sbj.setSubjectValues(usersId);
		
		policyIdentityModel.setSubject(sbj);
				
		String newPolicy = "";
		try {
			newPolicy = JsonUtils.serializeJson(policyIdentityModel);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//policy creata, procedi con la chiamata REST per inserirla in OpenAM
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/policies/")
			.setQuery("_action=create")
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		StringEntity strEntity = new StringEntity(newPolicy, HTTP.UTF_8);
		strEntity.setContentType("application/json");
		
		HttpPost httppost = new HttpPost(uri);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader(authToken, token);
		httppost.setEntity(strEntity);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
				if (respString.contains(policyName)) {
					return true;
				}
			} catch (ParseException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}    
		}
		
		return false;
	}
	
	public boolean createIdentityGroupsPolicy(String policyName, ArrayList<Action> actions, ArrayList<String> resources, ArrayList<String> groups, String applicationName, StringBuilder goingOn, String token) {
	
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		if (getPolicy(policyName, token).getName()!=null) {
			//policy con stesso nome già presente, return false
			return false;
		}
		
		//controllo che i gruppi da inserire nella policy siano tutti presenti, altrimento return false
		//altrimenti popolo l'array con gli universalid
		
		ArrayList<String> groupsId = new ArrayList<String>();
		
		for (int i=0; i<groups.size();i++) {
			String currentGroup = groups.get(i);
			if (getGroup(currentGroup, token).getUsername() == null) {
				return false;
			} else {
				groupsId.add(getGroup(currentGroup, token).getUniversalid().get(0)); 
			}
		}
		
		PolicyIdentityModel policyIdentityModel = new PolicyIdentityModel();
		
		policyIdentityModel.setName(policyName);
		policyIdentityModel.setActive(true);
		policyIdentityModel.setDescription(policyName+" created from REST.");
		policyIdentityModel.setResources(resources);
		policyIdentityModel.setApplicationName(applicationName);
		
		ActionValues___ actVal = new ActionValues___();
		
		for (int i = 0; i<actions.size(); i++) {
			Action currentAction = actions.get(i);
			String strAction = currentAction.getAction();
			
			if (strAction.equals("POST")) {
				actVal.setPOST(currentAction.getState());
			} else if (strAction.equals("PATCH")) {
				actVal.setPATCH(currentAction.getState());
			} else if (strAction.equals("GET")) {
				actVal.setGET(currentAction.getState());
			} else if (strAction.equals("DELETE")) {
				actVal.setDELETE(currentAction.getState());
			} else if (strAction.equals("OPTIONS")) {
				actVal.setOPTIONS(currentAction.getState());
			} else if (strAction.equals("PUT")) {
				actVal.setPUT(currentAction.getState());
			} else if (strAction.equals("HEAD")) {
				actVal.setHEAD(currentAction.getState());
			}
				
		}
		
		policyIdentityModel.setActionValues(actVal);
		
		Subject___ sbj = new Subject___();
		sbj.setType("Identity");
		sbj.setSubjectValues(groupsId);
		
		policyIdentityModel.setSubject(sbj);
				
		String newPolicy = "";
		try {
			newPolicy = JsonUtils.serializeJson(policyIdentityModel);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//policy creata, procedi con la chiamata REST per inserirla in OpenAM
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/policies/")
			.setQuery("_action=create")
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		StringEntity strEntity = new StringEntity(newPolicy, HTTP.UTF_8);
		strEntity.setContentType("application/json");
		
		HttpPost httppost = new HttpPost(uri);
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader(authToken, token);
		httppost.setEntity(strEntity);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
				if (respString.contains(policyName)) {
					goingOn.append(respString);
					return true;
				}
			} catch (ParseException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}    
		}
		
		goingOn.append(respString);
			
		return false;
	}
	
	public boolean updatePolicyIdentity(String name, String description, Boolean active, ArrayList<String> groups, Boolean nogr, ArrayList<String> resources, Boolean nores, ArrayList<Action> actions, Boolean noact, StringBuilder goingOn, String token) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		PolicyIdentityModel policyModel = new PolicyIdentityModel();
		Subject___ sub = new Subject___();
		policyModel.setName(name); // to be sure it not included in the JSON (name is used in the URL)
		policyModel.setActive(active);
		policyModel.setDescription(description);
		policyModel.setApplicationName(getPolicy(name, token).getApplicationName());
		
		if(resources.isEmpty() && !nores) {
			policyModel.setResources(getPolicy(name, token).getResources());
		}
		else if(!nores) {
			policyModel.setResources(resources);
		}
		
		if(groups.isEmpty() && !nogr) {
			try {
				policyModel.setSubject((Subject___) JsonUtils.deserializeJson(JsonUtils.serializeJson(getPolicy(name, token).getSubject()), sub.getClass()));
			} catch (JsonParseException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (JsonMappingException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		} else {
			ArrayList<String> groupsId = new ArrayList<String>();
			
			for (int i=0; i<groups.size();i++) {
				String currentGroup = groups.get(i);
				if (getGroup(currentGroup, token).getUsername() == null) {
					return false;
				} else {
					groupsId.add(getGroup(currentGroup, token).getUniversalid().get(0)); 
				}
			}
			
			Subject___ sbj = new Subject___();
			sbj.setType("Identity");
			sbj.setSubjectValues(groupsId);
			
			policyModel.setSubject(sbj);
		}
		
		ActionValues___ actVal = new ActionValues___();
		
		if(!actions.isEmpty()) {
			
			for (int i = 0; i<actions.size(); i++) {
				Action currentAction = actions.get(i);
				String strAction = currentAction.getAction();
				
				if (strAction.equals("POST")) {
					actVal.setPOST(currentAction.getState());
				} else if (strAction.equals("PATCH")) {
					actVal.setPATCH(currentAction.getState());
				} else if (strAction.equals("GET")) {
					actVal.setGET(currentAction.getState());
				} else if (strAction.equals("DELETE")) {
					actVal.setDELETE(currentAction.getState());
				} else if (strAction.equals("OPTIONS")) {
					actVal.setOPTIONS(currentAction.getState());
				} else if (strAction.equals("PUT")) {
					actVal.setPUT(currentAction.getState());
				} else if (strAction.equals("HEAD")) {
					actVal.setHEAD(currentAction.getState());
				}
					
			}
			
			policyModel.setActionValues(actVal);
		} else if(!noact) {
			try {
				policyModel.setActionValues((ActionValues___) JsonUtils.deserializeJson(JsonUtils.serializeJson(getPolicy(name, token).getActionValues()), actVal.getClass()));
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
		}
				
		String newPolicyInfo = "";
		
		try {
			newPolicyInfo = JsonUtils.serializeJson(policyModel);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/policies/"+name)
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		StringEntity strEntity = new StringEntity(newPolicyInfo, HTTP.UTF_8);
		strEntity.setContentType("application/json");
		
		HttpPut httpput = new HttpPut(uri);
		httpput.setHeader("Content-Type", "application/json");
		httpput.setHeader(authToken, token);
		httpput.setEntity(strEntity);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpput);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}    
		}
		
		goingOn.append(respString);
		
		return true;
	}
	
	public boolean updatePolicyAuthenticated(String name, String description, Boolean active, ArrayList<String> groups, Boolean nogr, ArrayList<String> resources, Boolean nores, ArrayList<Action> actions, Boolean noact, StringBuilder goingOn, String token) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		PolicyAuthenticatedModel policyModel = new PolicyAuthenticatedModel();
		Subject__ sub = new Subject__();
		policyModel.setName(name); // to be sure it not included in the JSON (name is used in the URL)
		policyModel.setActive(active);
		policyModel.setDescription(description);
		
		if(resources.isEmpty() && !nores) {
			policyModel.setResources(getPolicy(name, token).getResources());
		}
		else if(!nores) {
			policyModel.setResources(resources);
		}
		
		if(groups.isEmpty() && !nogr) {
			try {
				policyModel.setSubject((Subject__) JsonUtils.deserializeJson(JsonUtils.serializeJson(getPolicy(name, token).getSubject()), sub.getClass()));
			} catch (JsonParseException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (JsonMappingException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		} else {
			ArrayList<String> groupsId = new ArrayList<String>();
			
			for (int i=0; i<groups.size();i++) {
				String currentGroup = groups.get(i);
				if (getGroup(currentGroup, token).getUsername() == null) {
					return false;
				} else {
					groupsId.add(getGroup(currentGroup, token).getUniversalid().get(0)); 
				}
			}
			
			Subject__ sbj = new Subject__();
			sbj.setType("AuthenticatedUsers");
			
			policyModel.setSubject(sbj);
		}
		
		ActionValues__ actVal = new ActionValues__();
		
		if(!actions.isEmpty()) {
			
			for (int i = 0; i<actions.size(); i++) {
				Action currentAction = actions.get(i);
				String strAction = currentAction.getAction();
				
				if (strAction.equals("POST")) {
					actVal.setPOST(currentAction.getState());
				} else if (strAction.equals("PATCH")) {
					actVal.setPATCH(currentAction.getState());
				} else if (strAction.equals("GET")) {
					actVal.setGET(currentAction.getState());
				} else if (strAction.equals("DELETE")) {
					actVal.setDELETE(currentAction.getState());
				} else if (strAction.equals("OPTIONS")) {
					actVal.setOPTIONS(currentAction.getState());
				} else if (strAction.equals("PUT")) {
					actVal.setPUT(currentAction.getState());
				} else if (strAction.equals("HEAD")) {
					actVal.setHEAD(currentAction.getState());
				}
					
			}
			
			policyModel.setActionValues(actVal);
		} else  if(!noact) {
			try {
				policyModel.setActionValues((ActionValues__) JsonUtils.deserializeJson(JsonUtils.serializeJson(getPolicy(name, token).getActionValues()), actVal.getClass()));
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
		}
		
		String newPolicyInfo = "";
		
		try {
			newPolicyInfo = JsonUtils.serializeJson(policyModel);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/policies/"+name)
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		StringEntity strEntity = new StringEntity(newPolicyInfo, HTTP.UTF_8);
		strEntity.setContentType("application/json");
		
		HttpPut httpput = new HttpPut(uri);
		httpput.setHeader("Content-Type", "application/json");
		httpput.setHeader(authToken, token);
		httpput.setEntity(strEntity);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpput);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}    
		}
		
		goingOn.append(respString);
		
		return true;
	}
	
	public boolean updateGroup(String groupId, GroupModelWithUsers groupInfo, StringBuilder goingOn, String token) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		String newGroup = "";
		
		try {
			newGroup = JsonUtils.serializeJson(groupInfo);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/groups/"+groupId)
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		StringEntity strEntity = new StringEntity(newGroup, HTTP.UTF_8);
		strEntity.setContentType("application/json");
		
		HttpPut httpput = new HttpPut(uri);
		httpput.setHeader("Content-Type", "application/json");
		httpput.setHeader(authToken, token);
		httpput.setEntity(strEntity);
		
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpput);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}    
		}
		
		goingOn.append(respString);
		
		return true;
		
	}
	
	public boolean updateApplication(String applicationName, String description, ArrayList<String> resources, Boolean nores, StringBuilder goingOn, String token) {
		
		/*boolean currentSessionIsValid = isTokenValid();
		
		if (!currentSessionIsValid) {
			authenticate(null, null);
		}
		
		String adminAuthToken = SessionUtils.getAdminAuhtToken();*/
		
		Application application = new Application();
		
		application.setName(applicationName);
		application.setDescription(description);
		if(resources.isEmpty() && !nores) {
			application.setResources(getApplication(applicationName, token).getResources());
		}
		else if(!nores) {
			application.setResources(resources);
		}
		application.setAdditionalProperty("applicationType", "iPlanetAMWebAgentService");
		application.setAdditionalProperty("entitlementCombiner", "DenyOverride");
		
		List<String> subjects = Arrays.asList("AND", "OR", "NOT", "AuthenticatedUsers", "Identity", "JwtClaim");
		List<String> conditions = Arrays.asList(
				"AND",
		        "OR",
		        "NOT",
		        "AMIdentityMembership",
		        "AuthLevel",
		        "AuthScheme",
		        "AuthenticateToRealm",
		        "AuthenticateToService",
		        "IPv4",
		        "IPv6",
		        "LDAPFilter",
		        "LEAuthLevel",
		        "OAuth2Scope",
		        "ResourceEnvIP",
		        "Session",
		        "SessionProperty",
		        "SimpleTime");
		
		application.setSubjects(subjects);
		application.setConditions(conditions);
				
		String newApplicationInfo = "";
		
		try {
			newApplicationInfo = JsonUtils.serializeJson(application);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		URI uri = null;
		try {
			uri = new URIBuilder()
			.setScheme("http")
			.setHost(idpHost)
			.setPort(idpPort)
			.setPath(" /idp/json/applications/"+applicationName)
			.build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		StringEntity strEntity = new StringEntity(newApplicationInfo, HTTP.UTF_8);
		strEntity.setContentType("application/json");
		
		HttpPut httpput = new HttpPut(uri);
		httpput.setHeader("Content-Type", "application/json");
		httpput.setHeader(authToken, token);
		httpput.setEntity(strEntity);
		
		//Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpput);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		String respString = "";
		
		if (entity != null) {
		    
			try {
				respString = EntityUtils.toString(entity);
			} catch (ParseException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}    
		}
		
		goingOn.append(respString);
		
		return true;
	}
	
	public boolean addUsersToGroup(String groupId, ArrayList<String> users, StringBuilder goingOn, String token) {
				
		Group currentGroup = getGroup(groupId, token);
		
		if (currentGroup.getUsername() == null) {
			//il gruppo non esiste
			return false;
		}
		
		
		List<String> currentUsers = currentGroup.getUniqueMember(); //utenti al momento presenti nel gruppo
		ArrayList<String> usersId = new ArrayList<String>(); //uid degli utenti da aggiungere
		
		
		for (int i=0; i < users.size(); i++) {
			String currentUser = users.get(i);
			if (getUser(currentUser, token).getUsername() == null) {
				//uno degli utenti richiesti non esiste, esci
				return false;
			} else {
				usersId.add(getUser(currentUser, token).getDn().get(0)); 
			}
		}
		
		//se già ci sono utenti nel gruppo,
		//controllare possibili duplicati, e nel caso non aggiungerli (li cancello dalla lista passata in input)
		if (currentUsers.size() > 0) {
			for (int i=0; i<usersId.size();i++) {
				String auxId = usersId.get(i);
				if (currentUsers.contains(auxId)) {
					usersId.remove(i);
				}
			}
		}
		
		for(int i = 0; i < usersId.size(); i++) {
			currentUsers.add(usersId.get(i));
		}
		
		GroupModelWithUsers newGroupInfo = new GroupModelWithUsers();
		
		newGroupInfo.setUniqueMember(currentUsers);
		
		return updateGroup(groupId, newGroupInfo, goingOn, token);
		
	}
	
	public boolean deleteUsersFromGroup(String groupId, ArrayList<String> users, StringBuilder goingOn, String token) {
		
		Group currentGroup = getGroup(groupId, token);
		
		if (currentGroup.getUsername() == null) {
			//il gruppo non esiste
			return false;
		}
		
		List<String> currentUsers = currentGroup.getUniqueMember(); //utenti al momento presenti nel gruppo
		ArrayList<String> usersId = new ArrayList<String>(); //uid degli utenti da aggiungere
		
		
		for (int i=0; i<users.size();i++) {
			String currentUser = users.get(i);
			if (getUser(currentUser, token).getUsername() != null) {
				usersId.add(getUser(currentUser, token).getDn().get(0));
			}
		}
		
		if (currentUsers.size() > 0) {
			for (int i=0;i<usersId.size();i++){
				if (currentUsers.contains(usersId.get(i))) {
					currentUsers.remove(usersId.get(i));
				}
			}
		} else {
			//non ci sono utenti da eliminare!
			return true;
		}
		
		GroupModelWithUsers newGroupInfo = new GroupModelWithUsers();
		
		newGroupInfo.setUniqueMember(currentUsers);
		
		return updateGroup(groupId, newGroupInfo, goingOn, token);
		
	}
	
	public boolean userIsInGroup(String userId, String groupId, String token) {
		
		User currentUser = getUser(userId, token);
		Group currentGroup = getGroup(groupId, token);
		
		if (currentUser.getUsername() == null) {
			//l'utente non esiste
			return false;
		}
		if (currentGroup.getUsername() == null) {
			//il gruppo non esiste
			return false;
		}
		
		List<String> groupUsers = currentGroup.getUniqueMember();
		String currentUserId = currentUser.getDn().get(0);
		
		if (groupUsers.contains(currentUserId)) {
			return true;
		}
		
		return false;
	}
	
	public Groups listUserGroups(String userId, String token) {
		
		Groups groups = getGroups(token);
		List<String> list = groups.getResult();
		Iterator<String> iter = list.listIterator();
		while (iter.hasNext()) {
			String group = iter.next();
			if(!userIsInGroup(userId, group, token)) {
				iter.remove();
				groups.setResultCount(groups.getResultCount()-1);
			}
		}
		groups.setResult(list);
		
		return groups;
	}
	
	public Policies listApplicationPolicies(String appName, String token) {
		
		Policies policies = getPolicies(token);
		List<Result> list = policies.getResult();
		Iterator<Result> iter = list.listIterator();
		while (iter.hasNext()) {
			Result policy = iter.next();
			if(!policy.getApplicationName().equals(appName)) {
				iter.remove();
				policies.setResultCount(policies.getResultCount()-1);
			}
		}
		policies.setResult(list);
		
		return policies;
	}
	
}

