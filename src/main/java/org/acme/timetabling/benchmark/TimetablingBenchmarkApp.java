package org.acme.timetabling.benchmark;

import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;

import java.io.File;


public abstract class TimetablingBenchmarkApp{

    public static void main(String[] args){
        String FILENAME = "/home/svs/IdeaProjects/school-timetabling/src/main/java/org/acme/timetabling/benchmark/timetablingBenchmarkConfig.xml";
        File configXmlFile = new File(FILENAME);
        PlannerBenchmarkFactory plannerBenchmarkFactory = PlannerBenchmarkFactory.createFromXmlFile(configXmlFile);
        PlannerBenchmark plannerBenchmark = plannerBenchmarkFactory.buildPlannerBenchmark();
        plannerBenchmark.benchmarkAndShowReportInBrowser();
    }

}