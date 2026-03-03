package com.bedomain.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "state_machines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StateMachine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_type_id", nullable = false)
    private EntityType entityType;

    @OneToMany(mappedBy = "stateMachine", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StateSpec> states = new ArrayList<>();

    @OneToMany(mappedBy = "stateMachine", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransitionSpec> transitions = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addState(StateSpec state) {
        states.add(state);
        state.setStateMachine(this);
    }

    public void removeState(StateSpec state) {
        states.remove(state);
        state.setStateMachine(null);
    }

    public void addTransition(TransitionSpec transition) {
        transitions.add(transition);
        transition.setStateMachine(this);
    }

    public void removeTransition(TransitionSpec transition) {
        transitions.remove(transition);
        transition.setStateMachine(null);
    }
}
