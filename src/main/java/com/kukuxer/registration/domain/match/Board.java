package com.kukuxer.registration.domain.match;

import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Board {
    private Long matchId;

    private String WhiteUsername;
    private String BlackUsername;

    private int[][] board;

    private int lastMoveNumber;

}
