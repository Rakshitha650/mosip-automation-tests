package io.mosip.test.packetcreator.mosippacketcreator.service;

import org.json.JSONObject;

import org.mosip.dataprovider.test.partnerManagement.PartnerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.mosip.test.packetcreator.mosippacketcreator.dto.SelfRegisterDto;

@Component
public class PartnerService {
    
    private static Logger logger = LoggerFactory.getLogger(PartnerService.class);

    public String selfRegister(SelfRegisterDto selfRegister,String contextKey){
        JSONObject request = new JSONObject();
        request.put("address", selfRegister.getAddress());
        request.put("contactNumber", selfRegister.getContactNumber());
        request.put("emailId", selfRegister.getEmailId());
        request.put("organizationName", selfRegister.getOrganizationName());
        request.put("partnerId", selfRegister.getPartnerId());
        request.put("partnerType", selfRegister.getPartnerType());
        request.put("policyGroup", selfRegister.getPolicyGroup());

        JSONObject selfRegisterRequest = new JSONObject();
        selfRegisterRequest.put("id", "string");
        selfRegisterRequest.put("metadata", new JSONObject());
        selfRegisterRequest.put("request", request);
        
        return PartnerRequest.selfRegister(selfRegisterRequest,contextKey);
    }

    public String updatePartnerStatus(String contextKey,String partnerId, String status){
        return PartnerRequest.updatePartnerStatus(contextKey,partnerId, status);
    }
    
    public String submitPartnerAPIKeyRequest(String partnerID, String policyName, String useCaseDesc,String contextKey){
        return PartnerRequest.submitAPIKeyRequest(partnerID, policyName, useCaseDesc,contextKey);
    }

    public String approvePartnerAPIKeyRequest(String contextKey,String apiKeyRequestID){
        return PartnerRequest.approvePartnerAPIKeyRequest(contextKey,apiKeyRequestID);
    }

}
