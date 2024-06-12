const { exec } = require('child_process');

exec('docker-compose up -d', (err, stdout, stderr) => {
  if (err) {
    console.error(`Error starting Docker containers: ${err.message}`);
    process.exit(1);
  }
  console.log(stdout);
  console.error(stderr);
});
