package com.shasthosheba.doctor.app;

public enum IntentTags {
    INTERMEDIARY_NAME("doctor_name"),
    INTERMEDIARY_UID("doctor_uid"),
    INTERMEDIARY_STATUS("doctor_status"),
    DOCTOR("doctor_object"),
    DOCTOR_ID("doctor_id"),
    INTERMEDIARY_CALL_ENABLED("intermediary_call_enabled"),
    INTERMEDIARY_OBJ("intermediary_object"),

    PATIENT_ID("patient_id"),
    PATIENT_PRES_ID_LIST("patient_prescription_id_list"),
    PATIENT_NAME("patient_name"),
    PATIENT_BIRTH_YEAR("patient_birth_year"),
    PATIENT_OBJ("patient_object"),

    PRESC_PATIENT("presc_patient"),
    PRESC_DOC("presc_doctor"),
    PRESC_INTERMEDIARY_OBJ("presc_intermediary"),
    PRESCRIPTION_OBJ("prescription_object"),

    CHAMBER_MEMBER_OBJ("chamber_member_obj"),
    ;

    public final String tag;

    IntentTags(String tag) {
        this.tag = tag;
    }
}
