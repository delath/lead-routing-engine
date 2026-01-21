package delath.dev.lead_routing_engine.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Lead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientName;
    private String propertyId;
    private String city;

    @ManyToOne
    @JoinColumn(name = "agent_id")
    private Agent assignedAgent;

    private LocalDateTime createdAt;

    public Lead(String clientName, String propertyId, String city) {
        this.clientName = clientName;
        this.propertyId = propertyId;
        this.city = city;
        this.createdAt = LocalDateTime.now();
    }
}
