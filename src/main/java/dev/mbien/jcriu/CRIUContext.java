package dev.mbien.jcriu;

import java.nio.file.Path;

/**
 *
 * @author mbien
 */
public abstract class CRIUContext implements AutoCloseable {

    protected String logFile = "jcriu.log";
    protected int logLevel = 4;
    protected boolean tcpEstablished;
    protected boolean leaveRunning;
    protected boolean shellJob;
    
    public static CRIUContext create() {
        CRIUContext context = new CRIUContextImpl();
//        CRIUContext context = new CRIUContextFallback();
        context.aquire();
        return context;
    }
    
    public abstract void aquire();

    public abstract void dump(Path path);

    public abstract void restore(Path path);

    @Override
    public abstract void close();
    
    public CRIUContext logFile(String file) {
        this.logFile = file;
        return this;
    }
    
    public CRIUContext logLevel(int level) {
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
}
