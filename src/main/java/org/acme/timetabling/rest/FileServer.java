package org.acme.timetabling.rest;

import org.acme.timetabling.persistence.XmlFileIO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Path("/fm")
public class FileServer {

    private final String PATHNAME = "/home/svs/IdeaProjects/school-timetabling/data/unsolved";

    public String getPathNameFiles(){
        return PATHNAME;
    }

    @GET
    @Path("download/{fileName}/xml")
    public Response getFileInXmlForm(@PathParam("fileName") String fileName){

        if (fileName == null || fileName.isEmpty()){
            Response.ResponseBuilder response = Response.status(Response.Status.BAD_REQUEST);
            return response.build();
        }

        File file;
        String fn = "/" + fileName + ".xml";

        if (! Files.exists(Paths.get(PATHNAME + fn))){
            XmlFileIO generator = new XmlFileIO(PATHNAME, fileName);
            generator.saveDataToFile();
            file = generator.getFile();
        } else {
            file = new File(PATHNAME + fn); //Read existing file
        }

        Response.ResponseBuilder response = Response.ok((Object) file, MediaType.TEXT_XML);
        return response.build();
    }

    @GET
    @Path("load")
    public List<String> loadFiles(){
        List<String> fileNames = new ArrayList<>();
        final File folder = new File(PATHNAME);
        File[] docs = folder.listFiles();

        if (docs != null) {
            for (final File fileEntry : docs) {
                fileNames.add(fileEntry.getName().replaceFirst("[.][^.]+$", ""));
            }
        }
        return fileNames;
    }

    @GET
    @Path("loadFile")
    public List<String> loadFile(){
        List<String> fileName = new ArrayList<>(1);
        final File folder = new File(PATHNAME);
        File[] docs = folder.listFiles();

        if (docs != null) {
            Optional<File> fileEntry = Arrays.stream(docs).findFirst();
            fileEntry.ifPresent(file -> fileName.add(file.getName().replaceFirst("[.][^.]+$", "")));
        }
        return fileName;
    }

    @POST
    @Path("save/{fileName}/xml")
    public Response saveFileInXmlForm(@PathParam("fileName") String fileName){

        if (fileName == null || fileName.isEmpty()){
            Response.ResponseBuilder response = Response.status(Response.Status.BAD_REQUEST);
            return response.build();
        }

        XmlFileIO generator = new XmlFileIO(PATHNAME, fileName);
        generator.saveDataToFile();

        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Path("copy/{fileName}/xml")
    public Response copyXmlFile(@PathParam("fileName") String fileName){
        if (fileName == null || fileName.isEmpty()){
            Response.ResponseBuilder response = Response.status(Response.Status.BAD_REQUEST);
            return response.build();
        }

        XmlFileIO generator = new XmlFileIO(PATHNAME, fileName);
        generator.saveDataToFile();

        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Path("delete/{fileName}/xml")
    public Response deleteXmlFile(@PathParam("fileName") String fileName){

        if (fileName == null || fileName.isEmpty()){
            Response.ResponseBuilder response = Response.status(Response.Status.BAD_REQUEST);
            return response.build();
        }

        XmlFileIO generator = new XmlFileIO(PATHNAME, fileName);
        generator.deleteXmlFile();

        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Path("send/{message}")
    public void showVariableInShell(@PathParam("message") String message){
        if (message == null){
            System.out.println("Received null message");
        } else {
            System.out.println(message);
        }

        //return Response.status(Response.Status.OK).build();
    }
}
