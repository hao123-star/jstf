package com.qa.jstf.agent.utils.lcmd;

import java.io.IOException;

public interface LocalCommandExecutor {
    ExecutedResult executeCommand(String command, long timeout);

    Process executeCommand(String command) throws IOException;
}
