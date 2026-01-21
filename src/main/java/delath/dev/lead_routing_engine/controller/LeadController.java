package delath.dev.lead_routing_engine.controller;

import delath.dev.lead_routing_engine.domain.Lead;
import delath.dev.lead_routing_engine.service.RoutingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/leads")
@RequiredArgsConstructor
public class LeadController {

    private final RoutingService routingService;

    @PostMapping
    public ResponseEntity<Lead> createLead(@RequestBody LeadRequest request) {
        Lead lead = routingService.ingestAndAssign(
                request.name, request.propertyId, request.city
        );
        return ResponseEntity.ok(lead);
    }

    @Data
    public static class LeadRequest {
        public String name;
        public String email;
        public String phone;
        public String propertyId;
        public String city;
    }
}
