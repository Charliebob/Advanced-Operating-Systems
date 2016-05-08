remoteuser=xxw143130
remotecomputer1=dc01.utdallas.edu
remotecomputer2=dc02.utdallas.edu
remotecomputer3=dc03.utdallas.edu
remotecomputer4=dc04.utdallas.edu
remotecomputer5=dc05.utdallas.edu
remotecomputer6=dc06.utdallas.edu

ssh -l "$remoteuser" "$remotecomputer1" "pkill -9 -u `id -u xxw143130`" &
ssh -l "$remoteuser" "$remotecomputer2" "pkill -9 -u `id -u xxw143130`" &
ssh -l "$remoteuser" "$remotecomputer3" "pkill -9 -u `id -u xxw143130`" &
ssh -l "$remoteuser" "$remotecomputer4" "pkill -9 -u `id -u xxw143130`" &
ssh -l "$remoteuser" "$remotecomputer5" "pkill -9 -u `id -u xxw143130`" &
ssh -l "$remoteuser" "$remotecomputer6" "pkill -9 -u `id -u xxw143130`"

sleep 1;
echo "Cleanup completes!"

