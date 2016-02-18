package core;

import static spark.Spark.get;

import java.util.Timer;
import java.util.TimerTask;

import api.VitalObservation;
import api.VitalSensor;
import api.VitalService;
import api.VitalSystem;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import util.DMSUtils;
import util.DMSPermission;

public class DMS {

	final static int responseSuccess = 200;
	final static int responseUnauthorize = 401;
	final static int responseBadServer = 500;

	final static boolean isSecurityEnabled = true;

	static DMSPermission DP;
	static Timer timer;

	public static void main(String[] args) {

		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				System.out.println("Re-authenticating DMS...");
				DMSPermission.securityDMSAuth(); // Temporary blocked for
													// testing.
			}
		}, 20 * 60 * 1000, 60 * 1000);

		Spark.get(new Route("/") {

			@Override
			public Object handle(Request request, Response response) {
				DMSPermission.securityDMSAuth();// Temporary blocked for
												// testing.
				return "Welcome to DMS.";
			}
		});

		Spark.post(new Route("/insertSystem") {

			@Override
			public Object handle(Request request, Response response) {
				DBObject objRet = new BasicDBObject();
				response.type("application/json");
				try {
					String inputData = request.body().trim();
					VitalSystem.insertSystem(inputData);
					objRet.put("status", "success");
					response.status(responseSuccess);
					return objRet;

				} catch (Exception e) {
					response.status(responseBadServer);
					return DMSUtils.sendException(response, e);
				}
			}
		});

		Spark.post(new Route("/insertService") {

			@Override
			public Object handle(Request request, Response response) {
				DBObject objRet = new BasicDBObject();
				response.type("application/json");
				try {
					String inputData = request.body().trim();
					VitalService.insertService(inputData);
					objRet.put("status", "success");
					response.status(responseSuccess);
					return objRet;

				} catch (Exception e) {
					response.status(responseBadServer);
					return DMSUtils.sendException(response, e);
				}
			}
		});

		Spark.post(new Route("/insertSensor") {

			@Override
			public Object handle(Request request, Response response) {
				DBObject objRet = new BasicDBObject();
				response.type("application/json");
				try {
					String inputData = request.body().trim();
					VitalSensor.insertSensor(inputData);
					objRet.put("status", "success");
					response.status(responseSuccess);
					return objRet;

				} catch (Exception e) {
					response.status(responseBadServer);
					return DMSUtils.sendException(response, e);
				}
			}
		});

		Spark.post(new Route("/insertObservation") {

			@Override
			public Object handle(Request request, Response response) {
				DBObject objRet = new BasicDBObject();
				response.type("application/json");
				try {
					String inputData = request.body().trim();
					VitalObservation.insertObservation(inputData);
					objRet.put("status", "success");
					response.status(responseSuccess);
					return objRet;

				} catch (Exception e) {
					response.status(responseBadServer);
					return DMSUtils.sendException(response, e);
				}
			}
		});

		Spark.post(new Route("/querySystem") {

			@Override
			public Object handle(Request request, Response response) {
				DBObject query = DMSUtils.encodeKeys((DBObject) JSON
						.parse(request.body().trim()));
				try {
					response.type("application/json+ld");
					if (isSecurityEnabled) {

						int code = DMSPermission.checkPermission(request);
						if (code == DMSPermission.successfulPermission) {

							DBObject perm = DMSUtils.encodeKeys(DP
									.getPermission());

							DBObject filteredQuery = DMSPermission
									.permissionFilter(perm, query);
							// System.out.println("Query: " + filteredQuery);
							response.status(responseSuccess);
							return VitalSystem.querySystem(filteredQuery);

						} else if (code == DMSPermission.accessTokenNotFound) {
							response.status(responseUnauthorize);
							return DMSUtils
									.sendError(
											"Unauthorized. vitalAccessToken Not Found.",
											401);
						} else if (code == DMSPermission.unsuccessful) {
							response.status(responseUnauthorize);
							return DMSUtils
									.sendError(
											"Unauthorized. Permission Denied. Please re-authorize vitalAccessToken.",
											401);

						} else {
							response.status(responseBadServer);
							return DMSUtils.sendError("Internal Server error.",
									500);
						}
					} else {
						response.status(responseSuccess);
						return VitalSystem.querySystem(query);
					}

				} catch (Exception e) {
					response.status(responseBadServer);
					return DMSUtils.sendException(response, e);
				}

			}

		});

	}
}
