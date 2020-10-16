package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import java.time.LocalDateTime;
import java.util.HashSet;

public class ReferenceNumberTestHarness {

    public static void main(String[] args) {
        dailySimulation();
    }

    private static void dailySimulation() {

        int YEARS = 5;
        int NUM_REF_NOS_PER_DAY = 1000;

        System.out.println("YEARS = " + YEARS);
        System.out.println("NUMBER OF REF NOs TO GENERATE PER DAY = " + NUM_REF_NOS_PER_DAY);
        System.out.println("-----------------------");

        ReferenceNumberGeneratorService generatorService = new ReferenceNumberGeneratorService(LocalDateTime::now);

        int totalDuplicates = 0;
        int daysWithDuplicates = 0;
        HashSet<String> allGeneratedRefNos = new HashSet<>();

        for (int year = 0; year < YEARS; year++) {

            for (int day = 0; day < 365; day++) {

                int duplicateCountForTheDay = 0;
                HashSet<String> refNosForTheDay = new HashSet<>();

                String refNo;
                for (int i = 0; i < NUM_REF_NOS_PER_DAY; i++) {

                    refNo = generatorService.generateReferenceNumber();

                    // check if we've already had this refNo today
                    if (refNosForTheDay.contains(refNo)) {
                        System.out.println("MATCH - " + refNo);
                        duplicateCountForTheDay++;
                    }

                    // check if we've had this code ever
                    if (allGeneratedRefNos.contains(refNo)) {
                        totalDuplicates++;
                    }

                    refNosForTheDay.add(refNo);
                    allGeneratedRefNos.add(refNo);
                }

                // print out duplicate count at end of day
                if (duplicateCountForTheDay > 0) {
                    daysWithDuplicates++;
                    System.out.println(" * MATCH COUNT for year " + year + " day " + day + " = " + duplicateCountForTheDay);
                }
            }
        }

        System.out.println("-----------------------");
        System.out.println("NUMBER OF DAYS WITH DUPLICATES = " + daysWithDuplicates);
        System.out.println("TOTAL DUPLICATES OUT OF ALL REF NOs GENERATED = " + totalDuplicates);
    }
}
