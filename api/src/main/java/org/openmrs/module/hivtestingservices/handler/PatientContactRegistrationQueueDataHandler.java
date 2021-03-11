/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.hivtestingservices.handler;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.hivtestingservices.api.service.RegistrationDataService;
import org.openmrs.module.hivtestingservices.exception.QueueProcessorException;
import org.openmrs.module.hivtestingservices.model.QueueData;
import org.openmrs.module.hivtestingservices.model.RegistrationData;
import org.openmrs.module.hivtestingservices.model.handler.QueueDataHandler;
import org.openmrs.module.hivtestingservices.utils.JsonUtils;

import java.util.Date;

/**
 * Processes patient contact registration using relationships
 */
@Handler(supports = QueueData.class, order = 12)
public class PatientContactRegistrationQueueDataHandler implements QueueDataHandler {


    private static final String DISCRIMINATOR_VALUE = "json-createpatientcontactusingrelatioship";
    private final Log log = LogFactory.getLog(PatientContactRegistrationQueueDataHandler.class);
    private PatientContact unsavedPatientContact;
    private String payload;
    private QueueProcessorException queueProcessorException;


    @Override
    public void process(final QueueData queueData) throws QueueProcessorException {
        log.info("Processing patient contact registration using relationship: " + queueData.getUuid());
        queueProcessorException = new QueueProcessorException();
        try {
            if (validate(queueData)) {
                registerUnsavedPatientContact();
            }
        } catch (Exception e) {
            if (!e.getClass().equals(QueueProcessorException.class)) {
                queueProcessorException.addException(new Exception("Exception while process payload ",e));
            }
        } finally {
            if (queueProcessorException.anyExceptions()) {
                throw queueProcessorException;
            }
        }
    }

    @Override
    public boolean validate(QueueData queueData) {
        log.info("Processing patient contact registration using relationship: " + queueData.getUuid());
        queueProcessorException = new QueueProcessorException();
        try {
            payload = queueData.getPayload();
            unsavedPatientContact = new PatientContact();
            populateUnsavedPatientContactFromPayload();
            return true;
        } catch (Exception e) {
            queueProcessorException.addException(new Exception("Exception while validating payload ",e));
            return false;
        } finally {
            if (queueProcessorException.anyExceptions()) {
                throw queueProcessorException;
            }
        }
    }

    @Override
    public String getDiscriminator() {
        return DISCRIMINATOR_VALUE;
    }

    private void populateUnsavedPatientContactFromPayload() {
        setPatientContactFromPayload();
    }

    private void setPatientContactFromPayload(){
        String givenName = JsonUtils.readAsString(payload, "$['patient_firstName']");
        String middleName = JsonUtils.readAsString(payload, "$['patient_middleName']");
        String familyName = JsonUtils.readAsString(payload, "$['patient_familyName']");
        Integer relType = relationshipTypeConverter(JsonUtils.readAsString(payload, "$['relation_type']"));
        Date birthDate = JsonUtils.readAsDate(payload, "$['patient_birthDate']", JsonUtils.DATE_PATTERN_MEDIC);
        String sex = gender(JsonUtils.readAsString(payload, "$['patient_sex']"));
        String phoneNumber = JsonUtils.readAsString(payload, "$['patient_telephone']");
        Integer maritalStatus = maritalStatusConverter(JsonUtils.readAsString(payload, "$['patient_marital_status']"));
        String physicalAddress = JsonUtils.readAsString(payload, "$['patient_postalAddress']");

        Integer patientRelatedTo = null;
        Integer patient = null;
        String indexKemrUuid = JsonUtils.readAsString(payload, "$['relation_uuid']");
        patientRelatedTo = org.apache.commons.lang3.StringUtils.isNotBlank(indexKemrUuid) ? getPatientRelatedToContact(indexKemrUuid) : getPatientRelatedToContact(JsonUtils.readAsString(payload, "$['parent']['_id']"));
        String uuid = JsonUtils.readAsString(payload, "$['_id']");
        patient = org.apache.commons.lang3.StringUtils.isNotBlank(uuid) ? getPatientAsContact(uuid) : getPatientAsContact(JsonUtils.readAsString(payload, "$['parent']['_id']"));

        Boolean voided= false;

        unsavedPatientContact.setFirstName(givenName);
        unsavedPatientContact.setMiddleName(middleName);
        unsavedPatientContact.setLastName(familyName);
        unsavedPatientContact.setRelationType(relType);
        unsavedPatientContact.setBirthDate(birthDate);
        unsavedPatientContact.setSex(sex);
        unsavedPatientContact.setPhoneContact(phoneNumber);
        unsavedPatientContact.setMaritalStatus(maritalStatus);
        unsavedPatientContact.setPhysicalAddress(physicalAddress);
        unsavedPatientContact.setPatientRelatedTo(Context.getPatientService().getPatient(patientRelatedTo));
        unsavedPatientContact.setPatient(Context.getPatientService().getPatient(patient));
        unsavedPatientContact.setUuid(uuid);
        unsavedPatientContact.setVoided(voided);
    }

