package delath.dev.lead_routing_engine.service;

import delath.dev.lead_routing_engine.domain.Agent;
import delath.dev.lead_routing_engine.domain.Lead;
import delath.dev.lead_routing_engine.repository.AgentRepository;
import delath.dev.lead_routing_engine.repository.LeadRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class RoutingService {

    private static final int MAX_LEADS_PER_24H = 5;

    private final AgentRepository agentRepository;
    private final LeadRepository leadRepository;

    @Transactional
    public Lead ingestAndAssign(String clientName, String propertyId, String city) {
        var agents = agentRepository.findAgentsByCityWithLock(city);

        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

        Agent selectedAgent = agents.stream()
                .filter(agent -> hasCapacity(agent, twentyFourHoursAgo))
                .min(Comparator.comparingInt(agent -> getLeadsLast24h(agent, twentyFourHoursAgo)))
                .orElseThrow(() -> new RuntimeException("No agents available in " + city));

        Lead newLead = new Lead(clientName, propertyId, city);
        newLead.setAssignedAgent(selectedAgent);
        Lead savedLead = leadRepository.save(newLead);

        System.out.println("Notifying Agent: " + selectedAgent.getName());

        return savedLead;
    }

    /**
     * Checks if an agent has capacity to receive more leads.
     * An agent can have a maximum of MAX_LEADS_PER_24H leads assigned in the last 24 hours.
     */
    private boolean hasCapacity(Agent agent, LocalDateTime since) {
        return getLeadsLast24h(agent, since) < MAX_LEADS_PER_24H;
    }

    /**
     * Returns the number of leads assigned to an agent since the given timestamp.
     */
    private int getLeadsLast24h(Agent agent, LocalDateTime since) {
        return leadRepository.countByAssignedAgentAndCreatedAtAfter(agent, since);
    }
}
