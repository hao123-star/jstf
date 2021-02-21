package com.qa.jstf.agent.entity.android;

import lombok.*;

import java.io.InputStream;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RotationWatcher {
    Integer rotation;

    // 接受流
    InputStream inputStream;

    boolean started;
}
