# hadoop dns check

A cleaner implemenation `shyam334/hadoop-dns-checker` in scala. (which is a fork from `sujee/hadoop-dns-checker`)

Hadoop is very picky about DNS. Usually it uses java `InetAddress` api's for it.
But there are also places where `JNDI` is used for reverse lookup. (I was bit by a 'behaviour' of HBase with it).

This is meant to be a small utility to quickly diaganose how DNS would work in hadoop/java.

Prerequsites 
------------
For executing :
* `java 1.7 or +`

For building: 
* `sbt`


Assembling the jar
-----------------
Execute `sbt assembly` from the project home, It will produce a 'fat jar' in `target/scala-2.11` directory.

Usage
-----

To print the usage :

java -jar hadoop-dns-checker-assembly-1.0.jar --help

```

Utility for investigating DNS behaviour in java.
Useful in hadoop env.

USAGE:
File input                 :   checkDNS --file <file-1> <file-2> ...
                           :   checkDNS -f
Stdin/Usage in pipe        :   checkDNS --pipe
                           :   checkDNS -p
                           :   checkDNS -i
Command line               :   checkDNS <host-string-1> <host-string-2> ...
Print usage                :   checkDNS -h
                           :   checkDNS --help
Command line with no argument attempt perform a localhost dns lookup.

```

No argument invokation will result in local host lookup.

`java -jar hadoop-dns-checker-assembly-1.0.jar <host-name>`

`java -jar ~/hosts`

Example usage to resolve hosts in `/etc/hosts` aka usage from stdin/pipe.
`awk '{print $2}' /etc/hosts| java -jar hadoop-dns-checker-assembly-1.0.jar --pipe`
