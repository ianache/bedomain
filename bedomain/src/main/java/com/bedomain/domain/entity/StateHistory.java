package com.bedomain.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "state_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StateHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_instance_id", nullable = false)
    private EntityInstance entityInstance;

    @Column(name = "from_state")
    private String fromState;

    @Column(name = "to_state")
    private String toState;

    @Column(nullable = false)
    private String event;

    @Column(name = "triggered_by")
    private String triggeredBy;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(name = "hook_executed", nullable = false)
    @Builder.Default
    private boolean hookExecuted = false;

    @Column(name = "hook_type")
    private String hookType; // "onEnter" or "onExit"

    @Column(name = "hook_script_hash")
    private String hookScriptHash; // SHA-256 of script for audit

    @Column(name = "hook_error")
    private String hookError; // Error message if hook failed

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}
