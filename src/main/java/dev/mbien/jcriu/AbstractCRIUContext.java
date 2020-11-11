package dev.mbien.jcriu;

import java.nio.file.Path;

/**
 *
 * @author mbien
 */
public abstract class AbstractCRIUContext implements AutoCloseable {

    protected String logFile = "jcriu.log";
    protected int logLevel = 4;
    protected boolean tcpEstablished;
    protected boolean leaveRunning;
    protected boolean shellJob;
    
    public static AbstractCRIUContext create() {
        AbstractCRIUContext context = new CRIUContextImpl();
//        CRIUContext context = new CRIUContextFallback();
        context.aquire();
        return context;
    }
    
    public abstract void aquire();

    public abstract void dump(Path path);

    public abstract void restore(Path path);

    @Override
    public abstract void close();
    
    public AbstractCRIUContext logFile(String file) {
        this.logFile = file;
        return this;
    }
    
    public AbstractCRIUContext logLevel(int level) {
        this.logLevel = level;
        return this;
    }

    public AbstractCRIUContext leaveRunning(boolean b) {
        this.leaveRunning = b;
        return this;
    }
    
    public AbstractCRIUContext tcpEstablished(boolean b) {
        this.tcpEstablished = b;
        return this;
    }
    
    public AbstractCRIUContext shellJob(boolean b) {
        this.shellJob = b;
        return this;
    }
}
