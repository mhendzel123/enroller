package com.company.enroller.controllers;

import com.company.enroller.model.Meeting;
import com.company.enroller.model.Participant;
import com.company.enroller.persistence.MeetingService;
import com.company.enroller.persistence.ParticipantService;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/meetings")
public class MeetingRestController {

    @Autowired
    MeetingService meetingService;

    @Autowired
    ParticipantService participantService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getMeetings() {
        Collection<Meeting> meetings = meetingService.getAll();
        return new ResponseEntity<Collection<Meeting>>(meetings, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getMeeting(@PathVariable("id") long meetingId) {
        Meeting meeting = meetingService.findById(meetingId);
        if (meeting == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<?> addMeeting(@RequestBody Meeting meeting) {
        Meeting foundMeeting = meetingService.findById(meeting.getId());
        if (foundMeeting != null) {
            return new ResponseEntity("Cannot create. Meeting with ID: " + meeting.getId() + " already exists.", HttpStatus.CONFLICT);
        }
        meetingService.add(meeting);
        return new ResponseEntity<Meeting>(meeting, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{Id}/participants", method = RequestMethod.POST)
    public ResponseEntity<?> addParticipantToMeeting(@PathVariable("Id") long meetingId, @RequestBody Participant participant) {
        Meeting meeting = meetingService.findById(meetingId);
        if (meeting == null) {
            return new ResponseEntity<String>("Meeting with ID: " + meetingId + " doesn't exist.", HttpStatus.NOT_FOUND);
        }
        Participant existingParticipant = participantService.findByLogin(participant.getLogin());
        if (existingParticipant == null) {
            return new ResponseEntity<String>("Participant already exists.", HttpStatus.NOT_FOUND);
        }
        if (meeting.getParticipants().contains(participant)) {
            return new ResponseEntity<String>("Participant " + existingParticipant.getLogin() + "already added to meeting with ID: " + meeting.getId(), HttpStatus.CONFLICT);
        }
        meeting.addParticipant(participant);
        meetingService.update(meeting);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{Id}/participants", method = RequestMethod.GET)
    public ResponseEntity<?> getParticipants(@PathVariable("Id") long meetingId) {
        Meeting meeting = meetingService.findById(meetingId);
        if (meeting == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Collection<Participant> participants = meeting.getParticipants();
        return new ResponseEntity<Collection<Participant>>(participants, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteMeeting(@PathVariable("id") long meetingId) {
        Meeting meeting = meetingService.findById(meetingId);
        if (meeting == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        meetingService.delete(meeting);
        return new ResponseEntity<Meeting>(HttpStatus.GONE);
    }
    
    @RequestMapping(value = "/{meetingId}/participants/{login}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteParticipantFormMeeting(@PathVariable("meetingId") long meetingId, @PathVariable("login") String login){
        Meeting meeting = meetingService.findById(meetingId);
        if (meeting==null){
        return new ResponseEntity<String> ("Participant not added to meeting with ID: " + meetingId + ".", HttpStatus.NOT_FOUND);
        }
        Participant foundParticipant = participantService.findByLogin(login);
        if (foundParticipant == null) {
            return new ResponseEntity<String>("Participant doesn't exist.", HttpStatus.NOT_FOUND);
        }
        meeting.removeParticipant(foundParticipant);
        meetingService.update(meeting);
        return new ResponseEntity<Participant>(foundParticipant, HttpStatus.OK);
    }

}