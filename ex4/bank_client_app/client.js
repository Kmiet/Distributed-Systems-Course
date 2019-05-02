const Ice = require('ice').Ice;
const Bank = require('./Bank').Bank;
const fs = require('fs');
const os = require('os');
const readline = require('readline');

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

(async () => {
  let cache = JSON.parse(fs.readFileSync('./client-cache.json'))
  let communicator
  try {
    communicator = Ice.initialize(process.argv);

    if(process.argv.length != 4) {
      throw new Error("Invalid argument number. Example usage: node client BANK_PORT PESEL");
    }

    const proxy = communicator.stringToProxy("BankAdapter:default -p " + process.argv[2])
    const ClientI = await Bank.ClientPrx.uncheckedCast(proxy)
    console.log(new Bank.Currency('USD')._value)

    // Cache

    if(cache['banks'][process.argv[2]] === undefined) {
      let bank = { users: {} }
      cache['banks'][process.argv[2]] = bank
    }

    let bank_users = cache['banks'][process.argv[2]]['users']

    if(bank_users[process.argv[3]] === undefined) {
      console.log('Open an account in this bank:')
      console.log('First Name: ')
      let firstName = await getline()
      console.log('Last Name: ')
      let lastName = await getline()
      console.log('Declare monthly deposit')
      let deposit = parseInt(await getline())
      let res = await ClientI.registerNewAccount(firstName, lastName, process.argv[3], deposit)
      bank_users[process.argv[3]] = {
        password: res.password,
        pesel: process.argv[3],
        type: res.accountType._name
      }
    } 

    const user = bank_users[process.argv[3]]
    cache['banks'][process.argv[2]]['users'] = bank_users

    // Welcome
    welcome(user.pesel)
    menu(user.type)

    let line = null
    do {
      process.stdout.write("> ")
      line = await getline()
      try {
        if(line == "state") {
          let res = await ClientI.getCurrentState(getCredentials(user))
          console.log(res)
        } else if(user.type == 'PREMIUM' && line == "loan") {
          console.log('Choose currency: ')
          let curr = await getline()
          console.log(new Bank.Currency(curr))
          console.log('Amount: ')
          let amount = await getline()
          console.log('Return date: ')
          let rdate = await getline()
          rdate = toDate(rdate)
          let res = await ClientI.takeALoan(
            getCredentials(user), 
            new Bank.Currency(curr), 
            amount, 
            rdate
          )
          console.log(res)
        } else if(line == "help") {
          menu(user.type)
        } else if(line != "exit") {
          console.log("Unknown command: " + line)
        }
      } catch(e) {
        console.log(e.toString())
      }
    } while(line != "exit")
    process.exit(0)
  } catch(e) {
    console.log(e.toString())
    process.exitCode = 1
  } finally {
    if(communicator) {
      await communicator.destroy()
    }
    fs.writeFileSync('./client-cache.json', JSON.stringify(cache))
  }
})()

// Helpers

function toDate(dateString) {
  tokens = dateString.split('/')
  if(tokens.length != 3) throw new Error('Invalid date format. Use: dd/mm/yy')
  return new Bank.Date(parseInt(tokens[2]), parseInt(tokens[1]), parseInt(tokens[0]))
}

function getCredentials(user) {
  return new Bank.UserCredentials(user.pesel, user.password)
}

function welcome(pesel) {
  console.log('Welcome user ' + pesel + '!')
}

function menu(accountType) {
  console.log(`Usage:
  state: see account state`+ (accountType === 'PREMIUM' ? `
  loan: take a loan` : '') + `
  help
  exit
  `)
}

function getline() {
  return new Promise(resolve => {
    rl.on('line', (line) => resolve(line))
  })
}