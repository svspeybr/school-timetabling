package org.acme.timetabling.persistence;

import io.quarkus.panache.common.Sort;
import org.acme.timetabling.domain.*;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;


import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

public class XmlFileIO {

    protected final SolutionFileIO<TimeTable> solutionFileIO;
    private String outputDir;
    private String fileName;

    private File file;
    /* private static final Long SINGLETON_TIME_TABLE_ID = 1L;

    public static final String DATA_DIR_SYSTEM_PROPERTY = "org.optaplanner.examples.dataDir";*/

    public XmlFileIO() {
        solutionFileIO = new XStreamSolutionFileIO<>(TimeTable.class);
    }

    public XmlFileIO(String outputDir, String fileName) {
        solutionFileIO = new XStreamSolutionFileIO<>(TimeTable.class);
        this.outputDir = outputDir;
        this.fileName = fileName;

    }

    public void setOutputDir(String pathName) {
        this.outputDir = pathName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public File getFile(){return file;}

    @Transactional
    public void saveDataToFile(){
        String outputFileName = fileName + ".xml";
        this.file = new File(outputDir, outputFileName);
        TimeTable timeTable = new TimeTable(
                null,
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
        solutionFileIO.write(timeTable, file);
    }

    public void deleteXmlFile(){
        String outputFileName = this.fileName + ".xml";
        try {
            Files.deleteIfExists(
                    Paths.get(this.outputDir + "/" + outputFileName));
        }
        catch (NoSuchFileException e) {
            System.out.println("No such file/directory exists");
        }
        catch (DirectoryNotEmptyException e) {
            System.out.println("Directory is not empty.");
        }
        catch (IOException e) {
            System.out.println("Invalid permissions.");
        }
    }


    public TimeTable readDataFromFile(File newfile) {
        return solutionFileIO.read(newfile);
    }
}
