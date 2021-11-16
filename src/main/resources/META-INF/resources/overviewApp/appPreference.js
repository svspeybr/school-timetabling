var autoRefreshIntervalId = null;

function refreshPreferenceOverview() {
    $.getJSON("/preferences", function(preferenceList) {

        const preferenceOverview = $("#preferenceOverview");
        preferenceOverview.children().remove();

        const theadPreferenceOverview = $("<thead>").appendTo(preferenceOverview);
        const headerRowPreferenceOverview = $("<tr>").appendTo(theadPreferenceOverview);
        headerRowPreferenceOverview.append($("<th>Teacher</th>"));
        headerRowPreferenceOverview
                    .append($("<th/>")
                    .append($("<span/>").text("Day0fWeek")))
        headerRowPreferenceOverview
                            .append($("<th/>")
                            .append($("<span/>").text("Timeslot")))

        const tbodyPreferenceOverview = $("<tbody>").appendTo(preferenceOverview);
        $.each(preferenceList, (index, preference) => {
            const rowPreferenceOverview = $("<tr>").appendTo(tbodyPreferenceOverview);
            rowPreferenceOverview
                .append($(`<th class="align-middle"/>`)
                    .append($("<span/>").text(`
                        ${preference.teacher.acronym}
                    `)));
            rowPreferenceOverview
                 .append($(`<th class="align-middle"/>`)
                    .append($("<span/>").text(`
                        ${preference.timeslot.dayOfWeek.charAt(0) + preference.timeslot.dayOfWeek.slice(1).toLowerCase()}
                    `)));
            rowPreferenceOverview
                .append($(`<th class="align-middle"/>`)
                .append($("<span/>").text(`
                    ${moment(preference.timeslot.startTime, "HH:mm:ss").format("HH:mm")}
                        -
                    ${moment(preference.timeslot.endTime, "HH:mm:ss").format("HH:mm")}
                `)));
                      /*  .append($(`<button type="button" class="ml-2 mb-1 btn btn-light btn-sm p-1"/>`)
                            .append($(`<small class="fas fa-trash"/>`)).click(() => deleteTimeslot(timeslot)))));*/

    });
}).fail(function(xhr, ajaxOptions, thrownError) {
         showError("Start solving failed.", xhr);
     });
}

function convertToId(str) {
    // Base64 encoding without padding to avoid XSS
    return btoa(str).replace(/=/g, "");
}

function addRoom() {
    var name = $("#room_name").val().trim();
    $.post("/rooms", JSON.stringify({
        "name": name
    }), function() {
        refreshTimeTable();
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Adding room (" + name + ") failed.", xhr);
    });
    $("#roomDialog").modal('toggle');
}

function deleteRoom(room) {
    $.delete("/rooms/" + room.id, function() {
        refreshTimeTable();
    }).fail(function(xhr, ajaxOptions, thrownError) {
        showError("Deleting room (" + room.name + ") failed.", xhr);
    });
}


function showError(title, xhr) {
    const serverErrorMessage = !xhr.responseJSON ? `${xhr.status}: ${xhr.statusText}` : xhr.responseJSON.message;
    console.error(title + "\n" + serverErrorMessage);
    const notification = $(`<div class="toast" role="alert" aria-live="assertive" aria-atomic="true" style="min-width: 30rem"/>`)
        .append($(`<div class="toast-header bg-danger">
                 <strong class="mr-auto text-dark">Error</strong>
                 <button type="button" class="ml-2 mb-1 close" data-dismiss="toast" aria-label="Close">
                   <span aria-hidden="true">&times;</span>
                 </button>
               </div>`))
        .append($(`<div class="toast-body"/>`)
            .append($(`<p/>`).text(title))
            .append($(`<pre/>`)
                .append($(`<code/>`).text(serverErrorMessage))
            )
        );
    $("#notificationPanel").append(notification);
    notification.toast({ delay: 30000 });
    notification.toast('show');
}

$(document).ready(function() {
    $.ajaxSetup({
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        }
    });
    // Extend jQuery to support $.put() and $.delete()
    jQuery.each(["put", "delete"], function(i, method) {
        jQuery[method] = function(url, data, callback, type) {
            if (jQuery.isFunction(data)) {
                type = type || callback;
                callback = data;
                data = undefined;
            }
            return jQuery.ajax({
                url: url,
                type: method,
                dataType: type,
                data: data,
                success: callback
            });
        };
    });

   refreshPreferenceOverview()
});