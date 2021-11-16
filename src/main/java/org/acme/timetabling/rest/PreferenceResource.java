package org.acme.timetabling.rest;


import org.acme.timetabling.domain.Preference;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/preferences")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public class PreferenceResource {

        @GET
        public List<Preference> get() {
            return Preference.listAll();
        }

        @POST
        public Response add(Preference preference) {
            Preference.persist(preference);
            return Response.accepted(preference).build();
        }
        @DELETE
        @Path("{preferenceId}")
        public Response delete(@PathParam("preferenceId") Long preferenceId) {
            Preference preference = Preference.findById(preferenceId);
            if (preference == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            preference.delete();
            return Response.status(Response.Status.OK).build();
        }
    }
