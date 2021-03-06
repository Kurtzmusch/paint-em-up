#### description

Paint 'em Up is a top down shooter that can be played over the internet and with bots   

#### playing online

in order to play online someone will have to host a server. there is no server-only aplicaton, so whoever decides to host the server must keep the game running. if using a firewall, the firewall must be correctly configured to allow connections on the chosen port. if behind a router, then the server host will need to enable `port forwarding`

#### dependencyes

- to run:
	- java runtime environment(also called JRE or just Java)   
- to compile/build:
	- java development kit - JDK
	- java lightweight game library - `jlwgl`(included since i dont know if newer versions will work)   

#### installation/setup

single executable file (linux/windows): [download latest .jar file](https://github.com/Kurtzmusch/paint-em-up/releases)

#### building from source

compile all the `.java` files into `.class` files and dump them into `bin/`:\
`javac -d bin -classpath "./lib/lwjgl316/bin/*" @sources.txt`

copy the source shader directory `src/game/shader` in the correct output directory `bin/game/`:\
`cp -r src/game/shader/ bin/game/`

copy all of the directiories that are inside the `res/` directory in the correct output directory `bin/`:\
`cp -r res/* bin/`

then, to run the game from the bytecode files:\
`java -classpath 'bin:lib/lwjgl316/bin/*' game.Game`

#### building a single executable file
extract the required libraries into the `bin/` directory:\
`cd bin/`\
`for f in ../lib/lwjgl316/bin/*; do jar xf $f; done`\
`cd ..`\
optionally, remove unnecessary `.git`, `.sha1` files and the `META-INF` directory:\
`cd bin/`\
`rm -rf META-INF/`\
`rm *.git`\
`rm *.sha1`\
`cd ..`\
package all of the contents inside the `bin/` directory into on single executable jar file:\
`jar --create --file paint-em-up.jar --main-class game.Game -C bin/ .` 
