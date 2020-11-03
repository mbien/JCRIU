package dev.mbien.jcriu;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;

import static jdk.incubator.foreign.CLinker.*;

/**
 *
 * @author mbien
 */
public final class CRIUContext implements AutoCloseable {
    
    private static final ReentrantLock lock = new ReentrantLock();
    
    private String logFile = "jcriu.log";
    private int logLevel = 4;
    private boolean tcpEstablished;
    private boolean leaveRunning;
    
    private CRIUContext() {}
    
    public static CRIUContext create() {
        CRIUContext context = new CRIUContext();
        context.aquire();
        return context;
    }
    
    public void aquire() {
        lock.lock();
        criu_h.criu_init_opts();
    }

    @Override
    public void close() {
        try {
            criu_h.criu_free_opts();
        } finally {
            lock.unlock();
        }
    }
    
    public void dump(Path path) {
        
        criuAction(path, () -> {
            return criu_h.criu_dump();
        });
        
    }
    
    public void restore(Path path) {
        
        criuAction(path, () -> {
            return criu_h.criu_restore();
        });
        
    }
    
    private void criuAction(Path path, CRIUAction criu) {
        
        if(!Files.isDirectory(path)) {
            throw new IllegalArgumentException("path '"+path+"' is not a folder");
        }
        
        try(MemorySegment freezer = CLinker.toCString(path.toString());
            MemorySegment logfile = CLinker.toCString(logFile)) {

            // options
            criu_h.criu_set_log_level(logLevel);
            criu_h.criu_set_log_file(logfile);
            
            criu_h.criu_set_shell_job(TRUE);
            criu_h.criu_set_leave_running(bool(leaveRunning));
//            criu_h.criu_set_file_locks(TRUE);
//            criu_h.criu_set_ext_unix_sk(TRUE);
            criu_h.criu_set_tcp_established(bool(tcpEstablished));

            // image location
            try {
                int fd = (int) open.invoke(freezer.address(), O_DIRECTORY);
                criu_h.criu_set_images_dir_fd(fd);
            } catch (Throwable t) {
                throw new RuntimeException("can't create file handle for folder: " + path, t);
            }
            
            int ret = criu.execute();
            
            if(ret < 0) {
                throw new RuntimeException("criu error code: " + ret);
            }        
        }
        
    }

    private byte bool(boolean b) {
        return b ? TRUE : FALSE;
    }
    
    private static interface CRIUAction {
        public int execute();
    }
    
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

    // fcntl.h open(const char *__file, int __oflag, ...)
    private static final MethodHandle open = CLinker.getInstance().downcallHandle(
        LibraryLookup.ofDefault().lookup("open").get(),
        MethodType.methodType(int.class, MemoryAddress.class, int.class),
        FunctionDescriptor.of(C_INT, C_POINTER, C_INT)
    );
    private static final int O_DIRECTORY = 65536;
    
    private static final byte TRUE = (byte)1;
    private static final byte FALSE = (byte)0;
    
}
