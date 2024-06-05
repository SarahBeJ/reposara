package tn.esprit.eventsproject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;
import tn.esprit.eventsproject.services.EventServicesImpl;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServicesImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private LogisticsRepository logisticsRepository;

    @InjectMocks
    private EventServicesImpl eventServices;

    private Participant participant;
    private Event event;
    private Logistics logistics;

    @BeforeEach
    void setUp() {
        participant = new Participant();
        participant.setIdPart(1);
        participant.setNom("Test");
        participant.setPrenom("Participant");

        event = new Event();
        event.setIdEvent(1);
        event.setDescription("Test Event");
        event.setParticipants(new HashSet<>());

        logistics = new Logistics();
        logistics.setIdLog(1);
        logistics.setDescription("Test Logistics");
        logistics.setReserve(true);
        logistics.setPrixUnit(100);
        logistics.setQuantite(5);
    }

    @Test
    void addParticipant() {
        when(participantRepository.save(participant)).thenReturn(participant);
        Participant savedParticipant = eventServices.addParticipant(participant);
        assertEquals(participant, savedParticipant);
        verify(participantRepository, times(1)).save(participant);
    }

    @Test
    void addAffectEvenParticipant_ById() {
        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(eventRepository.save(event)).thenReturn(event);

        Event savedEvent = eventServices.addAffectEvenParticipant(event, 1);

        assertNotNull(participant.getEvents()); // Ensure participant's events are not null
        assertTrue(participant.getEvents().contains(event));
        assertEquals(event, savedEvent);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void addAffectEvenParticipant_ByParticipants() {
        Set<Participant> participants = new HashSet<>();
        participants.add(participant);
        event.setParticipants(participants);

        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(eventRepository.save(event)).thenReturn(event);

        Event savedEvent = eventServices.addAffectEvenParticipant(event);

        assertNotNull(participant.getEvents()); // Ensure participant's events are not null
        assertTrue(participant.getEvents().contains(event));
        assertEquals(event, savedEvent);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void addAffectLog() {
        when(eventRepository.findByDescription("Test Event")).thenReturn(event);
        when(logisticsRepository.save(logistics)).thenReturn(logistics);

        Logistics savedLogistics = eventServices.addAffectLog(logistics, "Test Event");

        assertNotNull(event.getLogistics()); // Ensure event's logistics are not null
        assertTrue(event.getLogistics().contains(logistics));
        assertEquals(logistics, savedLogistics);
        verify(logisticsRepository, times(1)).save(logistics);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void getLogisticsDates() {
        event.setLogistics(new HashSet<>(Collections.singletonList(logistics)));
        List<Event> events = Collections.singletonList(event);

        when(eventRepository.findByDateDebutBetween(LocalDate.now().minusDays(1), LocalDate.now())).thenReturn(events);

        List<Logistics> logisticsList = eventServices.getLogisticsDates(LocalDate.now().minusDays(1), LocalDate.now());

        assertNotNull(logisticsList); // Ensure logisticsList is not null
        assertEquals(1, logisticsList.size());
        assertTrue(logisticsList.contains(logistics));
    }

    @Test
    void calculCout() {
        participant.setTache(Tache.ORGANISATEUR); // Ensure participant has the correct task
        event.setParticipants(new HashSet<>(Collections.singletonList(participant)));
        event.setLogistics(new HashSet<>(Collections.singletonList(logistics)));
        List<Event> events = Collections.singletonList(event);

        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache("Tounsi", "Ahmed", Tache.ORGANISATEUR)).thenReturn(events);

        eventServices.calculCout();

        assertEquals(500, event.getCout());
        verify(eventRepository, times(1)).save(event);
    }
}
