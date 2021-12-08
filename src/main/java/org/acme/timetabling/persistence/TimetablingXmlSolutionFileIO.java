package org.acme.timetabling.persistence;

import org.acme.timetabling.domain.TimeTable;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;

public class TimetablingXmlSolutionFileIO extends XStreamSolutionFileIO<TimeTable> {

    public TimetablingXmlSolutionFileIO(){
        super(TimeTable.class);
    }
}
