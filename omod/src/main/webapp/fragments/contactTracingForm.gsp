<%
    ui.decorateWith("kenyaui", "panel", [heading: (command.original ? "Edit" : "Add") + " ContactTracing", frameOnly: true])

    def date = [
            [
                    [object: command, property: "date", label: "Date"]

            ]
    ]

    def patientContactTracing = [
            [

                    [object: command, property: "contactType", label: "Contact Type"],
                    [object: command, property: "status", label: "Status"]

            ],
    ]


    def linkageToCare = [
            [
                    [object: command, property: "facilityLinkedTo", label: "Facility Linked To"],
                    [object: command, property: "uniquePatientNo", label: "Unique Patient No."],
                    [object: command, property: "healthWorkerHandedTo", label: "Health Worker Linked To"]

            ]
    ]

    def remarks = [
            [
                    [object: command, property: "remarks", label: "Remarks"]
            ]
    ]


%>

<form id="patient-contact-trace-form" method="post"
      action="${ui.actionLink("hivtestingservices", "contactTracingForm", "saveClientTrace")}">
    <% if (command.original) { %>
    <input type="hidden" name="id" value="${command.original.id}"/>
    <% } %>

    <div class="ke-panel-content">

        <div class="ke-form-globalerrors" style="display: none"></div>

        <div class="ke-form-instructions">
            <strong>*</strong> indicates a required field
        </div>

        <fieldset>
            <legend>Tracing Date</legend>

            <% date.each { %>
            ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
            <% } %>

        </fieldset>

        <fieldset>
            <legend>Tracing Details</legend>
            <table>
                <tr>
                    <td class="ke-field-label">Contact Type</td>
                    <td class="ke-field-label">Outcome</td>
                </tr>
                <tr>
                    <td style="width: 270px">
                        <select name="contactType" id="contactType">
                            <option></option>
                            <% contactOptions.each { %>
                            <option value="${it}">${it}</option>
                            <% } %>
                        </select>
                    </td>
                    <td style="width: 260px">
                        <select name="status" id="tracingOutcome">
                            <option></option>
                            <% tracingOutcomeOptions.each { %>
                            <option value="${it}">${it}</option>
                            <% } %>
                        </select>
                    </td>
                </tr>
            </table>

        </fieldset>

        <fieldset id="linkageSection">
            <legend>Linkage Details</legend>
            <% linkageToCare.each { %>
            ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
            <% } %>
        </fieldset>

        <fieldset>
            <legend>Remarks</legend>
            <table>
                <tr>
                    <td class="ke-field-label">Provider Remarks</td>
                </tr>
                <tr>
                    <td>
                        <textarea name="remarks" rows="5" cols="80"></textarea>
                    </td>
                </tr>
            </table>
        </fieldset>

        <div class="ke-panel-footer">
            <button type="submit">
                <img src="${ui.resourceLink("kenyaui", "images/glyphs/ok.png")}"/> ${command.original ? "Save Changes" : "Create Contact Trace"}
            </button>
            <% if (config.returnUrl) { %>
            <button type="button" class="cancel-button"><img
                    src="${ui.resourceLink("kenyaui", "images/glyphs/cancel.png")}"/> Cancel</button>
            <% } %>
        </div>
    </div>
</form>


<script type="text/javascript">

    //On ready
    jQuery(function () {
        //defaults

        jQuery("#linkageSection").hide(); //hide linkage section

        jQuery('#patient-contact-trace-form .cancel-button').click(function () {
            ui.navigate('${ config.returnUrl }');
        });
        kenyaui.setupAjaxPost('patient-contact-trace-form', {
            onSuccess: function (data) {
                if (data.id) {
                    <% if (config.returnUrl) { %>
                    ui.navigate('${ config.returnUrl }');
                    <% } else { %>
                    ui.navigate('hivtestingservices', 'patientContactForm', {patientContactId: data.id});
                    <% } %>
                } else {
                    kenyaui.notifyError('Saving contact tracing was successful, but with unexpected response');
                }
            }
        });

        jQuery('#tracingOutcome').change(function () {
            var selectedOutcome = jQuery(this).val();
            var contactType = jQuery("#contactType").val();

            if(contactType != "") {
                if (selectedOutcome == "Contacted and Linked") {
                    jQuery("#linkageSection").show();
                } else {
                    jQuery("#linkageSection input").val("");
                    jQuery("#linkageSection").hide();
                }
            }
        });

        jQuery('#contactType').change(function () {
            var selectedOutcome = jQuery(this).val();
            var outcome = jQuery("#tracingOutcome").val();

            if(selectedOutcome != "" && outcome != "") {
                if (outcome == "Contacted and Linked") {
                    jQuery("#linkageSection").show();
                } else {
                    jQuery("#linkageSection input").val("");
                    jQuery("#linkageSection").hide();
                }
            }
        });

    }); // end of jQuery initialization bloc


</script>