    private void registerUnsavedPatientContact() {
        HTSService htsService = Context.getService(HTSService.class);
        RegistrationDataService registrationDataService = Context.getService(RegistrationDataService.class);
        String temporaryUuid = getPatientContactUuidFromPayload();
        RegistrationData registrationData = registrationDataService.getRegistrationDataByTemporaryUuid(temporaryUuid);

        if (registrationData != null) {
            try {
                htsService.savePatientContact(unsavedPatientContact);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            log.info("Unable to save, same contact already exist for the patient");
        }
    }

    private String getPatientContactUuidFromPayload(){
        return JsonUtils.readAsString(payload, "$['_id']");
    }

    private Integer getPatientRelatedToContact(String uuid) {
        Integer patientId = null;
        RegistrationDataService regDataService = Context.getService(RegistrationDataService.class);
        RegistrationData regData = regDataService.getRegistrationDataByTemporaryUuid(uuid);
        if(regData != null) {
            Patient p = Context.getPatientService().getPatientByUuid(regData.getAssignedUuid());
            if (p !=null){
                 patientId= p.getPatientId();
            }
        }
        return patientId;
    }

    private Integer getPatientAsContact(String uuid) {
        Integer patientId = null;
        RegistrationDataService regDataService = Context.getService(RegistrationDataService.class);
        RegistrationData regData = regDataService.getRegistrationDataByTemporaryUuid(uuid);
        if(regData != null) {
            Patient p = Context.getPatientService().getPatientByUuid(regData.getAssignedUuid());
            if (p !=null){
                patientId= p.getPatientId();
            }
        }

        return patientId;
    }

    private Integer relationshipTypeConverter(String relType) {
        Integer relTypeConverter = null;
        if(relType.equalsIgnoreCase("partner")){
            relTypeConverter =163565;
        }else if(relType.equalsIgnoreCase("parent") || relType.equalsIgnoreCase("guardian") || relType.equalsIgnoreCase("mother") || relType.equalsIgnoreCase("father")){
            relTypeConverter =970;
        }else if(relType.equalsIgnoreCase("sibling")){
            relTypeConverter =972;
        }else if(relType.equalsIgnoreCase("child")){
            relTypeConverter =1528;
        }else if(relType.equalsIgnoreCase("spouse")){
            relTypeConverter =5617;
        }else if(relType.equalsIgnoreCase("co-wife")){
            relTypeConverter =162221;
        }else if(relType.equalsIgnoreCase("Injectable drug user")){
            relTypeConverter =157351;
        }
        return relTypeConverter;
    }

    private Integer maritalStatusConverter(String marital_status) {
        Integer civilStatusConverter = null;
        if(marital_status.equalsIgnoreCase("_1057_neverMarried_99DCT")){
            civilStatusConverter =1057;
        }else if(marital_status.equalsIgnoreCase("_1058_divorced_99DCT")){
            civilStatusConverter =1058;
        }else if(marital_status.equalsIgnoreCase("_1059_widowed_99DCT")){
            civilStatusConverter =1059;
        }else if(marital_status.equalsIgnoreCase("_5555_marriedMonogomous_99DCT")){
            civilStatusConverter =5555;
        }else if(marital_status.equalsIgnoreCase("_159715_marriedPolygamous_99DCT")){
            civilStatusConverter =159715;
        }else if(marital_status.equalsIgnoreCase("_1060_livingWithPartner_99DCT")){
            civilStatusConverter =1060;
        }
        return civilStatusConverter;
    }
    private Integer livingWithPartnerConverter(String livingWithPatient) {
        Integer livingWithPatientConverter = null;
        if(livingWithPatient.equalsIgnoreCase("no")){
            livingWithPatientConverter =1066;
        }else if(livingWithPatient.equalsIgnoreCase("yes")){
            livingWithPatientConverter =1065;
        }else if(livingWithPatient.equalsIgnoreCase("Declined to answer")){
            livingWithPatientConverter =162570;
        }
        return livingWithPatientConverter;
    }

    private Integer pnsApproachConverter(String pns_approach) {
        Integer pnsApproach = null;
        if(pns_approach.equalsIgnoreCase("dual_referral")){
            pnsApproach =162284;
        }else if(pns_approach.equalsIgnoreCase("provider_referral")){
            pnsApproach =163096;
        }else if(pns_approach.equalsIgnoreCase("contract_referral")){
            pnsApproach =161642;
        } else if(pns_approach.equalsIgnoreCase("passive_referral")){
            pnsApproach =160551;
        }
        return pnsApproach;
    }

    private String gender(String gender) {
        String abbriviateGender = null;
        if(gender.equalsIgnoreCase("male")){
            abbriviateGender ="M";
        }
        if(gender.equalsIgnoreCase("female")) {
            abbriviateGender ="F";
        }
        return abbriviateGender;
    }


    @Override
    public boolean accept(final QueueData queueData) {
        return StringUtils.equals(DISCRIMINATOR_VALUE, queueData.getDiscriminator());
    }

}