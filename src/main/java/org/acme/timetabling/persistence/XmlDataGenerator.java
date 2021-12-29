package org.acme.timetabling.persistence;

import io.quarkus.panache.common.Sort;
import org.acme.timetabling.domain.*;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;

@ApplicationScoped
public class XmlDataGenerator {

    public static void main(){
        XmlDataGenerator generator = new XmlDataGenerator();
        generator.fetchData("SPC3thGrade");
    }

    protected final SolutionFileIO<TimeTable> solutionFileIO;
    protected final File outputDir;

    private static final Long SINGLETON_TIME_TABLE_ID = 1L;

    public static final String DATA_DIR_SYSTEM_PROPERTY = "org.optaplanner.examples.dataDir";

    public XmlDataGenerator() {
        solutionFileIO = new XStreamSolutionFileIO<>(TimeTable.class);
        outputDir = new File("/home/svs/IdeaProjects/school-timetabling/data/unsolved/");
    }

    private void fetchData(String oFileName){
        String outputFileName = oFileName + ".xml";
        File outputFile = new File(outputDir, outputFileName);
        TimeTable timeTable = new TimeTable(
                Timeslot.listAll(Sort.by("position")),
                Room.listAll(),
                Lesson.listAll(),
                LessonTask.listAll(),
                CourseLevel.listAll(),
                StudentGroup.listAll(),
                Teacher.listAll(),
                Preference.listAll(),
                SubjectCollection.listAll(),
                ThemeCollection.listAll());
        solutionFileIO.write(timeTable,outputFile);
    }
}
