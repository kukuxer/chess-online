package com.kukuxer.registration.domain.user;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kukuxer.registration.domain.match.Match;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email")
    private String email;

    @JsonIgnore
    @Column(name = "password")
    private String password;

    @Column(name = "username")
    private String username;

    @Column(name = "in_game")
    private boolean inGame;


    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "users_roles")
    @Column(name = "role")
    @Enumerated(value = EnumType.STRING)
    private Set<Role> roles;


    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_friends",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private List<User> friends;

//    @CollectionTable(name="users_matches")
//    @OneToMany
//    @JoinColumn(name="match_id")
//    private List<Match> matches;


}
