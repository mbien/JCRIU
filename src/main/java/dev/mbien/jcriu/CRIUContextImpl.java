package dev.mbien.jcriu;

import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;
import jdk.incubator.foreign.SegmentAllocator;

/**
 * Foreign function binding to the CRIU C-API.
 * Communication over RPC.
 * @author mbien
 */
public final class CRIUContextImpl extends CRIUContext {
    
    private static final ReentrantLock lock = new ReentrantLock();
    
    private static boolean initialized = false;
    
    CRIUContextImpl() {}
    
    private static void init() {
        if(!initialized) {
            int ret = criu_h.criu_init_opts();
            if(ret != 0) {
                throw new CRIUException(ret, "error during init");
            }
            initialized = true;
        }
    }
    
    @Override
    public void close() {
        lock.lock();
        try {
            criu_h.criu_free_opts();
            initialized = false;
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void checkpoint(Path path) throws CRIUException {
        lock.lock();
        try {
            criuAction(path, () -> criu_h.criu_dump());
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void restore(Path path) throws CRIUException {
        lock.lock();
        try {
            criuAction(path, () -> criu_h.criu_restore());
        } finally {
            lock.unlock();
        }
    }
    
    private void criuAction(Path path, CRIUAction action) throws CRIUException {
        
        init();

        if(!Files.isDirectory(path) || !Files.isReadable(path)) {
            throw new IllegalArgumentException("'"+path+"' is not a directory or can't be accessed");
        }
        
        try(ResourceScope scope = ResourceScope.newConfinedScope()) {

            SegmentAllocator sa = SegmentAllocator.nativeAllocator(scope);
            MemorySegment logfile = sa.allocateUtf8String(logFile);
            MemorySegment imageDir = sa.allocateUtf8String(path.toString());

            // options
            criu_h.criu_set_log_level(logLevel.ordinal());
            criu_h.criu_set_log_file(logfile);
            
            criu_h.criu_set_shell_job(shellJob);
            criu_h.criu_set_leave_running(leaveRunning);
//            criu_h.criu_set_file_locks(TRUE);
//            criu_h.criu_set_ext_unix_sk(TRUE);
            criu_h.criu_set_tcp_established(tcpEstablished);

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
            
            int ret = action.execute();
            
            if(ret < 0) {
                throw new CRIUException(ret, "CRIU action returned an error");
            }
        }
        
    }
    
    @Override
    public String getVersion() {
        int v;
        lock.lock();
        try {
            init();
            v = criu_h.criu_get_version();
        } finally {
            lock.unlock();
        }
        int major = v/10000;
        int minor = (v - major*10000) / 100;
        int sub = v - (major*10000 + minor*100);
        return major+"."+minor+"."+sub;
    }
    
//    public String getAPIVersion() {
//        return CLinker.toJavaString(criu_h.CRIU_VERSION());
//    }
    
    private static interface CRIUAction {
        public int execute();
    }

    // fcntl.h open(const char *__file, int __oflag, ...)
    private static final MethodHandle open = CLinker.systemCLinker().downcallHandle(
        CLinker.systemCLinker().lookup("open").get(),
        FunctionDescriptor.of(criu_h.C_INT, criu_h.C_POINTER, criu_h.C_INT)
    );
    private static final int O_DIRECTORY = 0200000;
    
}
