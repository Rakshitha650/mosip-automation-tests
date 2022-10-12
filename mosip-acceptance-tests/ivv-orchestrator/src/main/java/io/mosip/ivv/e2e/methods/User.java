package io.mosip.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.testng.Reporter;

import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.CenterHelper;
import io.mosip.ivv.orchestrator.UserHelper;
import io.mosip.kernel.util.KeycloakUserManager;
import io.mosip.service.BaseTestCase;
import io.mosip.testscripts.DeleteWithParam;
import io.mosip.testscripts.GetWithParam;
import io.mosip.testscripts.GetWithQueryParam;
import io.mosip.testscripts.PatchWithPathParam;
import io.mosip.testscripts.PutWithPathParam;
import io.mosip.testscripts.SimplePost;
import io.mosip.testscripts.SimplePut;
import io.restassured.response.Response;

public class User extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(User.class);
	private static final String PostCenter = "ivv_masterdata/RegistrationCenter/CreateRegistrationCenter.yml";
	
	UserHelper userHelper=new UserHelper();
	
	//GetWithParam
	@Override
	public void run() throws RigInternalError {
		String id = null;
		String user=null;
		String pwd=null;
		String calltype = null;
		int centerNum=0;
		
		HashMap<String, String> map =new HashMap<String, String>();

		HashMap<String, String> map2=new HashMap<String, String>();
		
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Method Type[POST/GET/PUT/PATCH] parameter is  missing from DSL step");
			throw new RigInternalError("Method Type[POST/GET/PUT/PATCH] parameter is  missing from DSL step: " + step.getName());
		} else {
			calltype = step.getParameters().get(0); 

		}
		if(step.getParameters().size() >= 2) { 
			 user = step.getParameters().get(1);
			 if(user.contains("@@")){
				 	String userDetails[]=user.split("@@");
				 	user=userDetails[0];
				 	pwd=userDetails[1];
				 	}
			 if (user.startsWith("$$")) {
				 map = step.getScenario().getVariables();
				 user = map.get("userid");

				}
		}		
		if(step.getParameters().size() >= 3) { 
			//here holding activate deeacrivate flag 
			 id = step.getParameters().get(2);
			 if (id.startsWith("$$")) {
				 map2 = step.getScenario().getVariables();
				 map.putAll(map2);
				 map.put("userid",user);
				 map.put("userpassword", pwd);
				}
					}	
		
		switch (calltype) {
		case "DELETE_CENTERMAPPING":
			
			if (step.getOutVarName() != null)
				step.getScenario().getVariables().putAll(map);
			userHelper.deleteCenterMapping(user);
			
			Reporter.log(map.toString(), true);
			break;
		case "DELETE_ZONEMAPPING":
			userHelper.deleteZoneMapping(user,map);
			break;
		case "CREATE_CENTERMAPPING":
			centerNum=Integer.parseInt(id);
			userHelper.createCenterMapping(user,map,centerNum);
			break;

		case "CREATE_ZONEMAPPING":
			userHelper.createZoneMapping(map,user);
			break;
		case "ACTIVATE_CENTERMAPPING":
			userHelper.activateCenterMapping(user,id);
			break;
		case "ACTIVATE_ZONEMAPPING":
			userHelper.activateZoneMapping(user,id);
			break;
		case "CREATE_ZONESEARCH":
			map=userHelper.createZoneSearch(user,map);
			step.getScenario().getVariables().putAll(map);
			Reporter.log(map.toString(), true);
			break;
		case "ADD_User":
			HashMap<String, List<String>> attrmap=new HashMap<String, List<String>>();
			List<String> list=new ArrayList<String>();
			String val=map.get("$$uin")!=null ?map.get("$$uin") : "11000000";
			list.add(val);
			attrmap.put("individualid",list);
			KeycloakUserManager.removeUser(user);
			KeycloakUserManager.createUsers(user, pwd,"roles", attrmap);
			BaseTestCase.mapUserToZone(user,"CSB");
			BaseTestCase.mapZone(user);
			HashMap<String, String> userdetails=new HashMap<String, String>();
			userdetails.put("user", user);
			userdetails.put("pwd",pwd);
			step.getScenario().getVariables().putAll(userdetails);
			
		
		break;
		case "REMOVE_User":
			
			KeycloakUserManager.createUsers();
		
			break;
		}

	}


}

