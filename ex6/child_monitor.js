const fs = require('fs')
const { fork } = require('child_process')

function Monitor() {
  let child;
  let executable;

  function kill() {
    if(child) {
      child.kill()
      child = null
    }
  } 
  
  function runExec() {
    if(!child) child = fork(executable)
  }

  function test(pathToExec) {
    try {
      if(fs.existsSync(pathToExec)) {
        fs.accessSync(pathToExec, fs.constants.X_OK)
        executable = pathToExec
      } else {
        throw 'File ' + pathToExec + ' does not exist'
      }
    } catch(err) {
      console.error(err)
      process.exit(1)
    }
  }

  return {
    kill,
    runExec,
    test
  }
}

module.exports = Monitor()