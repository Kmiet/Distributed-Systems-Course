const argparser = require('./argparser')
const monitor = require('./child_monitor')
const zk = require('node-zookeeper-client')
const readline = require('readline');

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

const args = argparser.parseArgs()

// Test if a file exists and is executable
monitor.test(args.exec)

const client = zk.createClient(args.server != null ? args.server : 'localhost:2181')

function traverseChildrenTree(parent, unvisited, tree, cb) {
  client.getChildren(parent, (err, children, stat) => {
    if(err) throw err
    tree.push(parent)
    children.forEach(child => {
      unvisited.push(parent + '/' + child)
    })
    if(unvisited.length > 0) traverseChildrenTree(unvisited.shift(), unvisited, tree, cb)
    else {
      cb(tree)
    }
  })
}

function printChildrenNodeTree() {
  traverseChildrenTree(args.node, [], [], tree => {
    console.log(tree)
  })
}

// ZNode event listener
function watch(e) {
  switch(e.name) {
    case 'NODE_CREATED':
      client.getChildren(args.node, watch, (err) => {
        if(err) throw err
      })
      monitor.runExec()
      break
    case 'NODE_DELETED':
      client.exists(args.node, watch, (err) => {
        if(err) throw err
      })
      monitor.kill()
      break
    case 'NODE_CHILDREN_CHANGED':
      client.getChildren(args.node, watch, (err, children) => {
        if(err) throw(err)
        else console.log("Children: " + children)
      })
      break
    default:
      break
  }
}

client.on('connected', () => {
  try {
    client.exists(args.node, watch, (err, stat) => {
      if(err) throw err
      else if(stat) {
        monitor.runExec()
        client.getChildren(args.node, watch, (error) => {
          if(error) throw error
        })
      }
    })
  } catch(err) {
    console.error(err)
    process.exit(1)
  }
})

client.on('disconnected', () => {
  process.exit(0)
})

process.on('beforeExit', () => monitor.kill())

client.connect()

onInput(() => process.exit(0))

async function onInput(cb) {
  let line = null;
  do {
    line = await getline()
    if(line == "tree") printChildrenNodeTree()
  } while(line != "exit")
  cb()
}

function getline() {
  return new Promise(resolve => {
    rl.on('line', (line) => resolve(line))
  })
}