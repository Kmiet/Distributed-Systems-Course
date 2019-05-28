const ArgumentParser = require('argparse').ArgumentParser;

const parser = new ArgumentParser({
  addHelp: true,
  description: 'Zookeeper example'
})

parser.addArgument(['exec'], {
  help: 'Path to executable'
})

parser.addArgument(['node'], {
  help: 'ZNode to watch'
})

parser.addArgument(['-s', '--server'], {
  help: 'address:port(,address2:port2)*'
})

module.exports = parser