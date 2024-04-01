package com.kukuxer.registration.domain.match;

import com.kukuxer.registration.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "match_history")
@Builder
public class MatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int moveNumber;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "board")
    private String board; //


    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    @Column(name = "move_timestamp")
    private LocalDateTime moveTimestamp;
}
