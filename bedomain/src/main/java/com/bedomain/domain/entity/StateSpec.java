package com.bedomain.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "state_specs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StateSpec {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StateType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_machine_id", nullable = false)
    private StateMachine stateMachine;

    public enum StateType {
        INITIAL,
        FINAL,
        INTERMEDIATE
    }
}
