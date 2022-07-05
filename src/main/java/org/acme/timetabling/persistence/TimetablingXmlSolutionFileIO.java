package org.acme.timetabling.persistence;

import org.acme.timetabling.domain.TimeTable;
import org.optaplanner.persistence.xstream.impl.domain.solution.XStreamSolutionFileIO;

import javax.transaction.Transactional;

public class TimetablingXmlSolutionFileIO extends XStreamSolutionFileIO<TimeTable> {

    public TimetablingXmlSolutionFileIO(){
        super(TimeTable.class);
    }
}
