const { exec } = require('child_process');
const path = require('path');

const prismPath = path.resolve(__dirname, 'node_modules', '.bin', 'prism');
const prismCommand = `${prismPath} mock -p 4010 --cors --host 0.0.0.0 ${path.resolve(__dirname, 'data', 'export.yml')} --errors`;

exec(prismCommand, { cwd: __dirname }, (error, stdout, stderr) => {
    if (error) {
        console.error(`Error starting Prism: ${error.message}`);
        return;
    }
    if (stderr) {
        console.error(`Prism stderr: ${stderr}`);
    }
    console.log(`Prism stdout: ${stdout}`);
});
