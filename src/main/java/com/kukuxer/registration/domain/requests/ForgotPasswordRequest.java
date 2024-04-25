package com.kukuxer.registration.domain.requests;

import com.kukuxer.registration.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "forgot_password_request")
public class ForgotPasswordRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String ipAddress;

    private String token;

    private boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
