wget "https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2"
tar xvfj phantomjs-2.1.1-linux-x86_64.tar.bz2
cd phantomjs-2.1.1-linux-x86_64
sed -e 's/}, 200);/}, 10000);/' examples/rasterize.js > rasterize.js
bin/phantomjs rasterize.js "http://$1:3000/dashboard/db/scylla-per-server-metrics?from=now-$2m&to=now" $3/grafana-snapshot.png

