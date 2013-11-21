

# back up database
function backupDB {
	_now=$(date +"%m_%d_%Y");
	_file="$HOME/backups/$1_backup_$_now.sql";
	touch "$_file";
	echo "Starting backup to $_file ..."
	mysqldump -u root -psnipstory $1 > "$_file";
}

backupDB snipstory

# pull in new code
git pull origin master

# enter in git credentials

function restartSnipStory {
	cd $HOME/SnipStoryV1
	_pid=$(cat RUNNING_PID)
	# Reads the password from stdin
	echo "Killing existing process"
	echo snipstory | sudo -S kill $_pid
	echo "Restarting play"
	sudo play "start 80"
}
