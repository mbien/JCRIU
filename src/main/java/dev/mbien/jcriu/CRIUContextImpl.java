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
public final class CRIUContextImpl extends CRIUContext {
    
    private static final ReentrantLock lock = new ReentrantLock();
    
    CRIUContextImpl() {}
    
    
    @Override
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
    
    @Override
    public void dump(Path path) {
        
        criuAction(path, () -> {
            return criu_h.criu_dump();
        });
        
    }
    
    @Override
    public void restore(Path path) {
        
        criuAction(path, () -> {
            return criu_h.criu_restore();
        });
        
    }
    
    private void criuAction(Path path, CRIUAction criu) {
        
        if(!Files.isDirectory(path) || !Files.isReadable(path)) {
            throw new IllegalArgumentException("'"+path+"' is not a directory or can't be accessed");
        }
        
        try(MemorySegment imageDir = CLinker.toCString(path.toString());
            MemorySegment logfile = CLinker.toCString(logFile)) {

            // options
            criu_h.criu_set_log_level(logLevel.ordinal());
            criu_h.criu_set_log_file(logfile);
            
            criu_h.criu_set_shell_job(bool(shellJob));
            criu_h.criu_set_leave_running(bool(leaveRunning));
//            criu_h.criu_set_file_locks(TRUE);
//            criu_h.criu_set_ext_unix_sk(TRUE);
            criu_h.criu_set_tcp_established(bool(tcpEstablished));

            // image location
            try {
                int fd = (int) open.invoke(imageDir.address(), O_DIRECTORY);
                if(fd < 0) {
                    throw new RuntimeException("can't open image directory"); // should never happen
                }
                criu_h.criu_set_images_dir_fd(fd);
            } catch (Throwable t) {
                throw new RuntimeException("can't create file handle for directory: " + path, t);
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

    // fcntl.h open(const char *__file, int __oflag, ...)
    private static final MethodHandle open = CLinker.getInstance().downcallHandle(
        LibraryLookup.ofDefault().lookup("open").get(),
        MethodType.methodType(int.class, MemoryAddress.class, int.class),
        FunctionDescriptor.of(C_INT, C_POINTER, C_INT)
    );
    private static final int O_DIRECTORY = 0200000;
    
    private static final byte TRUE = (byte)1;
    private static final byte FALSE = (byte)0;
    
}
