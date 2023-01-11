package io.mosip.ivv.e2e.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import io.mosip.admin.fw.util.AdminTestException;
import io.mosip.admin.fw.util.AdminTestUtil;
import io.mosip.admin.fw.util.TestCaseDTO;
import io.mosip.authentication.fw.precon.JsonPrecondtion;
import io.mosip.authentication.fw.util.AuthenticationTestException;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.e2e.constant.E2EConstants;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.TestRunner;
import io.mosip.testscripts.BioAuth;
import io.mosip.testscripts.IdpBioAuth;
import io.restassured.response.Response;

public class BioIdpAuthentication extends BaseTestCaseUtil implements StepInterface {
	static Logger logger = Logger.getLogger(BioIdpAuthentication.class);
	private static final String AuthenticateUser = "idaData/BioAuthIdp/BioAuthIdp.yml";
	Properties deviceProp = null;
	Properties uinResidentDataPathFinalProps = new Properties();
	IdpBioAuth idpBioAuth = new IdpBioAuth();
	String bioResponse = null;

	@Override
	public void run() throws RigInternalError {

		String deviceInfoFilePath = null;
		String transactionId = "";
		String uins = null;
		List<String> uinList = null;
		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("Parameter is  missing from DSL step");
			throw new RigInternalError("Modality paramter is  missing in step: " + step.getName());
		} else {
			deviceInfoFilePath = step.getParameters().get(0);
			if (!StringUtils.isBlank(deviceInfoFilePath)) {
				deviceInfoFilePath = TestRunner.getExternalResourcePath()
						+ props.getProperty("ivv.path.deviceinfo.folder") + deviceInfoFilePath + ".properties";
				deviceProp = AdminTestUtil.getproperty(deviceInfoFilePath);
			} else
				throw new RigInternalError("deviceInfo file path Parameter is  missing from DSL step");
		}
		if (step.getParameters().size() == 3) {
			uins = step.getParameters().get(1);
			if (!StringUtils.isBlank(uins))
				uinList = new ArrayList<>(Arrays.asList(uins.split("@@")));
		} else if (step.getParameters().size() == 4) { // e2e_bioAuthentication(faceDevice,$$uin,$$personaFilePath)
			uins = step.getParameters().get(1);
			String _personaFilePath = step.getParameters().get(2);
			if (uins.startsWith("$$") && _personaFilePath.startsWith("$$")) {
				uins = step.getScenario().getVariables().get(uins);
				_personaFilePath = step.getScenario().getVariables().get(_personaFilePath);
				uinList = new ArrayList<>();
				uinList.add(uins);
				uinPersonaProp.put(uins, _personaFilePath);
			}
		} else
			uinList = new ArrayList<>(uinPersonaProp.stringPropertyNames());

		if (step.getParameters() == null || step.getParameters().isEmpty() || step.getParameters().size() < 1) {
			logger.error("transactionId parameter is  missing from DSL step");
			throw new RigInternalError("transactionId paramter is  missing in step: " + step.getName());
		} else {
			transactionId = (String) oidcClientProp.get("transactionId");
			System.out.println(transactionId);

		}

		for (String uin : uinList) {
			String personFilePathvalue = null;
			if (uinPersonaProp.containsKey(uin))
				personFilePathvalue = uinPersonaProp.getProperty(uin);
			else
				throw new RigInternalError("Persona doesn't exist for the given UIN " + uin);

			String bioType = null, bioSubType = null;
			List<String> modalityList = new ArrayList<>();
			String modalityToLog = null;
			String modalityKeyTogetBioValue = null;
			if (deviceProp != null) {
				bioType = deviceProp.getProperty("bioType");
				bioSubType = deviceProp.getProperty("bioSubType");
				switch (bioType) {
				case E2EConstants.FACEBIOTYPE:
					modalityList.add(E2EConstants.FACEFETCH);
					modalityToLog = bioType;
					modalityKeyTogetBioValue = E2EConstants.FACEFETCH;
					break;
				case E2EConstants.IRISBIOTYPE:
					modalityList.add(E2EConstants.IRISFETCH);
					modalityToLog = bioSubType + "_" + bioType;
					modalityKeyTogetBioValue = (bioSubType.equalsIgnoreCase("left")) ? E2EConstants.LEFT_EYE
							: E2EConstants.RIGHT_EYE;
					break;
				case E2EConstants.FINGERBIOTYPE:
					modalityList.add(E2EConstants.FINGERFETCH);
					modalityToLog = bioSubType;
					modalityKeyTogetBioValue = bioSubType;
					break;
				default:
					throw new RigInternalError("Given BIO Type in device property file is not valid");
				}
			}

			bioResponse = packetUtility.retrieveBiometric(personFilePathvalue, modalityList);

			System.out.println("bioMetricValue= " + bioResponse);

			String fileName = AuthenticateUser;
			idpBioAuth.isInternal = false;
			Object[] casesList = idpBioAuth.getYmlTestData(fileName);

			if (bioResponse != null && !bioResponse.isEmpty() && modalityKeyTogetBioValue != null) {
				String bioValue = JsonPrecondtion.getValueFromJson(bioResponse, modalityKeyTogetBioValue);

				if (bioValue == null || bioValue.length() < 100)
					throw new RigInternalError(
							"Not able to get the bio value for field " + modalityToLog + " from persona");
				for (Object object : casesList) {
					TestCaseDTO test = (TestCaseDTO) object;
					packetUtility.idpBioAuth(modalityToLog, bioValue, uin, transactionId, deviceProp, test, idpBioAuth);
				}
			}
		}
	}

}