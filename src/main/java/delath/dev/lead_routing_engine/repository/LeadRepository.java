package delath.dev.lead_routing_engine.repository;

import delath.dev.lead_routing_engine.domain.Agent;
import delath.dev.lead_routing_engine.domain.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    
    /**
     * Counts the number of leads assigned to a specific agent since a given timestamp.
     * Used to check agent capacity (max 5 leads in the last 24 hours).
     */
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.assignedAgent = :agent AND l.createdAt >= :since")
    int countByAssignedAgentAndCreatedAtAfter(@Param("agent") Agent agent, @Param("since") LocalDateTime since);
}
