package delath.dev.lead_routing_engine.service;

import delath.dev.lead_routing_engine.domain.Agent;
import delath.dev.lead_routing_engine.domain.Lead;
import delath.dev.lead_routing_engine.repository.AgentRepository;
import delath.dev.lead_routing_engine.repository.LeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutingServiceTest {

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private LeadRepository leadRepository;

    @InjectMocks
    private RoutingService routingService;

    private Agent agent1;
    private Agent agent2;

    @BeforeEach
    void setUp() {
        agent1 = new Agent();
        agent1.setId(1L);
        agent1.setName("Agent One");
        agent1.setCity("Milano");

        agent2 = new Agent();
        agent2.setId(2L);
        agent2.setName("Agent Two");
        agent2.setCity("Milano");
    }

    @Test
    void shouldAssignLeadToAvailableAgent() {
        // Given
        when(agentRepository.findAgentsByCityWithLock("Milano")).thenReturn(List.of(agent1));
        when(leadRepository.countByAssignedAgentAndCreatedAtAfter(eq(agent1), any(LocalDateTime.class))).thenReturn(0);
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> {
            Lead lead = invocation.getArgument(0);
            lead.setId(1L);
            return lead;
        });

        // When
        Lead result = routingService.ingestAndAssign("Mario Rossi", "PROP-001", "Milano");

        // Then
        assertNotNull(result);
        assertEquals("Mario Rossi", result.getClientName());
        assertEquals("PROP-001", result.getPropertyId());
        assertEquals("Milano", result.getCity());
        assertEquals(agent1, result.getAssignedAgent());
        verify(leadRepository).save(any(Lead.class));
    }

    @Test
    void shouldAssignLeadToAgentWithLeastLoad() {
        // Given
        when(agentRepository.findAgentsByCityWithLock("Milano")).thenReturn(List.of(agent1, agent2));
        // Agent1 has 3 leads, Agent2 has 1 lead
        when(leadRepository.countByAssignedAgentAndCreatedAtAfter(eq(agent1), any(LocalDateTime.class))).thenReturn(3);
        when(leadRepository.countByAssignedAgentAndCreatedAtAfter(eq(agent2), any(LocalDateTime.class))).thenReturn(1);
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> {
            Lead lead = invocation.getArgument(0);
            lead.setId(1L);
            return lead;
        });

        // When
        Lead result = routingService.ingestAndAssign("Luigi Verdi", "PROP-002", "Milano");

        // Then
        assertNotNull(result);
        assertEquals(agent2, result.getAssignedAgent()); // Agent2 should be selected (least loaded)
    }

    @Test
    void shouldThrowExceptionWhenNoAgentsInCity() {
        // Given
        when(agentRepository.findAgentsByCityWithLock("Roma")).thenReturn(Collections.emptyList());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                routingService.ingestAndAssign("Paolo Bianchi", "PROP-003", "Roma")
        );
        assertEquals("No agents available in Roma", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAllAgentsAtCapacity() {
        // Given
        when(agentRepository.findAgentsByCityWithLock("Milano")).thenReturn(List.of(agent1, agent2));
        // Both agents have 5 leads (at capacity)
        when(leadRepository.countByAssignedAgentAndCreatedAtAfter(eq(agent1), any(LocalDateTime.class))).thenReturn(5);
        when(leadRepository.countByAssignedAgentAndCreatedAtAfter(eq(agent2), any(LocalDateTime.class))).thenReturn(5);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                routingService.ingestAndAssign("Anna Neri", "PROP-004", "Milano")
        );
        assertEquals("No agents available in Milano", exception.getMessage());
    }

    @Test
    void shouldNotAssignToAgentAtCapacity() {
        // Given
        when(agentRepository.findAgentsByCityWithLock("Milano")).thenReturn(List.of(agent1, agent2));
        // Agent1 is at capacity (5 leads), Agent2 has 4 leads
        when(leadRepository.countByAssignedAgentAndCreatedAtAfter(eq(agent1), any(LocalDateTime.class))).thenReturn(5);
        when(leadRepository.countByAssignedAgentAndCreatedAtAfter(eq(agent2), any(LocalDateTime.class))).thenReturn(4);
        when(leadRepository.save(any(Lead.class))).thenAnswer(invocation -> {
            Lead lead = invocation.getArgument(0);
            lead.setId(1L);
            return lead;
        });

        // When
        Lead result = routingService.ingestAndAssign("Carla Gialli", "PROP-005", "Milano");

        // Then
        assertNotNull(result);
        assertEquals(agent2, result.getAssignedAgent()); // Agent2 should be selected (Agent1 is at capacity)
    }
}
