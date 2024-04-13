MACHINES=("lab2-2")

tmux new-session \; \
	set-option -g mouse on \; \
	split-window -h \; \
	split-window -v \; \
	split-window -v \; \
    split-window -v \; \
    select-pane -t 0 \; \
    split-window -v \; \
    split-window -v \; \
	select-layout tiled \; \
	select-pane -t 0 \; \
	send-keys "cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; java -cp target/COMP535-1.0-SNAPSHOT-jar-with-dependencies.jar socs.network.Main conf/router1.conf" C-m \; \
	select-pane -t 1 \; \
	send-keys "cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; java -cp target/COMP535-1.0-SNAPSHOT-jar-with-dependencies.jar socs.network.Main conf/router2.conf" C-m \; \
	select-pane -t 2 \; \
	send-keys "cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; java -cp target/COMP535-1.0-SNAPSHOT-jar-with-dependencies.jar socs.network.Main conf/router3.conf" C-m \; \
	select-pane -t 3 \; \
	send-keys "cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; java -cp target/COMP535-1.0-SNAPSHOT-jar-with-dependencies.jar socs.network.Main conf/router4.conf" C-m \; \
    select-pane -t 4 \; \
	send-keys "cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; java -cp target/COMP535-1.0-SNAPSHOT-jar-with-dependencies.jar socs.network.Main conf/router5.conf" C-m \; \
	select-pane -t 5 \; \
	send-keys "cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; java -cp target/COMP535-1.0-SNAPSHOT-jar-with-dependencies.jar socs.network.Main conf/router6.conf" C-m \; \
	select-pane -t 6 \; \
	send-keys "cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; java -cp target/COMP535-1.0-SNAPSHOT-jar-with-dependencies.jar socs.network.Main conf/router7.conf" C-m \; \
