package org.openmrs.module.hivtestingservices.fragment.controller;

import org.openmrs.Patient;
import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivtestingservices.api.HTSService;
import org.openmrs.module.hivtestingservices.api.PatientContact;
import org.openmrs.module.kenyaui.form.AbstractWebForm;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.MethodParam;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Controller for adding and editing Patient Contacts
 */
public class PatientContactFormFragmentController{

    public void controller(@FragmentParam(value = "patientContact", required = false) PatientContact patientContact,
                           @RequestParam(value = "patientId", required = true) Patient patient,
                           PageModel model) {

        PatientContact exists = patientContact != null ? patientContact : null;

        model.addAttribute("command", newEditPatientContactForm(exists, patient));
        model.addAttribute("relationshipTypeOptions", getRelationshipTypeOptions());
        model.addAttribute("hivStatusOptions", hivStatusOptions());
        model.addAttribute("ipvOutcomeOptions", ipvOutcomeOptions());


    }

    private List<String> hivStatusOptions() {
        return Arrays.asList("Unknown", "Positive", "Negative");
    }

    private List<String> ipvOutcomeOptions() {
        return Arrays.asList("True", "False");
    }

    protected List<SimpleObject> getRelationshipTypeOptions() {
        List<SimpleObject> options = new ArrayList<SimpleObject>();

        for (RelationshipType type : Context.getPersonService().getAllRelationshipTypes()) {
            if (type.getaIsToB().equals(type.getbIsToA())) {
                options.add(SimpleObject.create("value", type.getId(), "label", type.getaIsToB()));
            }
            else {
                options.add(SimpleObject.create("value", type.getId(), "label", type.getaIsToB()));
            }
        }

        return options;
    }
    public SimpleObject savePatientContact(@MethodParam("newEditPatientContactForm") @BindParams EditPatientContactForm form, UiUtils ui) {
        ui.validate(form, form, null);

        PatientContact patientContact = form.save();

        return SimpleObject.create("id", patientContact.getPatientRelatedTo().getId());
    }

    public EditPatientContactForm newEditPatientContactForm(@RequestParam(value = "id", required = false) PatientContact patientContact, @RequestParam(value = "patientRelatedTo", required = true) Patient patient) {
        if (patientContact != null) {

            return new EditPatientContactForm(patientContact, patient);
        } else {
            return new EditPatientContactForm(patient);
        }
    }

    public class EditPatientContactForm extends AbstractWebForm {

        private PatientContact original;
        private String firstName;
        private String middleName;
        private String lastName;
        private String sex;
        private Date birthDate;
        private String physicalAddress;
        private String phoneContact;
        private Patient patientRelatedTo;
        private String relationType;
        private Date appointmentDate;
        private String baselineHivStatus;
        private String ipvOutcome;


        public EditPatientContactForm() {
        }

        public EditPatientContactForm(Patient patient) {
            this.patientRelatedTo = patient;
        }

        public EditPatientContactForm(PatientContact patientContact, Patient patient) {

            this.original = patientContact;
            this.firstName = patientContact.getFirstName();
            this.middleName = patientContact.getMiddleName();
            this.lastName = patientContact.getLastName();
            this.sex = patientContact.getSex();
            this.birthDate = patientContact.getBirthDate();
            this.physicalAddress = patientContact.getPhysicalAddress();
            this.phoneContact = patientContact.getPhoneContact();
            this.patientRelatedTo = patient;
            this.relationType = patientContact.getRelationType();
            this.appointmentDate = patientContact.getAppointmentDate();
            this.baselineHivStatus = patientContact.getBaselineHivStatus();
            this.ipvOutcome = patientContact.getIpvOutcome();

        }

        public PatientContact save() {
            PatientContact toSave;
            if (original != null) {

                toSave = original;
            } else {
                toSave = new PatientContact();
            }

            toSave.setFirstName(firstName);
            toSave.setMiddleName(middleName);
            toSave.setLastName(lastName);
            toSave.setSex(sex);
            toSave.setBirthDate(birthDate);
            toSave.setPatientRelatedTo(patientRelatedTo);
            toSave.setRelationType(relationType);
            toSave.setPhysicalAddress(physicalAddress);
            toSave.setPhoneContact(phoneContact);
            toSave.setAppointmentDate(appointmentDate);
            toSave.setBaselineHivStatus(baselineHivStatus);
            toSave.setIpvOutcome(ipvOutcome);
            PatientContact pc = Context.getService(HTSService.class).savePatientContact(toSave);
            return pc;
        }

        @Override
        public void validate(Object o, Errors errors) {
            require(errors, "sex");
            require(errors, "birthDate");
            if (birthDate != null) {
                if (birthDate.after(new Date())) {
                    errors.rejectValue("birthDate", "Cannot be a future date");
                } else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());
                    calendar.add(Calendar.YEAR, -120);
                    if (birthDate.before(calendar.getTime())) {
                        errors.rejectValue("birthDate", "error.date.invalid");
                    }
                }
            }
        }

        public PatientContact getOriginal() {
            return original;
        }

        public void setOriginal(PatientContact original) {
            this.original = original;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getMiddleName() {
            return middleName;
        }

        public void setMiddleName(String middleName) {
            this.middleName = middleName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }

        public Date getBirthDate() {
            return birthDate;
        }

        public void setBirthDate(Date birthDate) {
            this.birthDate = birthDate;
        }

        public String getPhysicalAddress() {
            return physicalAddress;
        }

        public void setPhysicalAddress(String physicalAddress) {
            this.physicalAddress = physicalAddress;
        }

        public String getPhoneContact() {
            return phoneContact;
        }

        public void setPhoneContact(String phoneContact) {
            this.phoneContact = phoneContact;
        }

        public Patient getPatientRelatedTo() {
            return patientRelatedTo;
        }

        public void setPatientRelatedTo(Patient patientRelatedTo) {
            this.patientRelatedTo = patientRelatedTo;
        }

        public String getRelationType() {
            return relationType;
        }

        public void setRelationType(String relationType) {
            this.relationType = relationType;
        }

        public Date getAppointmentDate() {
            return appointmentDate;
        }

        public void setAppointmentDate(Date appointmentDate) {
            this.appointmentDate = appointmentDate;
        }

        public String getBaselineHivStatus() {
            return baselineHivStatus;
        }

        public void setBaselineHivStatus(String baselineHivStatus) {
            this.baselineHivStatus = baselineHivStatus;
        }

        public String getIpvOutcome() {
            return ipvOutcome;
        }

        public void setIpvOutcome(String ipvOutcome) {
            this.ipvOutcome = ipvOutcome;
        }

    }


}