package org.mosip.dataprovider;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
//import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
//import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.mosip.dataprovider.mds.MDSClient;
import org.mosip.dataprovider.mds.MDSClientInterface;
import org.mosip.dataprovider.mds.MDSClientNoMDS;
import org.mosip.dataprovider.models.BioModality;
//import org.apache.commons.io.IOUtils;
//import org.apache.commons.lang3.tuple.Pair;
import org.mosip.dataprovider.models.BiometricDataModel;
import org.mosip.dataprovider.models.IrisDataModel;
import org.mosip.dataprovider.models.MosipIDSchema;
import org.mosip.dataprovider.models.ResidentModel;
import org.mosip.dataprovider.models.mds.MDSDevice;
import org.mosip.dataprovider.models.mds.MDSDeviceCaptureModel;
import org.mosip.dataprovider.models.mds.MDSRCaptureModel;
import org.mosip.dataprovider.preparation.MosipMasterData;
import org.mosip.dataprovider.util.CommonUtil;
import org.mosip.dataprovider.util.DataProviderConstants;
import org.mosip.dataprovider.util.FPClassDistribution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jamesmurty.utils.XMLBuilder;
//import java.util.Date;

import ch.qos.logback.classic.Logger;
import io.mosip.mock.sbi.test.CentralizedMockSBI;
import variables.VariableManager;



public class BiometricDataProvider {


	
	static String buildBirIris(String irisInfo, String irisName,String jtwSign,String payload,String qualityScore) throws ParserConfigurationException, FactoryConfigurationError, TransformerException, FileNotFoundException {
		String today = CommonUtil.getUTCDateTime(null);
		//irisInfo= Base64.getEncoder().encodeToString(irisInfo.getBytes());
		XMLBuilder builder = XMLBuilder.create("BIR")
				.a("xmlns", "http://standards.iso.org/iso-iec/19785/-3/ed-2/")
				.e("Version").e("Major").t("1").up().e("Minor").t("1").up().up()
				.e("CBEFFVersion").e("Major").t("1").up().e("Minor").t("1").up().up()
				.e("BIRInfo").e("Integrity").t("false").up().up()
				.e("BDBInfo").e("Format").e("Organization").t("Mosip").up().e("Type").t("9").up().up()
					.e("CreationDate").t(today).up().e("Type").t("Iris").up()
					.e("Subtype").t(irisName).up().e("Level").t("Raw").up().e("Purpose").t("Enroll").up()
					.e("Quality").e("Algorithm").e("Organization").t("HMAC").up().e("Type").t("SHA-256").up().up().e("Score").t(qualityScore).up()
				.up().up()
				.e("BDB").t(irisInfo).up().up();
		if(jtwSign!=null && payload!=null ) {
			jtwSign= Base64.getEncoder().encodeToString(jtwSign.getBytes());
			builder.e("SB").t(jtwSign).up().
			//e("others").e("Key").t("EXCEPTION").up().e("Value").t("false").up().up().
			//e("others").e("Key").t("RETRIES").up().e("Value").t("1").up().up().
			//e("others").e("Key").t("SDK_SCORE").up().e("Value").t("0.0").up().up().
			//e("others").e("Key").t("FORCE_CAPTURED").up().e("Value").t("false").up().up().
			//e("others").e("Key").t("PAYLOAD").up().e("Value").t(payload).up().up().
			//e("others").e("Key").t("SPEC_VERSION").up().e("Value").t("0.9.5").up().up().

			e("others").e("entry").a("key", "EXCEPTION").t("false").up().
			e("entry").a("key", "RETRIES").t("1").up().
			e("entry").a("key", "SDK_SCORE").t("0.0").up().
			e("entry").a("key", "FORCE_CAPTURED").t("false").up().
			e("entry").a("key", "PAYLOAD").t(payload).up().
			e("entry").a("key", "SPEC_VERSION").t("0.9.5").up().up();
		}
					
//		PrintWriter writer = new PrintWriter(new FileOutputStream("cbeffout-Iris"+irisName+".xml"));
//		builder.toWriter(true, writer, null);
				
				
		return builder.asString(null);
	}

