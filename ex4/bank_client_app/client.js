const Ice = require('ice').Ice;
const Bank = require('./ice_out/Bank').Bank;
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
    const UserI = await Bank.UserPrx.checkedCast(proxy)

    // Cache

    if(cache['banks'][process.argv[2]] === undefined) {
      let bank = { users: {} }
      cache['banks'][process.argv[2]] = bank
    }

    let bank_users = cache['banks'][process.argv[2]]['users']

    let AccountI
    if(bank_users[process.argv[3]] === undefined) {
      console.log('Open an account in this bank:')
      console.log('First Name: ')
      let firstName = await getline()
      console.log('Last Name: ')
      let lastName = await getline()
      console.log('Declare monthly deposit')
      let deposit = parseInt(await getline())
      let res = await UserI.registerNewAccount(firstName, lastName, process.argv[3], deposit)
      bank_users[process.argv[3]] = {
        password: res.password,
        pesel: process.argv[3],
        type: res.accountType._name
      }
      if(bank_users[process.argv[3]]['type'] == 'PREMIUM') AccountI = await Bank.PremiumAccountPrx.checkedCast(res.account)
      else AccountI = await Bank.AccountPrx.checkedCast(res.account)
    } else {
      let res = await UserI.login(getCredentials({
        pesel: bank_users[process.argv[3]]['pesel'], 
        password: bank_users[process.argv[3]]['password']
      }))
      if(bank_users[process.argv[3]]['type'] == 'PREMIUM') AccountI = await Bank.PremiumAccountPrx.checkedCast(res)
      else AccountI = await Bank.AccountPrx.checkedCast(res)
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
          let res = await AccountI.getCurrentState(getCredentials(user))
          console.log(res)
        } else if(user.type == 'PREMIUM' && line == "loan") {
          console.log('Choose currency: ')
          let curr = await getline()
          console.log('Amount: ')
          let amount = await getline()
          let res = await AccountI.takeALoan(
            getCredentials(user), 
            new Bank.Currency(curr), 
            amount
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

    cache['banks'][process.argv[2]]['users'] = bank_users
  } catch(e) {
    console.log(e.toString())
    process.exitCode = 1
  } finally {
    if(communicator) {
      await communicator.destroy()
    }
    fs.writeFileSync('./client-cache.json', JSON.stringify(cache))
    process.exit(0)
  }
})()

// Helpers

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