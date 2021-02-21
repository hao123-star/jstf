package com.qa.jstf.agent.utils.lcmd;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.concurrent.*;

@Slf4j
@Data
@Component
public class LocalCommandExecutorImpl implements LocalCommandExecutor {
    static ExecutorService pool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 3L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());

    public ExecutedResult executeCommand(String command, long timeout) {
        Process process = null;
        InputStream pIn = null;
        InputStream pErr = null;
        StreamGobbler outputGobbler = null;
        StreamGobbler errorGobbler = null;
        Future<Integer> executeFuture = null;

        try {
            log.info(command.toString());

            String os = System.getProperty("os.name");
            String[] commands = null;

            if ("linux".equals(os.toLowerCase()) || "unix".equals(os.toLowerCase()) || os.toLowerCase().startsWith("mac")) {
                commands = new String[]{"/bin/sh", "-c", command};
            } else {
                commands = new String[]{"cmd", "/c", command};
            }

            process = Runtime.getRuntime().exec(commands);

            final Process p = process;

            // close process's output stream.
            p.getOutputStream().close();

            pIn = p.getInputStream();

            outputGobbler = new StreamGobbler(pIn, "OUTPUT");
            outputGobbler.start();

            pErr = p.getErrorStream();
            errorGobbler = new StreamGobbler(pErr, "ERROR");
            errorGobbler.start();

            // create a Callable for the command's Process which can be called by an Executor
            Callable<Integer> call = new Callable<Integer>() {
                public Integer call() throws Exception {
                    p.waitFor();
                    return p.exitValue();
                }
            };

            // submit the command's call and get the result from a
            executeFuture = pool.submit(call);

            int exitCode = executeFuture.get(timeout, TimeUnit.MILLISECONDS);
            return new ExecutedResult(exitCode, outputGobbler.getContent());
        } catch (IOException ex) {
            String errorMessage = "The command [" + command + "] launch failed.";
            log.error(errorMessage, ex);
            return new ExecutedResult(-1, null);
        } catch (TimeoutException ex) {
            String errorMessage = "The command [" + command + "] timed out.";
            log.error(errorMessage, ex);
            return new ExecutedResult(-1, null);
        } catch (ExecutionException ex) {
            String errorMessage = "The command [" + command + "] did not complete due to an execution error.";
            log.error(errorMessage, ex);
            return new ExecutedResult(-1, null);
        } catch (InterruptedException ex) {
            String errorMessage = "The command [" + command + "] did not complete due to an interrupted error.";
            log.error(errorMessage, ex);
            return new ExecutedResult(-1, null);
        } finally {
            if (executeFuture != null) {
                try {
                    executeFuture.cancel(true);
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                }
            }
            if (pIn != null) {
                this.closeQuietly(pIn);
                if (outputGobbler != null && !outputGobbler.isInterrupted()) {
                    outputGobbler.interrupt();
                }
            }
            if (pErr != null) {
                this.closeQuietly(pErr);
                if (errorGobbler != null && !errorGobbler.isInterrupted()) {
                    errorGobbler.interrupt();
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
    }

    @Override
    public Process executeCommand(String command) throws IOException {
        log.info(command);
        String os = System.getProperty("os.name");
        String[] commands = null;

        if ("linux".equals(os.toLowerCase()) || "unix".equals(os.toLowerCase()) || os.toLowerCase().startsWith("mac")) {
            commands = new String[]{"/bin/sh", "-c", command};
        } else {
            commands = new String[]{"cmd", "/c", command};
        }

        return Runtime.getRuntime().exec(commands);
    }

    private void closeQuietly(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (IOException e) {
            log.error("exception", e);
        }
    }

    public static void main(String[] args) {
//        String cmd = "/Applications/Xcode.app/Contents/Developer/usr/bin/xcodebuild -project /Users/ted/openstf/Appium-WebDriverAgent/WebDriverAgent.xcodeproj -scheme WebDriverAgentRunner -destination id=a83d2b9bc137837fa5d7c7bc50656a1db2e3d376 test";
        String cmd = "iproxy 9100 9100";

        LocalCommandExecutorImpl localCommandExecutor = new LocalCommandExecutorImpl();
        try {
            Process process = localCommandExecutor.executeCommand(cmd);
            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "GBK");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
