package com.bedomain.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "transition_specs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransitionSpec {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String event;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_machine_id", nullable = false)
    private StateMachine stateMachine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_state_id", nullable = false)
    private StateSpec fromState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_state_id", nullable = false)
    private StateSpec toState;
}
