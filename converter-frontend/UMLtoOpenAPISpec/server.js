const express = require('express');
const path = require('path');
const app = express();

const port = process.env.PORT || 80;

app.use(express.static(__dirname + '/dist/umlto-open-apispec/browser'));

app.get('/*', (req, res) => {
  res.sendFile(path.join(__dirname + '/dist/umlto-open-apispec/browser/index.html'));
});

app.listen(port, () => {
  console.log(`App listening on port ${port}!`);
});