	static String buildBirFinger(String fingerInfo, String fingerName,String jtwSign,String payload,String qualityScore) throws ParserConfigurationException, FactoryConfigurationError, TransformerException, FileNotFoundException {
		String today = CommonUtil.getUTCDateTime(null);
		//fingerInfo= Base64.getEncoder().encodeToString(fingerInfo.getBytes());
		XMLBuilder builder = XMLBuilder.create("BIR")
				.a("xmlns", "http://standards.iso.org/iso-iec/19785/-3/ed-2/")
				.e("Version").e("Major").t("1").up().e("Minor").t("1").up().up()
				.e("CBEFFVersion").e("Major").t("1").up().e("Minor").t("1").up().up()
				.e("BIRInfo").e("Integrity").t("false").up().up()
				.e("BDBInfo").e("Format").e("Organization").t("Mosip").up().e("Type").t("7").up().up()
					.e("CreationDate").t(today).up().e("Type").t("Finger").up()
					.e("Subtype").t(fingerName).up().e("Level").t("Raw").up().e("Purpose").t("Enroll").up()
					.e("Quality").e("Algorithm").e("Organization").t("HMAC").up().e("Type").t("SHA-256").up().up().e("Score").t(qualityScore).up()
				.up().up()
				.e("BDB").t(fingerInfo).up().up();
		if(jtwSign!=null && payload!=null ) {
			jtwSign= Base64.getEncoder().encodeToString(jtwSign.getBytes());
			builder.e("SB").t(jtwSign).up().
//			e("others").e("Key").t("EXCEPTION").up().e("Value").t("false").up().up().
//			e("others").e("Key").t("RETRIES").up().e("Value").t("1").up().up().
//			e("others").e("Key").t("SDK_SCORE").up().e("Value").t("0.0").up().up().
//			e("others").e("Key").t("FORCE_CAPTURED").up().e("Value").t("false").up().up().
//			e("others").e("Key").t("PAYLOAD").up().e("Value").t(payload).up().up().
//			e("others").e("Key").t("SPEC_VERSION").up().e("Value").t("0.9.5").up().up();

			e("others").e("entry").a("key", "EXCEPTION").t("false").up().
			e("entry").a("key", "RETRIES").t("1").up().
			e("entry").a("key", "SDK_SCORE").t("0.0").up().
			e("entry").a("key", "FORCE_CAPTURED").t("false").up().
			e("entry").a("key", "PAYLOAD").t(payload).up().
			e("entry").a("key", "SPEC_VERSION").t("0.9.5").up().up();
		}
					
//		PrintWriter writer = new PrintWriter(new FileOutputStream("cbeffout-finger"+ fingerName+ ".xml"));
//		builder.toWriter(true, writer, null);
				
				
		return builder.asString(null);
	}
	static String buildBirFace(String faceInfo,String jtwSign,String payload,String qualityScore) throws ParserConfigurationException, FactoryConfigurationError, TransformerException, FileNotFoundException {
		String today = CommonUtil.getUTCDateTime(null);
		//faceInfo= Base64.getEncoder().encodeToString(faceInfo.getBytes());
		XMLBuilder builder = XMLBuilder.create("BIR")
				.a("xmlns", "http://standards.iso.org/iso-iec/19785/-3/ed-2/")
				.e("Version").e("Major").t("1").up().e("Minor").t("1").up().up()
				.e("CBEFFVersion").e("Major").t("1").up().e("Minor").t("1").up().up()
				.e("BIRInfo").e("Integrity").t("false").up().up()
				.e("BDBInfo").e("Format").e("Organization").t("Mosip").up().e("Type").t("8").up().up()
					.e("CreationDate").t(today).up().e("Type").t("Face").up()
					.e("Subtype").t("").up().e("Level").t("Raw").up().e("Purpose").t("Enroll").up()
					.e("Quality").e("Algorithm").e("Organization").t("HMAC").up().e("Type").t("SHA-256").up().up().e("Score").t(qualityScore).up()
				.up().up()
				.e("BDB").t(faceInfo).up().up();
		if(jtwSign!=null && payload!=null ) {
			jtwSign= Base64.getEncoder().encodeToString(jtwSign.getBytes());
			builder.e("SB").t(jtwSign).up().
			
//			<Others>
//			<Key>EXCEPTION</Key>
//			<Value>false</Value>
//			</Others>
//			e("others").e("Key").t("EXCEPTION").up().e("Value").t("false").up().up().
//			e("others").e("Key").t("RETRIES").up().e("Value").t("1").up().up().
//			e("others").e("Key").t("SDK_SCORE").up().e("Value").t("0.0").up().up().
//			e("others").e("Key").t("FORCE_CAPTURED").up().e("Value").t("false").up().up().
//			e("others").e("Key").t("PAYLOAD").up().e("Value").t(payload).up().up().
//			e("others").e("Key").t("SPEC_VERSION").up().e("Value").t("0.9.5").up().up();
			

			e("others").e("entry").a("key", "EXCEPTION").t("false").up().
			e("entry").a("key", "RETRIES").t("1").up().
			e("entry").a("key", "SDK_SCORE").t("0.0").up().
			e("entry").a("key", "FORCE_CAPTURED").t("false").up().
			e("entry").a("key", "PAYLOAD").t(payload).up().
			e("entry").a("key", "SPEC_VERSION").t("0.9.5").up().up();
			
			
			
			
			
		}
//			PrintWriter writer = new PrintWriter(new
//		 FileOutputStream("cbeffout-face"+".xml")); builder.toWriter(true, writer,
//		  null);
		 
				
				
		return builder.asString(null);
	}
	public static List<BioModality> getModalitiesByType(List<BioModality> bioExceptions, String type){
		List<BioModality> lst = new ArrayList<BioModality>();
		
		for(BioModality m: bioExceptions) {
			if(m.getType().equalsIgnoreCase(type)) {
				lst.add(m);
			}
		}
		return lst;
	}
	public static MDSRCaptureModel regenBiometricViaMDS(ResidentModel resident, String contextKey) throws Exception {
	
		BiometricDataModel biodata = resident.getBiometric();
		MDSClientInterface mds = null;
		String val;
		boolean bNoMDS = true;
		String mdsprofilePath = null;
		String profileName = null;
		
		val =  VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"mdsbypass").toString();
		if(val == null || val.equals("") || val.equals("false")) {
	
			val =  VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"mdsport").toString();
			int port = Integer.parseInt(val);
			mdsprofilePath = VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"mdsprofilepath").toString();
		
		//	port = (port ==0 ? 4501: port);
			
			String p12path =  VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"mosip.test.mockmds.p12.path").toString(); 
			
		port= CentralizedMockSBI.startSBI(contextKey, "Registration",  "Biometric Device",Paths.get(p12path, contextKey).toString()) ;
		//CentralizedMockSBI.stopSBI(context);
			mds =new MDSClient(port);
			profileName = "res"+ resident.getId();
			mds.createProfile(mdsprofilePath, profileName , resident);
			mds.setProfile(profileName);
			//mds.setProfile("Default");
				
		}
		else {
			mds = new MDSClientNoMDS();
			bNoMDS = false;
			profileName = "res"+ resident.getId();
			mds.createProfile(mdsprofilePath, profileName , resident);
			mds.setProfile(profileName);
		}

		MDSRCaptureModel capture = null;

		List<String> filteredAttribs = resident.getFilteredBioAttribtures();
		List<BioModality> bioExceptions = resident.getBioExceptions();
		
		if((filteredAttribs != null && filteredAttribs.contains("face")) && biodata.getRawFaceData() != null) {

			List<BioModality> faceExceptions = null;
			if(bioExceptions != null && !bioExceptions.isEmpty())
				faceExceptions = getModalitiesByType(bioExceptions, "Face");
			if(faceExceptions == null || faceExceptions.isEmpty() ) {

				List<MDSDevice> faceDevices = mds.getRegDeviceInfo(DataProviderConstants.MDS_DEVICE_TYPE_FACE);
				MDSDevice faceDevice = faceDevices.get(0);
				// client.captureFromRegDevice(d.get(0),r, "Face",null,60,1,1);
				
				capture =  mds.captureFromRegDevice(faceDevice,capture ,DataProviderConstants.MDS_DEVICE_TYPE_FACE,
						null, 60, faceDevice.getDeviceSubId().get(0));
			}
		}
		if( biodata.getIris() != null) {
			List<BioModality> irisExceptions = null;
			if(bioExceptions != null && !bioExceptions.isEmpty())
				irisExceptions = getModalitiesByType(bioExceptions, "Iris");
			List<MDSDevice> irisDevices = mds.getRegDeviceInfo(DataProviderConstants.MDS_DEVICE_TYPE_IRIS);
			MDSDevice irisDevice = irisDevices.get(0);
			
			if(irisExceptions == null || irisExceptions.isEmpty() ) {
				if(filteredAttribs != null && filteredAttribs.contains("leftEye")) {
					capture =  mds.captureFromRegDevice(irisDevice,capture ,DataProviderConstants.MDS_DEVICE_TYPE_IRIS,
							null, 60, irisDevice.getDeviceSubId().get(0));
				}
			
				if(irisDevice.getDeviceSubId().size() > 1) {
					if(filteredAttribs != null && filteredAttribs.contains("rightEye")) {
					
						capture =  mds.captureFromRegDevice(irisDevice,capture ,DataProviderConstants.MDS_DEVICE_TYPE_IRIS,
								null, 60, irisDevice.getDeviceSubId().get(1));
					}
				}
			}
			else {
				String [] irisSubTypes = new String[ irisExceptions.size()];
				int i=0;
				for(BioModality bm: irisExceptions) {
					irisSubTypes[i] = bm.getSubType();
					i++;
				}
				for(String f: irisSubTypes) {
				
					if(f.equalsIgnoreCase("right") && (filteredAttribs != null && filteredAttribs.contains("leftEye"))) {
						capture =  mds.captureFromRegDevice(irisDevice,capture ,DataProviderConstants.MDS_DEVICE_TYPE_IRIS,
								null, 60, irisDevice.getDeviceSubId().get(0));	
					}
					else
					if(f.equalsIgnoreCase("left") && (filteredAttribs != null && filteredAttribs.contains("rightEye"))) {
						
						if(irisDevice.getDeviceSubId().size() > 1)
							capture =  mds.captureFromRegDevice(irisDevice,capture ,DataProviderConstants.MDS_DEVICE_TYPE_IRIS,
									null, 60, irisDevice.getDeviceSubId().get(1));
					}
				}
			}
		}
		
		if(biodata.getFingerPrint() != null) {
			List<BioModality> fingerExceptions = null;
			List<MDSDeviceCaptureModel> lstToRemove = new ArrayList<MDSDeviceCaptureModel>();
			if(bioExceptions != null && !bioExceptions.isEmpty())
				fingerExceptions = getModalitiesByType(bioExceptions, "Finger");

			List<MDSDevice> fingerDevices = mds.getRegDeviceInfo(DataProviderConstants.MDS_DEVICE_TYPE_FINGER);
			MDSDevice fingerDevice = fingerDevices.get(0);
		
			for(int i = 0; i < fingerDevice.getDeviceSubId().size(); i++) {
				capture =  mds.captureFromRegDevice(fingerDevice,capture ,DataProviderConstants.MDS_DEVICE_TYPE_FINGER,
						null, 60, fingerDevice.getDeviceSubId().get(i));
			}
			List<MDSDeviceCaptureModel> lstFingers= capture.getLstBiometrics().get(DataProviderConstants.MDS_DEVICE_TYPE_FINGER);
			if(fingerExceptions != null  && !fingerExceptions.isEmpty()) {
				//remove the fingers listed
			
				for(BioModality bm: fingerExceptions) {
					for(MDSDeviceCaptureModel mdc: lstFingers) {
						if( bm.getSubType().equalsIgnoreCase( mdc.getBioSubType().toLowerCase())){
							lstToRemove.remove( mdc);
						}
					}
				}
				lstFingers.removeAll(lstToRemove);	
			}
			if(filteredAttribs != null ) {
				//schemaNames
				String attr = null;
			
				for(MDSDeviceCaptureModel mdc: lstFingers) {
					int indx =0;
					boolean bFound = false;
					for(indx = 0; indx < DataProviderConstants.schemaNames.length ; indx++) {
						if(DataProviderConstants.displayFingerName[indx].equals(mdc.getBioSubType())) {
							attr = DataProviderConstants.schemaNames[indx];
							break;
						}
					}
					if(attr != null) {
						for(String a: filteredAttribs) {
							if(a.equals(attr)) {
								bFound = true;
								break;
							}
						}
					}
					if(!bFound)
						lstToRemove.add( mdc);
				}
				lstFingers.removeAll(lstToRemove);			
			}
			
		}
		
		if(!bNoMDS) {
			mds.removeProfile( mdsprofilePath, profileName );
		}
		return capture;
	}
	
	public static String toCBEFFFromCapture(List<String> bioFilter,MDSRCaptureModel capture, String toFile) throws Exception {
	
		String retXml = "";
		
		String mosipVersion=null;
		try {
	      mosipVersion=VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"mosip.version").toString();
		}catch(Exception e) {
			
		}

		XMLBuilder builder = XMLBuilder.create("BIR")
					.a("xmlns", "http://standards.iso.org/iso-iec/19785/-3/ed-2/")		
					.e("BIRInfo").e("Integrity").t("false").up().up();
			
		builder.getDocument().setXmlStandalone(true);
		
		List<String> bioSubType= new ArrayList<>();
			
		//Step 1: convert finger print
		
		List<MDSDeviceCaptureModel> lstFingerData =  capture.getLstBiometrics().get(DataProviderConstants.MDS_DEVICE_TYPE_FINGER);
	//	String [] fingerPrint =  biometricDataModel.getFingerPrint();
			//for(int i=0; i< fingerPrint.length; i++) {
		int i=0;
		String fingerData = null;
			
		for(String finger :bioFilter) {
			if(finger.toLowerCase().contains("eye") || finger.toLowerCase().equals("face"))
				continue;
			i = Arrays.asList(DataProviderConstants.schemaNames).indexOf(finger);
			String displayName = DataProviderConstants.displayFingerName[i];
			MDSDeviceCaptureModel currentCM=null;
			for(MDSDeviceCaptureModel cm:lstFingerData) {
				if(cm.getBioSubType().equals(displayName)) {
					fingerData = cm.getBioValue();
					bioSubType.add(finger);
					currentCM=cm;
					break;
				}
			}

			if(i >=0 && fingerData != null) {
					String strFinger = DataProviderConstants.displayFingerName[i];	
					String	strFingerXml = buildBirFinger(fingerData, strFinger, currentCM.getSb(),
							currentCM.getPayload(),currentCM.getQualityScore());
					XMLBuilder fbuilder = XMLBuilder.parse(strFingerXml);
					builder = builder.importXMLBuilder(fbuilder);
			}
				
		}
			
			//Step 2: Add Face
		if(bioFilter.contains("face")) {
				
				List<MDSDeviceCaptureModel> lstFaceData =  capture.getLstBiometrics().get(DataProviderConstants.MDS_DEVICE_TYPE_FACE);
				bioSubType.add("face");
				String faceXml = buildBirFace( lstFaceData.get(0).getBioValue(),lstFaceData.get(0).getSb(),
						lstFaceData.get(0).getPayload(),lstFaceData.get(0).getQualityScore());
				builder = builder.importXMLBuilder( XMLBuilder.parse( faceXml));
				
		}
			
			// Step 3: Add IRIS
		List<MDSDeviceCaptureModel> lstIrisData =  capture.getLstBiometrics().get(DataProviderConstants.MDS_DEVICE_TYPE_IRIS);

		if(lstIrisData != null) {
			String irisXml ="";
			for(MDSDeviceCaptureModel cm: lstIrisData) {
				
				
				if(bioFilter.contains("leftEye") && cm.getBioSubType().equals("Left")) {
						irisXml = buildBirIris( cm.getBioValue(), "Left",cm.getSb(),cm.getPayload(),cm.getQualityScore());
						builder = builder.importXMLBuilder( XMLBuilder.parse( irisXml));
						bioSubType.add("Left");
				}
				if(bioFilter.contains("rightEye") && cm.getBioSubType().equals("Right") ) {
					
						irisXml = buildBirIris( cm.getBioValue(), "Right",cm.getSb(),cm.getPayload(),cm.getQualityScore());
						builder = builder.importXMLBuilder( XMLBuilder.parse( irisXml));
						bioSubType.add("Right");
				}
			}
		}
		
		if (mosipVersion != null && mosipVersion.startsWith("1.2") && !bioSubType.isEmpty()) {
			builder.e("Others").e("Key").t("CONFIGURED").up().e("Value")
					.t(bioSubType.toString().substring(1, bioSubType.toString().length() - 1)).up().up();
		}
		if(toFile != null) {
				PrintWriter writer = new PrintWriter(new FileOutputStream(toFile));
				builder.toWriter(true, writer, null);
		}
		
		
		retXml = builder.asString(null);
