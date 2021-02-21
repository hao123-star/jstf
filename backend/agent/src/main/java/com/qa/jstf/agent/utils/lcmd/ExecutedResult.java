package com.qa.jstf.agent.utils.lcmd;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@Builder
public class ExecutedResult {
    private int exitCode;
    private String executeOut;

    public ExecutedResult(int exitCode, String executeOut) {
        this.exitCode = exitCode;
        this.executeOut = executeOut;
    }

}
