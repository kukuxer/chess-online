package com.kukuxer.registration.domain;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveRequest {
    private String board;
    private int finishResult;
}
