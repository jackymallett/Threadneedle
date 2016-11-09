// Node.js server to provide browser view of simulation charts generated
// in batch mode.
//
// node chartserver <directory with output dirs>
//
// Todo: Implement rotate through directories
// (c) jacky mallett

var express  = require('express')
var glob = require('globule')
var os = require('os');
var fs = require('fs');

var masterdir = process.argv[2]

var app = express();

var index = 0;
var data = "";

app.use(express.static(__dirname + '/'));

app.get('/', function(req, res)
{
   dirs = glob.find([masterdir + '/*']);

   res.writeHead(200, {'Content-Type' : 'text/html'});
   res.write('<!DOCTYPE html>');
   res.write('<html><head><title>' + dirs[index] + '</title></head></html>');
   res.write('<link rel="stylesheet" type="text/css" href="styles.css" media="screen" />');

   res.write('<h1>' + dirs[index] + '</h1>');
   res.write('<div id="wrapper">');
   res.write('<section id="charts">');
   var files = glob.find([dirs[index] + '/' + '*.png']);

   for(var i = 0; i < files.length; i++)
   {
     res.write('<img src=' + files[i] + '></a>\n');
   }
   res.write('</section>')
   res.write('<section id="model">');

   data = fs.readFileSync(dirs[index] + '/test_config', 'utf8', function(err, data) {});

   rows = data.split(os.EOL);
   cols = 0;

   for(var i = 0; i < rows.length; i++)
   {
      cols = cols < rows[i].length ? rows[i].length + 1 : cols;
   }

   res.write('<textarea cols='+ cols + ' rows=' + (rows.length + 1) + '>'); 
   res.write(data);
   res.write('</textarea>'); 
   res.write('</section>')

   index === dirs.length - 1? index = 0 : index++;
   res.end('');
    
});

var server = app.listen(1234,function()
{
   console.log('listening at http://%s:%s', os.hostname(), server.address().port);
   if(process.argv.length < 2)
      console.log("Error: must specify directory with results");   
   else
      console.log("Displaying charts in: " + process.argv[2]);

}).on('error', function(err)
{
   if(err.errno == 'EADDRINUSE')
      console.log('Port busy/already running');
   else
      console.log(err);

});


