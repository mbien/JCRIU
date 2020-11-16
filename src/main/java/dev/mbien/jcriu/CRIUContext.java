package dev.mbien.jcriu;

import java.nio.file.Path;

/**
 *
 * @author mbien
 */
public abstract class CRIUContext implements AutoCloseable {
    
    /**
     * Log level used for CRIU log files.
     */
    public enum LOG_LEVEL {OFF, ERROR, WARNING, INFO, DEBUG} // order matters: 0=off, 4=debug

    protected String logFile = "jcriu.log";
    protected LOG_LEVEL logLevel = LOG_LEVEL.DEBUG;
    protected boolean tcpEstablished;
    protected boolean leaveRunning;
    protected boolean shellJob;
    
    public static CRIUContext create() {
        return new CRIUContextImpl();
    }
    
    /**
     * Creates a checkpoint by dumping the state of this JVM into an image.
     * @param path Path to the directory the image will be written to.
     * @throws CRIUException When CRIU returns an error.
     */
    public abstract void checkpoint(Path path) throws CRIUException;

    /**
     * Restores a process from an image.
     * @param path Path to the image directory.
     * @throws CRIUException When CRIU returns an error.
     */
    public abstract void restore(Path path) throws CRIUException;
    
    /**
     * Returns the version String of the underlying CRIU implementation.
     */
    public abstract String getVersion();

    /**
     * Releases all resources associated with this context.
     * @see AutoCloseable
     */
    @Override
    public abstract void close();
    
    public CRIUContext logFile(String file) {
        this.logFile = file;
        return this;
    }
    
    public CRIUContext logLevel(LOG_LEVEL level) {
        this.logLevel = level;
        return this;
    }

    public CRIUContext leaveRunning(boolean b) {
        this.leaveRunning = b;
        return this;
    }
    
    public CRIUContext tcpEstablished(boolean b) {
        this.tcpEstablished = b;
        return this;
    }
    
    public CRIUContext shellJob(boolean b) {
        this.shellJob = b;
        return this;
    }

    public String getLogFile() {
        return logFile;
    }

    public LOG_LEVEL getLogLevel() {
        return logLevel;
    }

    public boolean isTcpEstablished() {
        return tcpEstablished;
    }

    public boolean isLeaveRunningEnabled() {
        return leaveRunning;
    }

    public boolean isShellJobEnabled() {
        return shellJob;
    }
    
}
