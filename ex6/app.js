const argparser = require('./argparser')
const monitor = require('./child_monitor')
const zk = require('node-zookeeper-client')

const args = argparser.parseArgs()

// Test if a file exists and is executable
monitor.test(args.exec)

const client = zk.createClient(args.server != null ? args.server : 'localhost:2181')

client.on('connected', () => {
  try {
    client.exists(args.node, (e) => {
      switch(e) {
        case zk.Event.NODE_CREATED:
          monitor.runExec()
          break
        case zk.Event.NODE_DELETED:
          monitor.kill()
          break
        case zk.Event.NODE_CHILDREN_CHANGED:
          client.getChildren(args.node, (err, children, stat) => {
            if(err) throw err
            else console.log("Children: " + children)
          })
          break
        default:
          break
      }
    }, (err, stat) => {
      if(err) throw err
      else if(stat) monitor.runExec()
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