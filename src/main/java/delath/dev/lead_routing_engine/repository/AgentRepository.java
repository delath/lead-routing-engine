package delath.dev.lead_routing_engine.repository;

import delath.dev.lead_routing_engine.domain.Agent;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AgentRepository extends JpaRepository<Agent, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Agent a WHERE a.city = :city")
    List<Agent> findAgentsByCityWithLock(@Param("city") String city);
}
