# JCRIU - Java binding for CRIU
experimental java binding to [criu](https://github.com/checkpoint-restore/criu) currently using project [panama](https://openjdk.java.net/projects/panama/).

## minimal example
```java
    public static void main(String[] args) throws IOException {

        try(CRIUContext criu = CRIUContext.create()
                .logFile("criu.log").logLevel(INFO).tcpEstablished(false).leaveRunning(false)) {

            Path path = Paths.get("/tmp/freezer");

            if(!Files.exists(path))
                Files.createDirectory(path);

            System.out.println("pre checkpoint pid: "+ProcessHandle.current().pid());
            criu.checkpoint(path);
            System.out.println("post checkpoint/restore pid: "+ProcessHandle.current().pid());

        }
    }

```

1) run above program (currently requires root permissions, but will hopefully change with CAP_CHECKPOINT_RESTORE)
```bash
$ sudo jdk-16-panama+2-193/bin/java -XX:-UsePerfData -Xmx42m -XX:+UseSerialGC\
 --enable-preview --add-modules jdk.incubator.foreign -Dforeign.restricted=permit\
 -cp test.jar:JCRIU-x.x-SNAPSHOT.jar foo.Test
WARNING: Using incubator modules: jdk.incubator.foreign
pre checkpoint pid: 8845
```

2) restore from checkpoint
```bash
$ sudo criu restore --shell-job -D /tmp/freezer/
post checkpoint/restore pid: 8845

```

## build
1) run download-criu-header.sh once
2) build project with 'mvn clean install' or your fav. IDE

## requirements
+ early access [Java 16 panama build](https://jdk.java.net/panama/) (for now)
+ criu installed
+ recent linux kernel (if you experience 100% CPU usage after restore, try a different kernel)

## license
This project is distributed under the MIT License, see LICENSE file.
