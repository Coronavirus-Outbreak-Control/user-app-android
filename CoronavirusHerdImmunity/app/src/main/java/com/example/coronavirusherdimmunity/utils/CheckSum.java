package com.example.coronavirusherdimmunity.utils;

public class CheckSum {
    /**
     * Compute checksum by adding all digits of patient id and get last digit as checksum
     * @param patientId scanned by QR code
     * @return checksum computed
     */
    public static long computeChecksum(long patientId){
        long checksum = 0;
        long sum = 0;
        long digits = patientId;
        while(digits > 0){
            sum = sum + (digits % 10);
            digits = digits / 10;
        }
        checksum = sum % 10; //get last digit of sum
        return checksum;
    }
    /**
     * Split "patient Id" by "checksum" (last digit of "patId_with_checksum"), and verify if checksum is right or not
     * @param patId_with_checksum: patientId+checksum
     * @return "true" if checksum is right, "false" otherwise
     */
    public boolean verifyChecksum (long patId_with_checksum){
        long checksum = patId_with_checksum % 10;   //get checksum by patId_with_checksum (last digit of patId_with_checksum)
        long patient_id = patId_with_checksum / 10; // get patient id (patId_with_checksum without last digit)
        return (checksum == computeChecksum(patient_id)); //return true if checksum is well computed, else false
    }
}