//		PrintWriter writer = new PrintWriter(new FileOutputStream("cbeffout-cbeff-ALL"+ ".xml"));
//		builder.toWriter(true, writer, null);
		return retXml;
	}
		
	
	/*
	 * Construct CBEFF format XML file from biometric data 
	 */
	public static String toCBEFF(List<String> bioFilter,BiometricDataModel biometricDataModel, String toFile) throws Exception {
		String retXml = "";
		
		XMLBuilder builder = XMLBuilder.create("BIR")
				.a("xmlns", "http://standards.iso.org/iso-iec/19785/-3/ed-2/")		
				.e("BIRInfo").e("Integrity").t("false").up().up();
		
		builder.getDocument().setXmlStandalone(true);
		
		//Step 1: convert finger print
		String [] fingerPrint = biometricDataModel.getFingerPrint();
		
		//get qualityScore
		String qualityScore=null;
		Hashtable<String, List<MDSDeviceCaptureModel>> capture = biometricDataModel.getCapture();
		Enumeration<List<MDSDeviceCaptureModel>> elements = capture.elements();
		while(elements.hasMoreElements()) {
			List<MDSDeviceCaptureModel> nextElement = elements.nextElement();
			qualityScore=nextElement.get(0).getQualityScore();
			break;
		}
		
		//for(int i=0; i< fingerPrint.length; i++) {
		int i=0;
		for(String finger :bioFilter) {
			if(finger.toLowerCase().contains("eye") || finger.toLowerCase().equals("face"))
				continue;
			i = Arrays.asList(DataProviderConstants.schemaNames).indexOf(finger);
			
			if (i >= 0) {
				String strFinger = DataProviderConstants.displayFingerName[i];
				//TODO : THIS NEED TO IMPLEMENTED  WHEN WILL WORK WITH MDS
				String strFingerXml = buildBirFinger(fingerPrint[i], strFinger,null,null,qualityScore);
				XMLBuilder fbuilder = XMLBuilder.parse(strFingerXml);
				builder = builder.importXMLBuilder(fbuilder);
			}
			 
			
		}
		
		//Step 2: Add Face
		if(bioFilter.contains("Face")) {
			if(biometricDataModel.getEncodedPhoto() != null) {
				//TODO : THIS NEED TO IMPLEMENTED  WHEN WILL WORK WITH MDS
				String faceXml = buildBirFace( biometricDataModel.getEncodedPhoto(),null,null,qualityScore);
				builder = builder.importXMLBuilder( XMLBuilder.parse( faceXml));
			}
		}
		
		// Step 3: Add IRIS
		IrisDataModel irisInfo =  biometricDataModel.getIris();
		if(irisInfo != null) {
			String irisXml ="";
			if(bioFilter.contains("leftEye")) {
				//TODO : THIS NEED TO IMPLEMENTED  WHEN WILL WORK WITH MDS
				irisXml = buildBirIris( irisInfo.getLeft(), "Left",null,null,qualityScore);
				builder = builder.importXMLBuilder( XMLBuilder.parse( irisXml));
			}
			if(bioFilter.contains("rightEye")) {
				//TODO : THIS NEED TO IMPLEMENTED  WHEN WILL WORK WITH MDS
				irisXml = buildBirIris( irisInfo.getRight(), "Right",null,null,qualityScore);
				builder = builder.importXMLBuilder( XMLBuilder.parse( irisXml));
			}
		}
		
		if(toFile != null) {
			PrintWriter writer = new PrintWriter(new FileOutputStream(toFile));
			builder.toWriter(true, writer, null);
		}
		retXml = builder.asString(null);
		return retXml;
	}
	
	/*
	
	public static String toCBEFF(String [] prints) throws Exception {
		String retXml = "";
		RegistryIDType format = new RegistryIDType();
		format.setOrganization("257");
		format.setType("7");
		QualityType Qtype = new QualityType();
		Qtype.setScore(new Long(90));
		RegistryIDType algorithm = new RegistryIDType();
		algorithm.setOrganization("HMAC");
		algorithm.setType("SHA-256");
		Qtype.setAlgorithm(algorithm);
		ArrayList<BIR> createList = new ArrayList<BIR>();
		int i=0;
		Finger [] fingerType = Finger.values();
		
		for(i=0; i < prints.length; i++) {
			byte[] byteArray = prints[i].getBytes();
			try {

				BIR finger = new BIR.BIRBuilder().withBdb(byteArray )
						.withVersion(new BIRVersion.BIRVersionBuilder().withMinor(1).withMajor(1).build())
						.withCbeffversion(new BIRVersion.BIRVersionBuilder().withMinor(1).withMajor(1).build())
						.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
						.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format)
						.withQuality(Qtype).withType(Arrays.asList(SingleType.FINGER))
						.withSubtype(Arrays.asList(fingerName[i])) // fingerType[i].name()))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
						.build();
				
				createList.add(finger);
		
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		//CbeffImpl cbeffImpl = new CbeffImpl();
		CbeffContainerImpl cbeffContainer = new CbeffContainerImpl();
		BIRType bir = cbeffContainer.createBIRType(createList);
		byte[] xsd;
		try (InputStream xsdBytes = new FileInputStream("src/main/resource/schema/updatedcbeff.xsd")) {
			xsd = IOUtils.toByteArray(xsdBytes);
		}
		byte[] createXml = CbeffValidator.createXMLBytes(bir, xsd);
		//cbeffImpl.loadXSD();
		//byte[] createXml = cbeffImpl.createXML(createList);
		retXml  = new String(createXml);
	
		return retXml;
		
	}
	*/
	 
	public static BiometricDataModel getBiometricData(Boolean bFinger,String contextKey) throws IOException {
	
		BiometricDataModel data = new BiometricDataModel();
		
		File tmpDir;
	
		if(bFinger) {
			
			Object val = VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"enableExternalBiometricSource");
			boolean bExternalSrc = false;
			if(val != null )
				bExternalSrc = Boolean.valueOf(val.toString());
			
			if(bExternalSrc) {
				//folder where all bio input available
				String bioSrc = VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"externalBiometricsource").toString();

				String [] fingerPrints = new String[10];
				String [] fingerPrintHash = new String[10];
				byte[][] fingerPrintRaw = new byte[10][1];

				for(int i=0; i < 10; i++) {
					String modalityName = DataProviderConstants.MDSProfileFingerNames[i];
					modalityName = modalityName.replace('_', ' ');
					String fPath = bioSrc + modalityName +".jp2";
					byte[] fdata = Files.readAllBytes(Paths.get( fPath));
					fingerPrintRaw[i] = fdata;
					fingerPrints[i]= Base64.getEncoder().encodeToString(fdata);
					try {
						fingerPrintHash[i] =CommonUtil.getHexEncodedHash(fdata);
							
					} catch (Exception e) {
							// TODO Auto-generated catch block
						e.printStackTrace();
					}
					data.setFingerPrint(fingerPrints);
					data.setFingerHash(fingerPrintHash);
					data.setFingerRaw(fingerPrintRaw);
								
				}

			
				return data;
			}

			Boolean bAnguli = Boolean.parseBoolean( VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"enableAnguli").toString());
			if(bAnguli) {

								//else case
				try {
					tmpDir = Files.createTempDirectory("fps").toFile();
						Hashtable<Integer, List<File>> prints = generateFingerprint(tmpDir.getAbsolutePath(), 10, 2, 4, FPClassDistribution.arch );
					List<File> firstSet = prints.get(1);
			
					String [] fingerPrints = new String[10];
					String [] fingerPrintHash = new String[10];
					byte[][] fingerPrintRaw = new byte[10][1];
					
					int index = 0;
					for(File f: firstSet) {
						
						if(index >9) break;
						 Path path = Paths.get(f.getAbsolutePath());
						 byte[] fdata = Files.readAllBytes(path);
						 fingerPrintRaw[index] = fdata;
						 fingerPrints[index]= Base64.getEncoder().encodeToString(fdata);
						 
						 //fingerPrints[index]= Hex.encodeHexString( fdata ) ;
						// fingerPrints[index]=   fingerPrints[index].toUpperCase();
						 
						 //delete file
						try {
							fingerPrintHash[index] =CommonUtil.getHexEncodedHash(fdata);
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						 
						 index++;
						 
					}
					data.setFingerPrint(fingerPrints);
					data.setFingerHash(fingerPrintHash);
					data.setFingerRaw(fingerPrintRaw);
					tmpDir.deleteOnExit();
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else
			{
				
				
				//reach cached finger prints from folder
				String dirPath = VariableManager.getVariableValue(contextKey,"mosip.test.persona.fingerprintdatapath").toString();
				System.out.println("dirPath " + dirPath);
				Hashtable<Integer, List<File>> tblFiles = new Hashtable<Integer, List<File>>();
				File dir = new File(dirPath);

				File listDir[] = dir.listFiles();
				int numberOfSubfolders = listDir.length;

				int min=1;
				int max=numberOfSubfolders ;
				int randomNumber = (int) (Math.random()*(max-min)) + min;
					String beforescenario=VariableManager.getVariableValue(contextKey,"scenario").toString();
					String afterscenario=beforescenario.substring(0, beforescenario.indexOf(':'));
						
				int currentScenarioNumber = Integer.valueOf(afterscenario);
				
				System.out.println("beforescenario" +beforescenario + "afterscenario="+afterscenario);		
				// If the available impressions are less than scenario number, pick the random one

				// otherwise pick the impression of same of scenario number
				int impressionToPick = (numberOfSubfolders < currentScenarioNumber) ? currentScenarioNumber : randomNumber ;

				for(int i=min; i <= max; i++) {
					
					List<File> lst = CommonUtil.listFiles(dirPath +
							String.format("/Impression_%d/fp_1/", i));
					tblFiles.put(i,lst);
				}
				String [] fingerPrints = new String[10];
				String [] fingerPrintHash = new String[10];
				byte[][] fingerPrintRaw = new byte[10][1];
				List<File> firstSet = tblFiles.get(impressionToPick);
				System.out.println("Impression used "+ impressionToPick);
				
				int index = 0;
				for(File f: firstSet) {
					
					if(index >9) break;
					 Path path = Paths.get(f.getAbsolutePath());
					 byte[] fdata;
					try {
						fdata = Files.readAllBytes(path);
						fingerPrintRaw[index] = fdata;
						fingerPrints[index]= Base64.getEncoder().encodeToString(fdata);

						fingerPrintHash[index] =CommonUtil.getHexEncodedHash(fdata);
				
					} catch (  Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					index++;
					 
				}
				data.setFingerPrint(fingerPrints);
				data.setFingerHash(fingerPrintHash);
	data.setFingerRaw(fingerPrintRaw);
					
			}
		
			/*else
			{
				//reach cached finger prints from folder 
				//DataProviderConstants.RESOURCE +"/fingerprints/";
				String dirPath = VariableManager.getVariableValue(contextKey,"mosip.test.persona.fingerprintdatapath").toString();
			System.out.println("dirPath " + dirPath);
				Hashtable<Integer, List<File>> tblFiles = new Hashtable<Integer, List<File>>();
				int min=1;
						int max=2;
				int randomNumber = (int) (Math.random()*(max-min)) + min;
				
				for(int i=min; i <= max; i++) {
					
					List<File> lst = CommonUtil.listFiles(dirPath +
							String.format("/Impression_%d/fp_1/", i));
					tblFiles.put(i,lst);
				}
				String [] fingerPrints = new String[10];
				String [] fingerPrintHash = new String[10];
				byte[][] fingerPrintRaw = new byte[10][1];
				List<File> firstSet = tblFiles.get(randomNumber);
				System.out.println("Random Numer used "+ randomNumber);
				
				int index = 0;
				for(File f: firstSet) {
					
					if(index >9) break;
					 Path path = Paths.get(f.getAbsolutePath());
					 byte[] fdata;
					try {
						fdata = Files.readAllBytes(path);
						fingerPrintRaw[index] = fdata;
						fingerPrints[index]= Base64.getEncoder().encodeToString(fdata);

						fingerPrintHash[index] =CommonUtil.getHexEncodedHash(fdata);
				
					} catch (  Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					index++;
					 
				}
				data.setFingerPrint(fingerPrints);
				data.setFingerHash(fingerPrintHash);
	data.setFingerRaw(fingerPrintRaw);
					
				
			} */ 
		}
		return data;
	}
	
	//generate using Anguli
	
	static Hashtable<Integer, List<File>> generateFingerprint(String outDir, int nFingerPrints,
			int nImpressionsPerPrints , int nThreads, FPClassDistribution classDist){
		
		Hashtable<Integer, List<File>> tblFiles = new Hashtable<Integer, List<File>>();

		//C:\Mosip.io\gitrepos\biometric-data\anguli
		String[] commands = {DataProviderConstants.ANGULI_PATH+"/Anguli.exe",
				"-outdir" , outDir, "-numT",String.format("%d", nThreads),"-num",
				String.format("%d", nFingerPrints) ,"-ni",
				String.format("%d", nImpressionsPerPrints),"-cdist", classDist.name() };
System.out.println("Anguli commands" + commands);
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.directory(new File(DataProviderConstants.ANGULI_PATH));
		 
		try {
			Process proc = pb.start(); // rt.exec(commands);
			BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			// Read any errors from the attempted command
		//	System.out.println("Error:\n");
			String s;
			
			while (( s = stdError.readLine()) != null) {
			    System.out.println(s);
			}
			//read from outdir
			for(int i=1; i <= nImpressionsPerPrints; i++) {
				
				List<File> lst = CommonUtil.listFiles(outDir +
						String.format("/Impression_%d/fp_1/", i));
				tblFiles.put(i,lst);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tblFiles;
	}
	public static IrisDataModel loadIris(String filePath, String subModality, IrisDataModel im) throws Exception {
		
		IrisDataModel m = im;
		if(m == null)
			m = new IrisDataModel();
		String irisData ="";
		String irisHash ="";
			
		if(Files.exists(Paths.get(filePath))) {
			byte[] fdata = Files.readAllBytes(Paths.get(filePath));
			irisData = Hex.encodeHexString( fdata ) ;	
			irisHash = CommonUtil.getHexEncodedHash(fdata);
			if(subModality.equals("left")) {
				m.setLeftHash(irisHash);
				m.setLeft(irisData);
			}
			else {
				m.setRightHash(irisHash);
				m.setRight(irisData);
			}
		}

		return m;
	}
	//Left Eye, Right Eye
	static List<IrisDataModel> generateIris(int count,String contextKey) throws Exception{
	
		List<IrisDataModel> retVal = new ArrayList<IrisDataModel>();
		
		IrisDataModel m = new IrisDataModel();
		
		Object val = VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"enableExternalBiometricSource");
		boolean bExternalSrc = false;
		//BufferedImage img = null;

		
		if(val != null )
			bExternalSrc = Boolean.valueOf(val.toString());
		
		if(bExternalSrc) {
			//folder where all bio input available
			String bioSrc = VariableManager.getVariableValue(VariableManager.NS_DEFAULT,"externalBiometricsource").toString();

			String fPathL = bioSrc + "Left Iris.jp2";
			String fPathR = bioSrc + "Right Iris.jp2";
			
			 String leftIrisData ="";
			 String rightIrisData = "";
			 String irisHash = "";
			if(Files.exists(Paths.get(fPathL))) {
				 byte[] fdata = Files.readAllBytes(Paths.get(fPathL));
				leftIrisData = Hex.encodeHexString( fdata ) ;	
				irisHash = CommonUtil.getHexEncodedHash(fdata);
				m.setLeftHash(irisHash);
			}
			if(Files.exists(Paths.get(fPathR))) {
				 byte[] fdata = Files.readAllBytes(Paths.get(fPathR));
				rightIrisData = Hex.encodeHexString( fdata ) ;	
				irisHash = CommonUtil.getHexEncodedHash(fdata);
				m.setRightHash(irisHash);
			}
			if(leftIrisData.equals(""))
				leftIrisData = rightIrisData;
			else
			if(rightIrisData.equals(""))
					rightIrisData = leftIrisData;
			m.setLeft(leftIrisData);
			m.setRight(rightIrisData);
			retVal.add( m);
	
		
			
		}
		else
		{
			String srcPath = VariableManager.getVariableValue(contextKey,"mosip.test.persona.irisdatapath").toString(); 
			int []index = CommonUtil.generateRandomNumbers(count, 224, 1);
			
			for(int i=0; i < count; i++) {
				String fPathL = srcPath + String.format("%03d", index[i]) + "/01_L.bmp";
				String fPathR = srcPath + String.format("%03d", index[i]) + "/05_R.bmp";
				
				 String leftIrisData ="";
				 String rightIrisData = "";
				 String irisHash = "";
				if(Files.exists(Paths.get(fPathL))) {
					 byte[] fdata = Files.readAllBytes(Paths.get(fPathL));
					leftIrisData = Hex.encodeHexString( fdata ) ;	
					irisHash = CommonUtil.getHexEncodedHash(fdata);
					m.setLeftHash(irisHash);
				}
				if(Files.exists(Paths.get(fPathR))) {
					 byte[] fdata = Files.readAllBytes(Paths.get(fPathR));
					rightIrisData = Hex.encodeHexString( fdata ) ;	
					irisHash = CommonUtil.getHexEncodedHash(fdata);
					m.setRightHash(irisHash);
				}
				if(leftIrisData.equals(""))
					leftIrisData = rightIrisData;
				else
				if(rightIrisData.equals(""))
						rightIrisData = leftIrisData;
				m.setLeft(leftIrisData);
				m.setRight(rightIrisData);
				retVal.add( m);
			}
		}
		return retVal;
	}
	public static void main(String[] args) {
		
		try {
			String value=buildBirFinger("addfdfd","finger","jwtSign","payload",null);
			System.out.println(value);
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (ParserConfigurationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (FactoryConfigurationError e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (TransformerException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		try {
			List<IrisDataModel> m = generateIris(1,"contextKey");
			m.forEach( im -> {
				System.out.println(im.getLeftHash());
				System.out.println(im.getRightHash());
				
			});
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		BiometricDataModel bio = null;
		try {
			bio = getBiometricData(true,"contextkey");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		/*
		Hashtable<Integer, List<File>> tblFiles =generateFingerprint(DataProviderConstants.ANGULI_PATH+"/fps",10, 2,4, FPClassDistribution.arch);
		Set<Integer> k = tblFiles.keySet();

		k.forEach( (key)->{
			List<File> lst = tblFiles.get(key);
			lst.forEach( (f)->{
				System.out.println(f.getAbsolutePath());
				
			});
			
		});
		*/
		
		String xml ="";
		List<String> lstBioAttributes = new ArrayList<String>();
		lstBioAttributes.add("leftEye");
		lstBioAttributes.add("rightEye");
		
		try {
			xml = toCBEFF(lstBioAttributes, bio, "cbeffallfingersOut.xml");
/*
			CbeffContainerImpl cbeffContainer = new CbeffContainerImpl();
			//C:\temp\10002300012\REGISTRATION_CLIENT\NEW\rid_id\individualBiometrics_bio_CBEFF.xml
			byte[] xsd, xmlData;
			try (InputStream xsdBytes = new FileInputStream("src/main/resource/schema/updatedcbeff.xsd")) {
				xsd = IOUtils.toByteArray(xsdBytes);
			}
	
		try (InputStream xmlBytes = new FileInputStream("C:\\temp\\10002300012\\REGISTRATION_CLIENT\\NEW\\rid_id\\individualBiometrics_bio_CBEFF.xml")) {
				xmlData = IOUtils.toByteArray(xmlBytes);
			}

			//Boolean bret = cbeffContainer.validateXML(xmlData, xsd);
			
			Boolean bret = cbeffContainer.validateXML(xml.getBytes(), xsd);
			System.out.println("Valid ?" + bret);

*/

			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(xml);
	}
}
