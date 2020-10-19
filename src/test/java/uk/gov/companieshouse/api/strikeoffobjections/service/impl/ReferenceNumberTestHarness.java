package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import java.time.LocalDateTime;
import java.util.HashSet;

class ReferenceNumberTestHarness {

    public static void main(String[] args) {
        dailySimulation();
        // bruteForceTest();
    }

    private static void dailySimulation() {

        int YEARS = 5;
        int NUM_REF_NOS_PER_DAY = 5000;

        System.out.println("YEARS = " + YEARS);
        System.out.println("NUMBER OF REF NOs TO GENERATE PER DAY = " + NUM_REF_NOS_PER_DAY);
        System.out.println("-----------------------");

        LocalDateTime date = LocalDateTime.now();

        int totalDuplicates = 0;
        int daysWithDuplicates = 0;
        HashSet<String> allGeneratedRefNos = new HashSet<>();

        for (int year = 0; year < YEARS; year++) {

            for (int day = 0; day < 365; day++) {
                // increment the date -> next day
                date = date.plusDays(1);
                // lambda required a 'final' var to pass in
                LocalDateTime finalDate = date;
                ReferenceNumberGeneratorService generatorService = new ReferenceNumberGeneratorService(() -> finalDate);

                int duplicateCountForTheDay = 0;
                HashSet<String> refNosForTheDay = new HashSet<>();

                String refNo;
                for (int i = 0; i < NUM_REF_NOS_PER_DAY; i++) {

                    refNo = generatorService.generateReferenceNumber();

                    // check if we've already had this refNo today
                    if (refNosForTheDay.contains(refNo)) {
                        // System.out.println("MATCH - " + refNo);
                        duplicateCountForTheDay++;
                    }

                    // check if we've had this code ever
                    if (allGeneratedRefNos.contains(refNo)) {
                       // System.out.println("MATCH - " + refNo);
                        totalDuplicates++;
                    }

                    refNosForTheDay.add(refNo);
                    allGeneratedRefNos.add(refNo);
                }

                // print out duplicate count at end of day
                if (duplicateCountForTheDay > 0) {
                    daysWithDuplicates++;
                    System.out.println(" * DUPLICATE COUNT for year " + year + " day " + day + " = " + duplicateCountForTheDay);
                }
            }
        }

        System.out.println("-----------------------");
        System.out.println("NUMBER OF DAYS WITH DUPLICATES = " + daysWithDuplicates);
        System.out.println("TOTAL DUPLICATES OUT OF ALL REF NOs GENERATED = " + totalDuplicates);
    }

    private static void bruteForceTest() {
        int LIMIT = 1000000;

        ReferenceNumberGeneratorService generatorService = new ReferenceNumberGeneratorService(LocalDateTime::now);

        int duplicateCount = 0;
        HashSet<String> allRefNos = new HashSet<>();

        String refNo;
        for (int i = 0; i < LIMIT; i++) {
            refNo = generatorService.generateReferenceNumber();

            if (allRefNos.contains(refNo)) {
                duplicateCount++;
            }
            allRefNos.add(refNo);
        }

        System.out.println("TOTAL DUPLICATES = " + duplicateCount);
    }
}
