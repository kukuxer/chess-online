package com.kukuxer.registration.domain.match;

import com.kukuxer.registration.domain.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "match_history")
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
