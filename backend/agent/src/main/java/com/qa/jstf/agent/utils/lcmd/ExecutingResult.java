package com.qa.jstf.agent.utils.lcmd;

import lombok.Data;
import lombok.ToString;

import java.io.InputStream;
import java.util.concurrent.Future;

@Data
@ToString
public class ExecutingResult {
    Process process;
    InputStream pIn;
    InputStream pErr;
    StreamGobbler outputGobbler;
    StreamGobbler errorGobbler;
    Future<Integer> executeFuture;

    public ExecutingResult(Process process, InputStream pIn, InputStream pErr, StreamGobbler outputGobbler, StreamGobbler errorGobbler) {
    }

}
