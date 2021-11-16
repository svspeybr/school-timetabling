package org.acme.timetabling.rest;

import org.acme.timetabling.domain.Room;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class RoomResource {

    // Add room??
    @POST
    public Response add(Room room) {
        Room.persist(room);
        return Response.accepted(room).build();
    }
    @DELETE
    @Path("{roomId}")
    public Response delete(@PathParam("roomId") Long roomId) {
        Room room = Room.findById(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        room.delete();
        return Response.status(Response.Status.OK).build();
    }
}
