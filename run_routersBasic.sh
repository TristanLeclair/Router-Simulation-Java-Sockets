MACHINES=("lab2-2")

tmux new-session \; \
	set-option -g mouse on \; \
	split-window -h \; \
	split-window -h \; \
	select-pane -t 0 \; \
	send-keys "cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; java -jar target/COMP535-1.0-SNAPSHOT-jar-with-dependencies.jar conf/router1.conf" C-m \; \
	select-pane -t 1 \; \
	send-keys "cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; java -jar target/COMP535-1.0-SNAPSHOT-jar-with-dependencies.jar conf/router2.conf" C-m \; \
	select-pane -t 2 \; \
	send-keys "cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; java -jar target/COMP535-1.0-SNAPSHOT-jar-with-dependencies.jar conf/router3.conf" C-m \; \
	
