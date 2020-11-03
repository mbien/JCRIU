# JCRIU - Java binding for CRIU
experimental java binding to [criu](https://github.com/checkpoint-restore/criu) currently using project [panama](https://openjdk.java.net/projects/panama/).

## minimal example
```java
    public static void main(String[] args) throws IOException {

        try(CRIUContext criu = CRIUContext.create()
                .logFile("criu.log").logLevel(4).tcpEstablished(false).leaveRunning(false)) {

            if(!Files.exists(path))
                Files.createDirectory(path);

            Path path = Paths.get("/tmp/freezer");

            System.out.println("pre dump pid: "+ProcessHandle.current().pid());
            criu.dump(path);
            System.out.println("post dump/restore pid: "+ProcessHandle.current().pid());

        }
    }

```

1) run above program (currently requires root permissions, but will hopefully change with CAP_CHECKPOINT_RESTORE)
```bash
$ sudo jdk-16-panama+2-193/bin/java -XX:-UsePerfData -Xmx42m -XX:+UseSerialGC\
 --enable-preview --add-modules jdk.incubator.foreign -Dforeign.restricted=permit -cp test.jar:JCRIU-x.x-SNAPSHOT.jar foo.Test
WARNING: Using incubator modules: jdk.incubator.foreign
pre dump pid: 8845
```

2) restore from checkpoint
```bash
$ sudo criu restore --shell-job -D /tmp/freezer/
post dump/restore pid: 8845

```

## build
run download-criu-header.sh once
after that you can build it with mvn clean install or your fav IDE.

## requirements
+ early access Java 16 build with panama (for now)
+ criu installed
+ recent linux kernel (if you experience 100% CPU usage after restore, try a different kernel)

## license
This project is distributed under the MIT License, see LICENSE file.
