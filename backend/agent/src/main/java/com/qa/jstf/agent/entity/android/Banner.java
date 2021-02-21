package com.qa.jstf.agent.entity.android;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Banner {
    private int version;

    private int length;

    private int pid;

    private int readWidth;

    private int readHeight;

    private int virtualWidth;

    private int virtualHeight;

    private int orientation;

    private int quirks;
}
